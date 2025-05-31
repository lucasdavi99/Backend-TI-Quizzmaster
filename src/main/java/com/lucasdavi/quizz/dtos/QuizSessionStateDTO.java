package com.lucasdavi.quizz.dtos;

import java.time.LocalDateTime;

public record QuizSessionStateDTO(Long sessionId,
                                  Integer currentQuestionIndex,
                                  Integer totalQuestions,
                                  Integer score,
                                  Boolean isActive,
                                  Boolean isCompleted,
                                  QuestionForSessionDTO currentQuestion,
                                  LocalDateTime createdAt,
                                  LocalDateTime finishedAt) {
}
