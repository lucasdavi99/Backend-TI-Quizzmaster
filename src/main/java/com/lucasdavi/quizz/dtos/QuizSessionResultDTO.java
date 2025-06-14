package com.lucasdavi.quizz.dtos;

import java.time.LocalDateTime;

public record QuizSessionResultDTO(Long sessionId,
                                   Integer finalScore,
                                   Integer totalQuestions,
                                   Boolean wasCompleted,
                                   LocalDateTime createdAt,
                                   LocalDateTime finishedAt,
                                   String message) {
}
