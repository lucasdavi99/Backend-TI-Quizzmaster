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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * Método para buscar todas as perguntas com melhor tratamento de erros
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllQuestions() {
        try {
            System.out.println("🔍 [ADMIN] Buscando todas as perguntas...");

            List<QuestionDTO> questions = questionService.getAllQuestionsDTO();

            System.out.println("✅ [ADMIN] " + questions.size() + " pergunta(s) encontrada(s)");

            return ResponseEntity.ok(questions);

        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar perguntas: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Erro interno do servidor",
                            "message", "Não foi possível carregar as perguntas",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Método para buscar pergunta por ID com melhor tratamento de erros
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getQuestionById(@PathVariable Long id) {
        try {
            System.out.println("🔍 [ADMIN] Buscando pergunta ID: " + id);

            Optional<QuestionDTO> question = questionService.getQuestionDTOById(id);

            if (question.isPresent()) {
                System.out.println("✅ [ADMIN] Pergunta encontrada: " + question.get().content().substring(0, Math.min(50, question.get().content().length())) + "...");
                return ResponseEntity.ok(question.get());
            } else {
                System.out.println("⚠️ [ADMIN] Pergunta não encontrada: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Pergunta não encontrada",
                                "message", "Não existe pergunta com o ID " + id
                        ));
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar pergunta " + id + ": " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Erro interno do servidor",
                            "message", "Não foi possível carregar a pergunta",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Método para criar pergunta com melhor validação e tratamento de erros
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createQuestion(@Valid @RequestBody QuestionDTO questionDTO) {
        try {
            System.out.println("📝 [ADMIN] Criando nova pergunta...");

            // Validações adicionais
            if (questionDTO.content() == null || questionDTO.content().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Conteúdo da pergunta é obrigatório",
                                "message", "Informe o conteúdo da pergunta"
                        ));
            }

            if (questionDTO.answers() == null || questionDTO.answers().size() < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Respostas insuficientes",
                                "message", "A pergunta deve ter pelo menos 2 respostas"
                        ));
            }

            if (questionDTO.answers().size() > 6) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Muitas respostas",
                                "message", "A pergunta pode ter no máximo 6 respostas"
                        ));
            }

            boolean hasCorrectAnswer = questionDTO.answers().stream()
                    .anyMatch(answer -> answer.isCorrect() != null && answer.isCorrect());

            if (!hasCorrectAnswer) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Resposta correta obrigatória",
                                "message", "Pelo menos uma resposta deve ser marcada como correta"
                        ));
            }

            Question savedQuestion = questionService.saveQuestion(questionDTO);

            System.out.println("✅ [ADMIN] Pergunta criada com ID: " + savedQuestion.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Pergunta criada com sucesso",
                            "questionId", savedQuestion.getId(),
                            "success", true
                    ));

        } catch (Exception e) {
            System.err.println("❌ Erro ao criar pergunta: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Erro interno do servidor",
                            "message", "Não foi possível criar a pergunta",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Método para atualizar pergunta com melhor tratamento de erros
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionDTO questionDTO) {
        try {
            System.out.println("📝 [ADMIN] Atualizando pergunta ID: " + id);

            // Validações similares ao create
            if (questionDTO.content() == null || questionDTO.content().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Conteúdo da pergunta é obrigatório",
                                "message", "Informe o conteúdo da pergunta"
                        ));
            }

            if (questionDTO.answers() == null || questionDTO.answers().size() < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Respostas insuficientes",
                                "message", "A pergunta deve ter pelo menos 2 respostas"
                        ));
            }

            boolean hasCorrectAnswer = questionDTO.answers().stream()
                    .anyMatch(answer -> answer.isCorrect() != null && answer.isCorrect());

            if (!hasCorrectAnswer) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Resposta correta obrigatória",
                                "message", "Pelo menos uma resposta deve ser marcada como correta"
                        ));
            }

            QuestionDTO updatedQuestion = questionService.updateQuestionById(id, questionDTO);

            if (updatedQuestion != null) {
                System.out.println("✅ [ADMIN] Pergunta " + id + " atualizada com sucesso");

                return ResponseEntity.ok(Map.of(
                        "message", "Pergunta atualizada com sucesso",
                        "question", updatedQuestion,
                        "success", true
                ));
            } else {
                System.out.println("⚠️ [ADMIN] Pergunta " + id + " não encontrada para atualização");

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Pergunta não encontrada",
                                "message", "Não existe pergunta com o ID " + id
                        ));
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao atualizar pergunta " + id + ": " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Erro interno do servidor",
                            "message", "Não foi possível atualizar a pergunta",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Método para deletar pergunta com melhor tratamento de erros
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        try {
            System.out.println("🗑️ [ADMIN] Deletando pergunta ID: " + id);

            // Verifica se a pergunta existe antes de deletar
            Optional<QuestionDTO> existingQuestion = questionService.getQuestionDTOById(id);

            if (existingQuestion.isEmpty()) {
                System.out.println("⚠️ [ADMIN] Pergunta " + id + " não encontrada para deleção");

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Pergunta não encontrada",
                                "message", "Não existe pergunta com o ID " + id
                        ));
            }

            questionService.deleteQuestionById(id);

            System.out.println("✅ [ADMIN] Pergunta " + id + " deletada com sucesso");

            return ResponseEntity.ok(Map.of(
                    "message", "Pergunta deletada com sucesso",
                    "deletedQuestionId", id,
                    "success", true
            ));

        } catch (Exception e) {
            System.err.println("❌ Erro ao deletar pergunta " + id + ": " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Erro interno do servidor",
                            "message", "Não foi possível deletar a pergunta",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Endpoint para obter estatísticas das perguntas
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getQuestionsStats() {
        try {
            List<QuestionDTO> allQuestions = questionService.getAllQuestionsDTO();

            long totalQuestions = allQuestions.size();
            long questionsWithCorrectAnswers = allQuestions.stream()
                    .filter(q -> q.answers().stream().anyMatch(a -> a.isCorrect() != null && a.isCorrect()))
                    .count();

            Map<String, Object> stats = Map.of(
                    "totalQuestions", totalQuestions,
                    "questionsWithCorrectAnswers", questionsWithCorrectAnswers,
                    "questionsWithoutCorrectAnswers", totalQuestions - questionsWithCorrectAnswers,
                    "averageAnswersPerQuestion", allQuestions.stream()
                            .mapToInt(q -> q.answers().size())
                            .average()
                            .orElse(0.0)
            );

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            System.err.println("❌ Erro ao obter estatísticas das perguntas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro ao obter estatísticas"));
        }
    }
}