package com.lucasdavi.quizz.dtos;

import com.lucasdavi.quizz.models.Answer;
import com.lucasdavi.quizz.models.Question;

public record AnswerDTO(Long id, String content, Boolean isCorrect) {
    public Answer toAnswer(Question question) {
        Answer answer = new Answer();
        answer.setContent(this.content());
        answer.setIsCorrect(this.isCorrect());
        answer.setQuestion(question);
        return answer;
    }
}
