package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.dtos.ForgotPasswordDTO;
import com.lucasdavi.quizz.dtos.ResetPasswordDTO;
import com.lucasdavi.quizz.models.PasswordResetToken;
import com.lucasdavi.quizz.models.User;
import com.lucasdavi.quizz.repositories.PasswordResetTokenRepository;
import com.lucasdavi.quizz.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Transactional
    public boolean sendPasswordResetCode(ForgotPasswordDTO dto) {
        try {
            // 1. Busca usuário pelo email
            User user = userRepository.findByEmail(dto.email());
            if (user == null) {
                // Por segurança, não revelamos se o email existe ou não
                System.out.println("🔍 Tentativa de reset para email não encontrado: " + dto.email());
                return true; // Retorna true para não dar dica se email existe
            }

            // 2. Verifica rate limiting (máximo 3 tentativas por hora)
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long recentAttempts = tokenRepository.countRecentTokensByEmail(dto.email(), oneHourAgo);

            if (recentAttempts >= 3) {
                System.out.println("⚠️ Rate limit atingido para email: " + dto.email());
                throw new RuntimeException("Muitas tentativas. Tente novamente em 1 hora.");
            }

            // 3. Desativa tokens ativos anteriores do usuário
            tokenRepository.deactivateAllTokensByUser(user);

            // 4. Gera novo código de 6 dígitos
            String resetCode = generateResetCode();

            // 5. Cria novo token (expira em 15 minutos)
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setEmail(dto.email());
            token.setToken(resetCode);
            token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            token.setCreatedAt(LocalDateTime.now());

            tokenRepository.save(token);

            // 6. Envia email
            boolean emailSent = emailService.sendPasswordResetEmail(
                    dto.email(),
                    user.getUsername(),
                    resetCode
            );

            if (!emailSent) {
                throw new RuntimeException("Erro no envio do email. Tente novamente.");
            }

            System.out.println("✅ Código de reset enviado para: " + dto.email());
            return true;

        } catch (RuntimeException e) {
            throw e; // Re-lança exceptions conhecidas
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado no reset de senha: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro interno. Tente novamente mais tarde.");
        }
    }

    @Transactional
    public boolean resetPassword(ResetPasswordDTO dto) {
        try {
            // 1. Busca token válido
            PasswordResetToken token = tokenRepository
                    .findByTokenAndIsActiveTrue(dto.token())
                    .orElseThrow(() -> new RuntimeException("Código inválido ou expirado"));

            // 2. Verifica se token ainda é válido
            if (!token.isValid()) {
                throw new RuntimeException("Código inválido ou expirado");
            }

            // 3. Valida nova senha
            if (dto.newPassword().length() < 6) {
                throw new RuntimeException("A nova senha deve ter pelo menos 6 caracteres");
            }

            // 4. Atualiza senha do usuário
            User user = token.getUser();
            String encodedPassword = passwordEncoder.encode(dto.newPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);

            // 5. Marca token como usado
            token.setUsedAt(LocalDateTime.now());
            token.setIsActive(false);
            tokenRepository.save(token);

            // 6. Desativa todos os outros tokens do usuário
            tokenRepository.deactivateAllTokensByUser(user);

            System.out.println("✅ Senha redefinida com sucesso para usuário: " + user.getUsername());
            return true;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Erro ao redefinir senha: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro interno. Tente novamente.");
        }
    }

    private String generateResetCode() {
        // Gera código de 6 dígitos (100000 a 999999)
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    /**
     * Limpeza automática de tokens expirados - executa a cada hora
     */
    @Scheduled(cron = "0 0 * * * *") // A cada hora
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24); // Remove tokens > 24h
        int deletedCount = tokenRepository.deleteExpiredTokens(cutoffTime);

        if (deletedCount > 0) {
            System.out.println("🧹 Limpeza automática: removidos " + deletedCount + " tokens expirados");
        }
    }
}