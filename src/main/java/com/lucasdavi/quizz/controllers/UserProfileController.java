package com.lucasdavi.quizz.controllers;

import com.lucasdavi.quizz.dtos.UserProfileDTO;
import com.lucasdavi.quizz.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    /**
     * Busca informações do perfil do usuário atual
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile() {
        try {
            UserProfileDTO profile = userProfileService.getCurrentUserProfile();
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated")) {
                return ResponseEntity.status(401).build();
            }
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Atualiza o username do usuário atual
     */
    @PutMapping("/username")
    public ResponseEntity<?> updateUsername(@RequestBody Map<String, String> request) {
        try {
            String newUsername = request.get("newUsername");

            if (newUsername == null || newUsername.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Username é obrigatório",
                        "message", "Informe o novo username"
                ));
            }

            UserProfileDTO updatedProfile = userProfileService.updateUsername(newUsername.trim());

            return ResponseEntity.ok(Map.of(
                    "message", "Username alterado com sucesso",
                    "user", updatedProfile,
                    "success", true
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated")) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "Usuário não autenticado",
                        "message", "Faça login para continuar"
                ));
            }
            if (e.getMessage().contains("already exists") || e.getMessage().contains("já está em uso")) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "Username já está em uso",
                        "message", "Escolha um nome de usuário diferente"
                ));
            }
            if (e.getMessage().contains("invalid")) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Username inválido",
                        "message", "Use 3-20 caracteres (letras, números, _ e -)"
                ));
            }
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erro interno do servidor",
                    "message", "Não foi possível alterar o username. Tente novamente."
            ));
        }
    }

    /**
     * Altera a senha do usuário atual
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Senha atual é obrigatória",
                        "message", "Informe sua senha atual"
                ));
            }

            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Nova senha muito curta",
                        "message", "A nova senha deve ter pelo menos 6 caracteres"
                ));
            }

            userProfileService.changePassword(currentPassword, newPassword);

            return ResponseEntity.ok(Map.of(
                    "message", "Senha alterada com sucesso",
                    "success", true
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated")) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "Usuário não autenticado",
                        "message", "Faça login para continuar"
                ));
            }
            if (e.getMessage().contains("incorrect") || e.getMessage().contains("incorreta")) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Senha atual incorreta",
                        "message", "A senha atual informada está incorreta"
                ));
            }
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erro interno do servidor",
                    "message", "Não foi possível alterar a senha. Tente novamente."
            ));
        }
    }

    /**
     * Exclui a conta do usuário atual
     */
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, String> request) {
        try {
            String currentPassword = request.get("currentPassword");

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Senha obrigatória",
                        "message", "Informe sua senha atual para confirmar a exclusão"
                ));
            }

            userProfileService.deleteAccount(currentPassword);

            return ResponseEntity.ok(Map.of(
                    "message", "Conta excluída com sucesso",
                    "success", true
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated")) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "Usuário não autenticado",
                        "message", "Faça login para continuar"
                ));
            }
            if (e.getMessage().contains("incorrect") || e.getMessage().contains("incorreta")) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Senha incorreta",
                        "message", "A senha informada está incorreta"
                ));
            }
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erro interno do servidor",
                    "message", "Não foi possível excluir a conta. Tente novamente."
            ));
        }
    }

    /**
     * Busca estatísticas detalhadas do usuário (para a seção de histórico)
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            Map<String, Object> stats = userProfileService.getUserDetailedStats();
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated")) {
                return ResponseEntity.status(401).build();
            }
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Endpoint específico para verificar se o usuário atual é administrador
     */
    @GetMapping("/admin-check")
    public ResponseEntity<?> checkAdminStatus() {
        try {
            UserProfileDTO profile = userProfileService.getCurrentUserProfile();

            boolean isAdmin = profile.role() != null && profile.role().name().equals("ADMIN");

            Map<String, Object> response = new HashMap<>();
            response.put("isAdmin", isAdmin);
            response.put("role", profile.role() != null ? profile.role().name() : "UNKNOWN");
            response.put("username", profile.username());
            response.put("userId", profile.id());

            System.out.println("🔍 Admin check para " + profile.username() +
                    " - Role: " + profile.role() +
                    " - IsAdmin: " + isAdmin);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated")) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "Not authenticated",
                        "isAdmin", false
                ));
            }
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Internal server error",
                    "isAdmin", false
            ));
        }
    }
}