package com.lucasdavi.quizz.dtos;

import com.lucasdavi.quizz.enums.UserRole;
import java.time.LocalDateTime;

public record AdminUserDTO(
        Long id,
        String username,
        String email,
        UserRole role,
        LocalDateTime createdAt,
        Integer totalGames,
        Integer bestScore,
        Double averageScore,
        Integer totalPoints,
        Long rankingPosition,
        LocalDateTime lastGameDate,
        Integer activeSessions,
        String status
) {
}