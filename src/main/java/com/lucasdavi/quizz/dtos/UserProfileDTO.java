// UserProfileDTO.java - ÚNICO DTO necessário para responses
package com.lucasdavi.quizz.dtos;

import java.time.LocalDateTime;

public record UserProfileDTO(
        Long id,
        String username,
        String email,
        LocalDateTime createdAt,
        String status
) {
}