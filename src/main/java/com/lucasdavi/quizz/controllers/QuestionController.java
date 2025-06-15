package com.lucasdavi.quizz.controllers;

import com.lucasdavi.quizz.dtos.QuestionDTO;
import com.lucasdavi.quizz.models.Question;
import com.lucasdavi.quizz.services.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // ✅ USANDO SEU MÉTODO REAL: getAllQuestionsDTO()
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<QuestionDTO>> getAllQuestions() {
        try {
            List<QuestionDTO> questions = questionService.getAllQuestionsDTO();
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar perguntas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ USANDO SEU MÉTODO REAL: getQuestionDTOById()
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<QuestionDTO> getQuestionById(@PathVariable Long id) {
        Optional<QuestionDTO> question = questionService.getQuestionDTOById(id);
        return question.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ USANDO SEU MÉTODO REAL: saveQuestion(QuestionDTO)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody QuestionDTO questionDTO) {
        try {
            Question savedQuestion = questionService.saveQuestion(questionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedQuestion);
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar pergunta: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ✅ USANDO SEU MÉTODO REAL: updateQuestionById()
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<QuestionDTO> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionDTO questionDTO) {
        try {
            QuestionDTO updatedQuestion = questionService.updateQuestionById(id, questionDTO);
            if (updatedQuestion != null) {
                return ResponseEntity.ok(updatedQuestion);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao atualizar pergunta: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ✅ USANDO SEU MÉTODO REAL: deleteQuestionById()
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        try {
            // Verifica se a pergunta existe antes de deletar
            Optional<QuestionDTO> existingQuestion = questionService.getQuestionDTOById(id);
            if (existingQuestion.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            questionService.deleteQuestionById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("❌ Erro ao deletar pergunta: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}