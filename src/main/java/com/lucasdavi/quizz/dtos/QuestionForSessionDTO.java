package com.lucasdavi.quizz.dtos;

import java.util.List;

public record QuestionForSessionDTO(Long id, String content, List<AnswerForSessionDTO> answers) {
}
