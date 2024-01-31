package com.lucasdavi.quizz.repositories;

import com.lucasdavi.quizz.models.Score;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository extends JpaRepository<Score, Long> {
}
