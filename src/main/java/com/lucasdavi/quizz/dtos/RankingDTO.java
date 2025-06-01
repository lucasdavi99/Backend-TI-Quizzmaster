package com.lucasdavi.quizz.dtos;

public record RankingDTO(Long userId,
                         String username,
                         Integer bestScore,
                         Integer totalGames,
                         Double averageScore,
                         Integer totalPoints,
                         Long position) {

    public RankingDTO withPosition(Long position) {
        return new RankingDTO(
                this.userId,
                this.username,
                this.bestScore,
                this.totalGames,
                this.averageScore,
                this.totalPoints,
                position
        );
    }
}
