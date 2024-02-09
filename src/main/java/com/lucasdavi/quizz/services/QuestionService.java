package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.models.Question;
import com.lucasdavi.quizz.repositories.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public Question saveQuestion(Question question) {
        return this.questionRepository.save(question);
    }

    public Optional<Question> getQuestionById(Long id) {
        return this.questionRepository.findById(id);
    }

    public List<Question> getAllQuestions() {
        List<Question> questions = questionRepository.findAll();
        Collections.shuffle(questions);
        return questions;
    }

    public Question updateQuestionById(Long id, Question question) {
        Optional<Question> questionData = this.questionRepository.findById(id);
        if (questionData.isPresent()) {
            Question _question = questionData.get();
            _question.setContent(question.getContent());
            return this.questionRepository.save(_question);
        } else {
            return null;
        }
    }

    public void deleteQuestionById(Long id) {
        this.questionRepository.deleteById(id);
    }
}
