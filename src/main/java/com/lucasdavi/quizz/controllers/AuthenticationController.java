package com.lucasdavi.quizz.controllers;

import com.lucasdavi.quizz.configs.security.TokenService;
import com.lucasdavi.quizz.dtos.AuthenticationDTO;
import com.lucasdavi.quizz.dtos.LoginResponseDTO;
import com.lucasdavi.quizz.dtos.RegisterDTO;
import com.lucasdavi.quizz.models.User;
import com.lucasdavi.quizz.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationDTO data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = this.authManager.authenticate(usernamePassword);
            var token = this.tokenService.generateToken((User) auth.getPrincipal());
            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Credenciais inválidas",
                    "message", "Usuário ou senha incorretos"
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO data) {
        try {
            // 🔧 CORREÇÃO: Verificação mais robusta de duplicatas
            if (this.userRepository.findByUsername(data.login()) != null) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "Nome de usuário já está em uso",
                        "field", "username",
                        "message", "Escolha um nome de usuário diferente"
                ));
            }

            if (this.userRepository.findByEmail(data.email()) != null) {
                return ResponseEntity.status(409).body(Map.of(
                    "error", "Email já está em uso",
                    "field", "email",
                    "message", "Este email já está cadastrado no sistema"
                ));
            }

            String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
            User newUser = new User(data.login(), encryptedPassword, data.email());
            User savedUser = this.userRepository.save(newUser);

            // ✅ CORREÇÃO PRINCIPAL: Retorna JSON consistente
            return ResponseEntity.status(201).body(Map.of(
                    "message", "Usuário criado com sucesso",
                    "userId", savedUser.getId(),
                    "username", savedUser.getUsername(),
                    "success", true
            ));

        } catch (Exception e) {
            // 🔧 CORREÇÃO: Tratamento de erro robusto
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erro interno do servidor",
                    "message", "Não foi possível criar o usuário. Tente novamente.",
                    "details", e.getMessage()
            ));
        }
    }
}
