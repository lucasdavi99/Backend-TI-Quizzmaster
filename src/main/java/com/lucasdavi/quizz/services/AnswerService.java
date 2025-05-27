package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.dtos.AnswerDTO;
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

    public Answer saveAnswer(AnswerDTO answerDTO) {
        Answer answer = new Answer();
        answer.setContent(answerDTO.content());
        answer.setIsCorrect(answerDTO.isCorrect());

        return this.answerRepository.save(answer);
    }

    public Optional<AnswerDTO> getAnswerDTOById(Long id) {
        return this.answerRepository.findById(id)
                .map(answer -> new AnswerDTO(
                        answer.getId(),
                        answer.getContent(),
                        answer.getIsCorrect()
                ));
    }

    public List<AnswerDTO> getAllAnswersDTO() {
        return this.answerRepository.findAll().stream()
                .map(answer -> new AnswerDTO(
                        answer.getId(),
                        answer.getContent(),
                        answer.getIsCorrect()
                ))
                .toList();
    }

    public AnswerDTO updateAnswerById(Long id, AnswerDTO answerDTO) {
        Optional<Answer> answerData = this.answerRepository.findById(id);
        if (answerData.isPresent()) {
            Answer answer = answerData.get();
            answer.setContent(answerDTO.content());
            answer.setIsCorrect(answerDTO.isCorrect());

            Answer updatedAnswer = this.answerRepository.save(answer);

            return new AnswerDTO(
                    updatedAnswer.getId(),
                    updatedAnswer.getContent(),
                    updatedAnswer.getIsCorrect()
            );
        } else {
            return null;
        }
    }

    public void deleteAnswerById(Long id) {
        this.answerRepository.deleteById(id);
    }
}
