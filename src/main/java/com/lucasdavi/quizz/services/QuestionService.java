package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.dtos.AnswerDTO;
import com.lucasdavi.quizz.dtos.QuestionDTO;
import com.lucasdavi.quizz.models.Question;
import com.lucasdavi.quizz.repositories.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Transactional
    public Question saveQuestion(QuestionDTO data) {
        Question question = new Question();

        question.setContent(data.content());
        question.setAnswers(data.answers().stream()
                .map(answerDTO -> answerDTO.toAnswer(question))
                .toList());
        return this.questionRepository.save(question);
    }


    @Transactional(readOnly = true)
    public Optional<QuestionDTO> getQuestionDTOById(Long id) {
        // Usa o método específico que faz JOIN FETCH das answers
        Question question = questionRepository.findByIdWithAnswers(id);

        if (question == null) {
            return Optional.empty();
        }

        return Optional.of(new QuestionDTO(
                question.getId(),
                question.getContent(),
                question.getAnswers().stream()
                        .map(answer -> new AnswerDTO(
                                answer.getId(),
                                answer.getContent(),
                                answer.getIsCorrect()
                        ))
                        .toList()
        ));
    }

    @Transactional(readOnly = true)
    public List<QuestionDTO> getAllQuestionsDTO() {
        // Usa o método otimizado que faz JOIN FETCH
        List<Question> questions = questionRepository.findAllWithAnswers();

        return questions.stream()
                .map(question -> {
                    List<AnswerDTO> answerDTOs = question.getAnswers().stream()
                            .map(answer -> new AnswerDTO(
                                    answer.getId(),
                                    answer.getContent(),
                                    answer.getIsCorrect()
                            ))
                            .toList();

                    return new QuestionDTO(
                            question.getId(),
                            question.getContent(),
                            answerDTOs
                    );
                })
                .toList();
    }

    @Transactional
    public QuestionDTO updateQuestionById(Long id, QuestionDTO questionDTO) {
        // Primeiro busca a question com answers
        Question question = questionRepository.findByIdWithAnswers(id);

        if (question == null) {
            return null;
        }

        // Atualiza o conteúdo
        question.setContent(questionDTO.content());

        // Remove answers antigas e adiciona novas
        question.getAnswers().clear();
        question.getAnswers().addAll(questionDTO.answers().stream()
                .map(answerDTO -> answerDTO.toAnswer(question))
                .toList());

        Question updatedQuestion = this.questionRepository.save(question);

        // Retorna o DTO atualizado
        return new QuestionDTO(
                updatedQuestion.getId(),
                updatedQuestion.getContent(),
                updatedQuestion.getAnswers().stream()
                        .map(answer -> new AnswerDTO(
                                answer.getId(),
                                answer.getContent(),
                                answer.getIsCorrect()
                        ))
                        .toList()
        );
    }

    @Transactional
    public void deleteQuestionById(Long id) {
        this.questionRepository.deleteById(id);
    }
}