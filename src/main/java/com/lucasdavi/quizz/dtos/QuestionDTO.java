package com.lucasdavi.quizz.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuestionDTO(Long id, @NotNull String content, @NotNull List<AnswerDTO> answers) {
}
