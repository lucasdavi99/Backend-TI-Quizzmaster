package com.lucasdavi.quizz.dtos;

import com.lucasdavi.quizz.enums.Difficulty;
import jakarta.validation.constraints.NotNull;

import java.util.List;


public record QuestionDTO(Long id, @NotNull String content, @NotNull Difficulty difficulty, @NotNull List<AnswerDTO> answers) {
}

