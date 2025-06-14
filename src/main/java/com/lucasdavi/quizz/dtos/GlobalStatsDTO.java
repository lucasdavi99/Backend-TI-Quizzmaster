package com.lucasdavi.quizz.dtos;

public record GlobalStatsDTO(Long totalPlayers,
                             Long totalGames,
                             Integer highestScore,
                             Double averageScore,
                             Long totalPoints,
                             String topPlayer) {
}
