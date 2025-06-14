package com.lucasdavi.quizz.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final HttpClient httpClient;

    public EmailService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean sendPasswordResetEmail(String toEmail, String username, String resetCode) {
        try {
            String emailContent = buildPasswordResetEmailContent(username, resetCode);
            return sendEmail(toEmail, "🔐 Código para Redefinir sua Senha", emailContent);
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar email de reset: " + e.getMessage());
            return false;
        }
    }

    private boolean sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            String requestBody = String.format("""
                {
                    "sender": {
                        "name": "%s",
                        "email": "%s"
                    },
                    "to": [
                        {
                            "email": "%s"
                        }
                    ],
                    "subject": "%s",
                    "htmlContent": "%s"
                }
                """, senderName, senderEmail, toEmail, subject,
                    htmlContent.replace("\"", "\\\"").replace("\n", "\\n"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                System.out.println("✅ Email enviado com sucesso para: " + toEmail);
                return true;
            } else {
                System.err.println("❌ Erro no envio do email. Status: " + response.statusCode() +
                        ", Response: " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Exceção ao enviar email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String buildPasswordResetEmailContent(String username, String resetCode) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                             color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .code-box { background: #fff; border: 2px solid #667eea; border-radius: 8px; 
                               padding: 20px; text-align: center; margin: 20px 0; }
                    .reset-code { font-size: 32px; font-weight: bold; color: #667eea; 
                                 letter-spacing: 3px; font-family: monospace; }
                    .warning { background: #fff3cd; border-left: 4px solid #ffc107; 
                              padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Redefinição de Senha</h1>
                        <p>Quiz App - Seu código de segurança</p>
                    </div>
                    
                    <div class="content">
                        <h2>Olá, %s!</h2>
                        
                        <p>Você solicitou a redefinição da sua senha. Use o código abaixo para criar uma nova senha:</p>
                        
                        <div class="code-box">
                            <p style="margin: 0; color: #666;">Seu código de verificação:</p>
                            <div class="reset-code">%s</div>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Importante:</strong>
                            <ul style="margin: 10px 0 0 0;">
                                <li>Este código expira em <strong>15 minutos</strong></li>
                                <li>Use apenas na página oficial do Quiz App</li>
                                <li>Não compartilhe este código com ninguém</li>
                            </ul>
                        </div>
                        
                        <p>Se você não solicitou esta redefinição, ignore este email. Sua senha permanecerá inalterada.</p>
                        
                        <p>Atenciosamente,<br><strong>Equipe de um homem só QuizzMaster</strong></p>
                    </div>
                    
                    <div class="footer">
                        <p>Este é um email automático. Não responda a esta mensagem.</p>
                    </div>
                </div>
            </body>
            </html>
            """, username, resetCode);
    }
}
