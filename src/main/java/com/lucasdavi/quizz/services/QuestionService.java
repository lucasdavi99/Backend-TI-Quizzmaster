package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.dtos.AnswerDTO;
import com.lucasdavi.quizz.dtos.QuestionDTO;
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

    public Question saveQuestion(QuestionDTO data) {
        Question question = new Question();

        question.setContent(data.content());
        question.setAnswers(data.answers().stream()
                .map(answerDTO -> answerDTO.toAnswer(question))
                .toList());
        return this.questionRepository.save(question);
    }

    public Optional<QuestionDTO> getQuestionDTOById(Long id) {
        return this.questionRepository.findById(id)
                .map(question -> new QuestionDTO(
                        question.getId(),
                        question.getContent(),
                        question.getAnswers().stream()
                                .map(answer -> new AnswerDTO(answer.getId(), answer.getContent(), answer.getIsCorrect()))
                                .toList()
                ));
    }
    public List<QuestionDTO> getAllQuestionsDTO() {
        return this.questionRepository.findAll().stream()
                .map(question -> new QuestionDTO(
                    question.getId(),
                    question.getContent(),
                    question.getAnswers().stream()
                            .map(answer -> new AnswerDTO(answer.getId(), answer.getContent(), answer.getIsCorrect()))
                            .toList()
                ))
                .toList();
    }

    public QuestionDTO updateQuestionById(Long id, QuestionDTO questionDTO) {
        Optional<Question> questionData = this.questionRepository.findById(id);
        if (questionData.isPresent()) {
            Question question = questionData.get();
            question.setContent(questionDTO.content());

            // Atualiza as respostas existentes ou adiciona novas
            // Importante: esta implementação substitui todas as respostas existentes
            question.getAnswers().clear();
            question.getAnswers().addAll(questionDTO.answers().stream()
                    .map(answerDTO -> answerDTO.toAnswer(question))
                    .toList());

            Question updatedQuestion = this.questionRepository.save(question);

            // Converte a entidade de volta para DTO antes de retornar
            return new QuestionDTO(
                    updatedQuestion.getId(),
                    updatedQuestion.getContent(),
                    updatedQuestion.getAnswers().stream()
                            .map(answer -> new AnswerDTO(answer.getId(), answer.getContent(), answer.getIsCorrect()))
                            .toList()
            );
        } else {
            return null;
        }
    }

    public void deleteQuestionById(Long id) {
        this.questionRepository.deleteById(id);
    }
}
