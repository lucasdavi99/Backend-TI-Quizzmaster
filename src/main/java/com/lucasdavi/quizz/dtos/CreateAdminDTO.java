package com.lucasdavi.quizz.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAdminDTO(
        @NotBlank(message = "Username é obrigatório")
        @Size(min = 3, max = 20, message = "Username deve ter entre 3 e 20 caracteres")
        String username,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ter formato válido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
        String password
) {
}