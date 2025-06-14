package com.lucasdavi.quizz.dtos;

import jakarta.validation.constraints.NotNull;

public record AnswerQuestionDTO(@NotNull Long answerId) {
}
