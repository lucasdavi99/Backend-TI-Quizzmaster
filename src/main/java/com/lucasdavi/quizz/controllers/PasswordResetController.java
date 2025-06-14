package com.lucasdavi.quizz.controllers;

import com.lucasdavi.quizz.dtos.ForgotPasswordDTO;
import com.lucasdavi.quizz.dtos.ResetPasswordDTO;
import com.lucasdavi.quizz.services.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        try {
            boolean success = passwordResetService.sendPasswordResetCode(dto);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "message", "Se o email estiver cadastrado, você receberá um código para redefinir sua senha",
                        "success", true,
                        "info", "Verifique sua caixa de entrada e spam"
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                        "error", "Erro no envio do email",
                        "message", "Tente novamente mais tarde"
                ));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Erro na solicitação",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erro interno do servidor",
                    "message", "Tente novamente mais tarde"
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        try {
            boolean success = passwordResetService.resetPassword(dto);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "message", "Senha redefinida com sucesso!",
                        "success", true,
                        "info", "Você já pode fazer login com sua nova senha"
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                        "error", "Erro na redefinição",
                        "message", "Tente novamente"
                ));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Erro na validação",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erro interno do servidor",
                    "message", "Tente novamente mais tarde"
            ));
        }
    }
}
