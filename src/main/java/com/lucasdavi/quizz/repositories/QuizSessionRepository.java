package com.lucasdavi.quizz.repositories;

import com.lucasdavi.quizz.models.QuizSession;
import com.lucasdavi.quizz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    List<QuizSession> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = true")
    Optional<QuizSession> findActiveSessionByUser(@Param("user") User user);

    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = false ORDER BY qs.score DESC")
    List<QuizSession> findCompletedSessionsByUserOrderByScoreDesc(@Param("user") User user);
}
