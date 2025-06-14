package com.lucasdavi.quizz.dtos;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordDTO( @NotBlank String token, @NotBlank String newPassword) {
}
