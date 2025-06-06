package com.lucasdavi.quizz.controllers;

import com.lucasdavi.quizz.dtos.RankingDTO;
import com.lucasdavi.quizz.dtos.UserStatsDTO;
import com.lucasdavi.quizz.models.Score;
import com.lucasdavi.quizz.services.ScoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    @Autowired
    ScoreService scoreService;

    // ========================================
    // ENDPOINTS ADMIN (requerem autenticação de ADMIN)
    // ========================================

    @PostMapping
    public ResponseEntity<Score> createScore(@Valid @RequestBody Score score) {
        try {
            Score newScore = scoreService.saveScore(score);
            return ResponseEntity.status(HttpStatus.CREATED).body(newScore);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Score>> getAllScores() {
        try {
            List<Score> scores = scoreService.getAllScores();
            return ResponseEntity.ok(scores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Score> updateScore(@PathVariable Long id, @RequestBody Score score) {
        try {
            Score updatedScore = scoreService.updateScoreById(id, score);
            if (updatedScore != null) {
                return ResponseEntity.ok(updatedScore);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ========================================
    // ENDPOINTS PÚBLICOS (não requerem autenticação)
    // ========================================

    /**
     * Endpoint público para ranking ordenado
     * Acessível por qualquer usuário (autenticado ou não)
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<RankingDTO>> getRanking(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            // Validação de entrada
            if (limit <= 0 || limit > 1000) {
                limit = 50; // Valor padrão seguro
            }

            List<RankingDTO> ranking = scoreService.getTopPlayersRanking(limit);

            System.out.println("📊 [PUBLIC] Ranking solicitado - Limite: " + limit +
                    ", Resultados: " + ranking.size());

            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Erro ao buscar ranking: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint público para estatísticas gerais
     * Acessível por qualquer usuário (autenticado ou não)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGlobalStats() {
        try {
            Map<String, Object> stats = scoreService.getGlobalStats();

            System.out.println("📈 [PUBLIC] Estatísticas globais solicitadas - " +
                    "Total de jogadores: " + stats.get("totalPlayers"));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Erro ao buscar estatísticas globais: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint público para top scores por usuário
     * Acessível por qualquer usuário (autenticado ou não)
     */
    @GetMapping("/top-by-user")
    public ResponseEntity<List<RankingDTO>> getTopScoresByUser() {
        try {
            List<RankingDTO> topScores = scoreService.getTopScoresByUser();

            System.out.println("🎯 [PUBLIC] Top scores por usuário solicitados - " +
                    "Resultados: " + topScores.size());

            return ResponseEntity.ok(topScores);
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Erro ao buscar top scores: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint público para melhores scores
     * Acessível por qualquer usuário (autenticado ou não)
     */
    @GetMapping("/best-scores")
    public ResponseEntity<List<RankingDTO>> getBestScores(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            // Validação de entrada
            if (limit <= 0 || limit > 100) {
                limit = 20; // Valor padrão seguro
            }

            List<RankingDTO> bestScores = scoreService.getBestScoresByUser(limit);

            System.out.println("🏅 [PUBLIC] Melhores scores solicitados - Limite: " + limit +
                    ", Resultados: " + bestScores.size());

            return ResponseEntity.ok(bestScores);
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Erro ao buscar melhores scores: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint público para ranking com paginação
     * Retorna informações extras se o usuário estiver autenticado
     */
    @GetMapping("/ranking/paginated")
    public ResponseEntity<Map<String, Object>> getPaginatedRanking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // Validação de entrada
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 10;

            Map<String, Object> paginatedRanking = scoreService.getPaginatedRanking(page, size);

            System.out.println("📈 [PUBLIC] Ranking paginado solicitado - Página: " + page +
                    ", Tamanho: " + size);

            return ResponseEntity.ok(paginatedRanking);
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Erro ao buscar ranking paginado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================
    // ENDPOINTS PRIVADOS (requerem autenticação)
    // ========================================

    /**
     * Endpoint privado para estatísticas do usuário atual
     * Requer autenticação
     */
    @GetMapping("/my-stats")
    public ResponseEntity<UserStatsDTO> getMyStats() {
        try {
            UserStatsDTO userStats = scoreService.getCurrentUserStats();

            System.out.println("👤 [PRIVATE] Estatísticas do usuário solicitadas - " +
                    "User: " + userStats.username());

            return ResponseEntity.ok(userStats);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated")) {
                System.err.println("🔐 [AUTH] Usuário não autenticado tentando acessar estatísticas pessoais");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            System.err.println("❌ [ERROR] Erro ao buscar estatísticas do usuário: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Erro inesperado ao buscar estatísticas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint privado para buscar posição específica de um usuário
     * Requer autenticação
     */
    @GetMapping("/my-position")
    public ResponseEntity<Map<String, Object>> getMyRankingPosition() {
        try {
            Map<String, Object> position = scoreService.getCurrentUserRankingPosition();

            System.out.println("🔍 [PRIVATE] Posição no ranking solicitada - " +
                    "User: " + position.get("username") +
                    ", Posição: " + position.get("position"));

            return ResponseEntity.ok(position);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated")) {
                System.err.println("🔐 [AUTH] Usuário não autenticado tentando acessar posição no ranking");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            System.err.println("❌ [ERROR] Erro ao buscar posição do usuário: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Erro inesperado ao buscar posição: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}