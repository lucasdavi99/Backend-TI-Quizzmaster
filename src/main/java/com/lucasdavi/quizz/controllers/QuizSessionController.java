package com.lucasdavi.quizz.controllers;

import com.lucasdavi.quizz.dtos.AnswerQuestionDTO;
import com.lucasdavi.quizz.dtos.QuizSessionResultDTO;
import com.lucasdavi.quizz.dtos.QuizSessionStateDTO;
import com.lucasdavi.quizz.dtos.StartQuizSessionDTO;
import com.lucasdavi.quizz.services.QuizSessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-session")
public class QuizSessionController {

    @Autowired
    private QuizSessionService quizSessionService;

    @PostMapping("/start")
    public ResponseEntity<QuizSessionStateDTO> startQuizSession(@Valid @RequestBody StartQuizSessionDTO dto) {
        try {
            QuizSessionStateDTO sessionState = quizSessionService.startNewSession(dto);
            return ResponseEntity.ok(sessionState);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<?> answerQuestion(@PathVariable Long sessionId, @Valid @RequestBody AnswerQuestionDTO dto) {
        try {
            System.out.println("🔍 CONTROLLER - Chamando answerQuestion");

            QuizSessionResultDTO result = quizSessionService.answerQuestion(sessionId, dto);

            System.out.println("🔍 CONTROLLER - Service retornou: " + (result != null ? "Result DTO" : "NULL"));

            if (result != null) {
                System.out.println("🔍 CONTROLLER - Retornando resultado final");
                return ResponseEntity.ok(result);
            } else {
                System.out.println("🔍 CONTROLLER - Buscando próximo estado...");

                QuizSessionStateDTO nextState = quizSessionService.getSessionState(sessionId);

                System.out.println("🔍 CONTROLLER - Estado obtido com sucesso!");
                return ResponseEntity.ok(nextState);
            }
        } catch (Exception e) {
            System.err.println("❌ CONTROLLER ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<QuizSessionStateDTO> getSessionState(@PathVariable Long sessionId) {
        try {
            QuizSessionStateDTO sessionState = quizSessionService.getSessionState(sessionId);
            return ResponseEntity.ok(sessionState);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<QuizSessionResultDTO>> getUserQuizHistory() {
        try {
            List<QuizSessionResultDTO> history = quizSessionService.getUserQuizHistory();
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
