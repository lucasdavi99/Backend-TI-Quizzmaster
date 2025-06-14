package com.lucasdavi.quizz.controllers;

import com.lucasdavi.quizz.dtos.AdminUserDTO;
import com.lucasdavi.quizz.dtos.CreateAdminDTO;
import com.lucasdavi.quizz.services.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Lista todos os usuários do sistema com suas estatísticas
     * Apenas ADMIN pode acessar
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        try {
            List<AdminUserDTO> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated") || e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Busca um usuário específico por ID
     * Apenas ADMIN pode acessar
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long userId) {
        try {
            AdminUserDTO user = adminService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("not authenticated") || e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Promove um usuário a administrador
     * Apenas ADMIN pode acessar
     */
    @PutMapping("/users/{userId}/promote")
    public ResponseEntity<?> promoteUser(@PathVariable Long userId) {
        try {
            AdminUserDTO updatedUser = adminService.promoteUserToAdmin(userId);

            return ResponseEntity.ok(Map.of(
                    "message", "Usuário promovido a administrador com sucesso",
                    "user", updatedUser,
                    "success", true
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Usuário não encontrado",
                        "message", "O usuário especificado não existe"
                ));
            }
            if (e.getMessage().contains("already admin")) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Usuário já é administrador",
                        "message", "Este usuário já possui privilégios de administrador"
                ));
            }
            if (e.getMessage().contains("not authenticated") || e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "Acesso negado",
                        "message", "Apenas administradores podem promover usuários"
                ));
            }
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erro interno do servidor",
                    "message", "Não foi possível promover o usuário"
            ));
        }
    }

    /**
     * Rebaixa um administrador a usuário comum
     * Apenas ADMIN pode acessar
     */
    @PutMapping("/users/{userId}/demote")
    public ResponseEntity<?> demoteUser(@PathVariable Long userId) {
        try {
            AdminUserDTO updatedUser = adminService.demoteAdminToUser(userId);

            return ResponseEntity.ok(Map.of(
                    "message", "Administrador rebaixado a usuário com sucesso",
                    "user", updatedUser,
                    "success", true
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Usuário não encontrado",
                        "message", "O usuário especificado não existe"
                ));
            }
            if (e.getMessage().contains("not admin")) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Usuário não é administrador",
                        "message", "Este usuário não possui privilégios de administrador"
                ));
            }
            if (e.getMessage().contains("cannot demote self")) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Operação não permitida",
                        "message", "Você não pode rebaixar a si mesmo"
                ));
            }
            if (e.getMessage().contains("not authenticated") || e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "Acesso negado",
                        "message", "Apenas administradores podem rebaixar outros administradores"
                ));
            }
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erro interno do servidor",
                    "message", "Não foi possível rebaixar o usuário"
            ));
        }
    }

    /**
     * Cria um novo administrador
     * Apenas ADMIN pode acessar
     */
    @PostMapping("/users/create-admin")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateAdminDTO createAdminDTO) {
        try {
            AdminUserDTO newAdmin = adminService.createAdmin(createAdminDTO);

            return ResponseEntity.status(201).body(Map.of(
                    "message", "Administrador criado com sucesso",
                    "user", newAdmin,
                    "success", true
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("username already exists")) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "Nome de usuário já existe",
                        "message", "Este username já está em uso"
                ));
            }
            if (e.getMessage().contains("email already exists")) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "Email já existe",
                        "message", "Este email já está cadastrado"
                ));
            }
            if (e.getMessage().contains("not authenticated") || e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "Acesso negado",
                        "message", "Apenas administradores podem criar outros administradores"
                ));
            }
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erro interno do servidor",
                    "message", "Não foi possível criar o administrador"
            ));
        }
    }

    /**
     * Deleta um usuário e todos os seus dados
     * Apenas ADMIN pode acessar
     * CUIDADO: Esta operação é irreversível!
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            String deletedUsername = adminService.deleteUser(userId);

            return ResponseEntity.ok(Map.of(
                    "message", "Usuário deletado com sucesso",
                    "deletedUser", deletedUsername,
                    "success", true
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Usuário não encontrado",
                        "message", "O usuário especificado não existe"
                ));
            }
            if (e.getMessage().contains("cannot delete self")) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Operação não permitida",
                        "message", "Você não pode deletar sua própria conta pelo painel admin"
                ));
            }
            if (e.getMessage().contains("not authenticated") || e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "Acesso negado",
                        "message", "Apenas administradores podem deletar usuários"
                ));
            }
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erro interno do servidor",
                    "message", "Não foi possível deletar o usuário"
            ));
        }
    }

    /**
     * Busca estatísticas do usuário específico
     * Apenas ADMIN pode acessar
     */
    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<?> getUserStats(@PathVariable Long userId) {
        try {
            Map<String, Object> stats = adminService.getUserDetailedStats(userId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Usuário não encontrado"
                ));
            }
            if (e.getMessage().contains("not authenticated") || e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Busca estatísticas gerais do sistema
     * Apenas ADMIN pode acessar
     */
    @GetMapping("/system/stats")
    public ResponseEntity<?> getSystemStats() {
        try {
            Map<String, Object> stats = adminService.getSystemStats();
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated") || e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Busca relatório de saúde do sistema
     * Apenas ADMIN pode acessar
     */
    @GetMapping("/system/health")
    public ResponseEntity<?> getSystemHealth() {
        try {
            Map<String, Object> health = adminService.getSystemHealth();
            return ResponseEntity.ok(health);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated") || e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Força limpeza completa do sistema (sessões abandonadas, scores zero, etc.)
     * Apenas ADMIN pode acessar
     * CUIDADO: Operação agressiva de limpeza!
     */
    @DeleteMapping("/system/force-cleanup")
    public ResponseEntity<?> forceSystemCleanup() {
        try {
            Map<String, Object> result = adminService.forceSystemCleanup();

            return ResponseEntity.ok(Map.of(
                    "message", "Limpeza forçada executada com sucesso",
                    "results", result,
                    "success", true
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated") || e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "Acesso negado",
                        "message", "Apenas administradores podem executar limpeza forçada"
                ));
            }
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erro interno do servidor",
                    "message", "Não foi possível executar a limpeza forçada"
            ));
        }
    }
}