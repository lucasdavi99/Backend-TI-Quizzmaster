package com.lucasdavi.quizz.dtos;

import java.time.LocalDateTime;

public record UserStatsDTO(
        Long userId,
        String username,
        Integer totalGames,
        Integer bestScore,
        Double averageScore,
        Integer totalPoints,
        Long rankingPosition,
        Integer gamesWon,
        Integer gamesLost,
        Double winRate,
        LocalDateTime lastGameDate,
        LocalDateTime joinDate
) {
}
