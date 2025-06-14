// UserProfileDTO.java - ÚNICO DTO necessário para responses
package com.lucasdavi.quizz.dtos;

import com.lucasdavi.quizz.enums.UserRole;

import java.time.LocalDateTime;

public record UserProfileDTO(
        Long id,
        String username,
        String email,
        UserRole role,
        LocalDateTime createdAt,
        String status
) {
}