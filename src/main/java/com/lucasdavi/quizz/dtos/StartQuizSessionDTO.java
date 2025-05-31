package com.lucasdavi.quizz.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StartQuizSessionDTO(@NotNull @Min(1) Integer numberOfQuestions) {
}
