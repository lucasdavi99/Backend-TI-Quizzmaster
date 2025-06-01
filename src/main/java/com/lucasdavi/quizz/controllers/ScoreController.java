package com.lucasdavi.quizz.controllers;

import com.lucasdavi.quizz.dtos.RankingDTO;
import com.lucasdavi.quizz.dtos.UserStatsDTO;
import com.lucasdavi.quizz.models.Score;
import com.lucasdavi.quizz.services.ScoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    @Autowired
    ScoreService scoreService;

    @PostMapping
    public ResponseEntity<Score> createScore(@Valid @RequestBody Score score) {
        Score newScore = this.scoreService.saveScore(score);
        return ResponseEntity.ok().body(newScore);
    }

    @GetMapping
    public ResponseEntity<List<Score>> getAllScores() {
        return ResponseEntity.ok(scoreService.getAllScores());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Score> updateScore(@PathVariable Long id, @RequestBody Score score) {
        return ResponseEntity.ok(scoreService.updateScoreById(id, score));
    }

    // 🏆 NOVO: Endpoint para ranking ordenado
    @GetMapping("/ranking")
    public ResponseEntity<List<RankingDTO>> getRanking(@RequestParam(defaultValue = "50") int limit) {
        try {
            List<RankingDTO> ranking = scoreService.getTopPlayersRanking(limit);
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 📊 NOVO: Endpoint para estatísticas gerais
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGlobalStats() {
        try {
            Map<String, Object> stats = scoreService.getGlobalStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 👤 NOVO: Endpoint para estatísticas do usuário atual
    @GetMapping("/my-stats")
    public ResponseEntity<UserStatsDTO> getMyStats() {
        try {
            UserStatsDTO userStats = scoreService.getCurrentUserStats();
            return ResponseEntity.ok(userStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 🎯 NOVO: Endpoint para top scores por usuário
    @GetMapping("/top-by-user")
    public ResponseEntity<List<RankingDTO>> getTopScoresByUser() {
        try {
            List<RankingDTO> topScores = scoreService.getTopScoresByUser();
            return ResponseEntity.ok(topScores);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 📈 NOVO: Endpoint para ranking com paginação
    @GetMapping("/ranking/paginated")
    public ResponseEntity<Map<String, Object>> getPaginatedRanking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> paginatedRanking = scoreService.getPaginatedRanking(page, size);
            return ResponseEntity.ok(paginatedRanking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 🔍 NOVO: Endpoint para buscar posição específica de um usuário
    @GetMapping("/my-position")
    public ResponseEntity<Map<String, Object>> getMyRankingPosition() {
        try {
            Map<String, Object> position = scoreService.getCurrentUserRankingPosition();
            return ResponseEntity.ok(position);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 🏅 NOVO: Endpoint para melhor score de cada usuário
    @GetMapping("/best-scores")
    public ResponseEntity<List<RankingDTO>> getBestScores(@RequestParam(defaultValue = "20") int limit) {
        try {
            List<RankingDTO> bestScores = scoreService.getBestScoresByUser(limit);
            return ResponseEntity.ok(bestScores);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}