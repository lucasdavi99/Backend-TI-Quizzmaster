package com.lucasdavi.quizz.controllers;

import com.lucasdavi.quizz.models.Answer;
import com.lucasdavi.quizz.models.Score;
import com.lucasdavi.quizz.services.ScoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {
    @Autowired
    ScoreService scoreService;

    @PostMapping
    public ResponseEntity<Score> createScore(@Valid @RequestBody Score score, @PathVariable Long answerId) {
        Score newScore = this.scoreService.saveScore(score, answerId);
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
}
