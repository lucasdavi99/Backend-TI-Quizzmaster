package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.models.Answer;
import com.lucasdavi.quizz.repositories.AnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    public Answer saveAnswer(Answer answer) {
        return this.answerRepository.save(answer);
    }

    public Optional<Answer> getAnswerById(Long id) {
        return this.answerRepository.findById(id);
    }

    public List<Answer> getAllAnswers() {
        return this.answerRepository.findAll();
    }

    public Answer updateAnswerById(Long id, Answer answer) {
        Optional<Answer> answerData = this.answerRepository.findById(id);
        if (answerData.isPresent()) {
            Answer _answer = answerData.get();
            _answer.setContent(answer.getContent());
            _answer.setIsCorrect(answer.getIsCorrect());
            return this.answerRepository.save(_answer);
        } else {
            return null;
        }
    }

    public void deleteAnswerById(Long id) {
        this.answerRepository.deleteById(id);
    }
}
