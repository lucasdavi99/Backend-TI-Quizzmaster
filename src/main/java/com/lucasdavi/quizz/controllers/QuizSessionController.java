package com.lucasdavi.quizz.controllers;

import com.lucasdavi.quizz.dtos.AnswerQuestionDTO;
import com.lucasdavi.quizz.dtos.QuizSessionResultDTO;
import com.lucasdavi.quizz.dtos.QuizSessionStateDTO;
import com.lucasdavi.quizz.dtos.StartQuizSessionDTO;
import com.lucasdavi.quizz.services.QuizSessionService;
import com.lucasdavi.quizz.services.ScheduledCleanupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz-session")
public class QuizSessionController {

    @Autowired
    private QuizSessionService quizSessionService;

    @Autowired
    private ScheduledCleanupService scheduledCleanupService;

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


    @DeleteMapping("/cleanup/abandoned")
    public ResponseEntity<Map<String, Object>> cleanupAbandonedSessions() {
        try {
            int deletedCount = quizSessionService.cleanupAbandonedSessions();

            Map<String, Object> response = Map.of(
                    "message", "Sessões abandonadas removidas com sucesso",
                    "deletedSessions", deletedCount
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erro ao limpar sessões abandonadas",
                    "details", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/cleanup/zero-score")
    public ResponseEntity<Map<String, Object>> cleanupZeroScoreSessions(
            @RequestParam(value = "hours", defaultValue = "24") int hours) {
        try {
            if (hours < 1) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "O número de horas deve ser maior que 0"
                ));
            }

            int deletedCount = scheduledCleanupService.cleanupZeroScoreSessionsOlderThanHours(hours);

            Map<String, Object> response = Map.of(
                    "message", String.format("Sessões com score zero há mais de %d hora(s) removidas", hours),
                    "deletedSessions", deletedCount,
                    "hoursFilter", hours,
                    "type", "zero_score_cleanup"
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erro ao limpar sessões com score zero",
                    "details", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/cleanup/all-zero-score")
    public ResponseEntity<Map<String, Object>> cleanupAllZeroScoreSessions() {
        try {
            int deletedCount = scheduledCleanupService.cleanupAllZeroScoreSessions();

            Map<String, Object> response = Map.of(
                    "message", "Todas as sessões com score zero foram removidas",
                    "deletedSessions", deletedCount,
                    "type", "aggressive_zero_score_cleanup",
                    "warning", "Esta operação remove TODAS as sessões com score 0"
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erro ao limpar todas as sessões com score zero",
                    "details", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/cleanup/active-zero-score")
    public ResponseEntity<Map<String, Object>> cleanupActiveZeroScoreSessions() {
        try {
            int deletedCount = scheduledCleanupService.cleanupActiveZeroScoreSessions();

            Map<String, Object> response = Map.of(
                    "message", "Sessões ativas com score zero removidas",
                    "deletedSessions", deletedCount,
                    "type", "active_zero_score_cleanup",
                    "description", "Remove apenas sessões ativas abandonadas sem afetar histórico"
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erro ao limpar sessões ativas com score zero",
                    "details", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/cleanup/intelligent-zero-score")
    public ResponseEntity<Map<String, Object>> intelligentZeroScoreCleanup() {
        try {
            int deletedCount = scheduledCleanupService.intelligentZeroScoreCleanup();

            Map<String, Object> response = Map.of(
                    "message", "Limpeza inteligente de score zero executada",
                    "deletedSessions", deletedCount,
                    "type", "intelligent_zero_score_cleanup",
                    "criteria", Map.of(
                            "abandonedSessions", "Ativas há mais de 2 horas com score 0",
                            "finishedSessions", "Finalizadas há mais de 24 horas com score 0"
                    )
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erro na limpeza inteligente de score zero",
                    "details", e.getMessage()
            ));
        }
    }

    @GetMapping("/report/zero-score")
    public ResponseEntity<Map<String, Object>> getZeroScoreReport() {
        try {
            Map<String, Long> report = scheduledCleanupService.getZeroScoreSessionsReport();

            Map<String, Object> response = Map.of(
                    "message", "Relatório de sessões com score zero",
                    "data", report,
                    "summary", Map.of(
                            "totalZeroScoreSessions", report.get("totalZeroScoreSessions"),
                            "needsCleanup", report.get("totalZeroScoreSessions") > 50,
                            "recommendedAction", report.get("activeZeroScoreSessions") > 10 ?
                                    "Considere executar limpeza de sessões ativas" : "Sistema limpo"
                    )
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erro ao gerar relatório de score zero",
                    "details", e.getMessage()
            ));
        }
    }

@DeleteMapping("/cleanup/old")
public ResponseEntity<Map<String, Object>> cleanupOldAbandonedSessions(@RequestParam(value = "days", defaultValue = "7") int days) {
    try {
        if (days < 1) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "O número de dias deve ser maior que 0"
            ));
        }

        int deletedCount = quizSessionService.cleanupOldAbandonedSessions(days);

        Map<String, Object> response = Map.of(
                "message", String.format("Sessões abandonadas há mais de %d dia(s) removidas", days),
                "deletedSessions", deletedCount,
                "daysFilter", days
        );

        return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Erro ao limpar sessões antigas",
                "details", e.getMessage()
        ));
    }
}


@PutMapping("/finish-all")
public ResponseEntity<Map<String, Object>> finishAllActiveSessions() {
    try {
        int finishedCount = quizSessionService.finishAllActiveSessions();

        Map<String, Object> response = Map.of(
                "message", "Sessões ativas finalizadas com sucesso",
                "finishedSessions", finishedCount
        );

        return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Erro ao finalizar sessões ativas",
                "details", e.getMessage()
        ));
    }
}
}
