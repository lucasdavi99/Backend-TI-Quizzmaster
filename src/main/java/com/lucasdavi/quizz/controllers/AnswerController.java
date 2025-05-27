package com.lucasdavi.quizz.controllers;

import com.lucasdavi.quizz.dtos.AnswerDTO;
import com.lucasdavi.quizz.models.Answer;
import com.lucasdavi.quizz.services.AnswerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @PostMapping
    public ResponseEntity<AnswerDTO> createAnswer(@Valid @RequestBody AnswerDTO answerDTO) {
        Answer answer = answerService.saveAnswer(answerDTO);
        AnswerDTO savedAnswerDTO = new AnswerDTO(
                answer.getId(),
                answer.getContent(),
                answer.getIsCorrect()
        );
        return ResponseEntity.status(201).body(savedAnswerDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnswerDTO> getAnswerById(@PathVariable Long id) {
        return answerService.getAnswerDTOById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AnswerDTO>> getAllAnswers() {
        List<AnswerDTO> answerDTOs = answerService.getAllAnswersDTO();
        return ResponseEntity.ok(answerDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnswerDTO> updateAnswer(@PathVariable Long id, @Valid @RequestBody AnswerDTO answerDTO) {
        AnswerDTO updatedAnswer = answerService.updateAnswerById(id, answerDTO);
        if (updatedAnswer != null) {
            return ResponseEntity.ok(updatedAnswer);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id) {
        this.answerService.deleteAnswerById(id);
        return ResponseEntity.ok().build();
    }
}
