package com.lucasdavi.quizz.repositories;

import com.lucasdavi.quizz.models.QuizSession;
import com.lucasdavi.quizz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    List<QuizSession> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = true")
    Optional<QuizSession> findActiveSessionByUser(@Param("user") User user);

    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = false ORDER BY qs.score DESC")
    List<QuizSession> findCompletedSessionsByUserOrderByScoreDesc(@Param("user") User user);

    // NOVOS MÉTODOS para limpeza de sessões abandonadas

    /**
     * Busca todas as sessões ativas de um usuário (útil para limpeza geral)
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = true")
    List<QuizSession> findActiveSessionsByUser(@Param("user") User user);

    /**
     * Busca sessões ativas criadas antes de uma data específica
     * Útil para limpar sessões abandonadas há muito tempo
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = true AND qs.createdAt < :cutoffDate")
    List<QuizSession> findActiveSessionsOlderThan(@Param("user") User user, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Busca sessões que estão ativas há mais de X horas
     * Útil para identificar sessões realmente abandonadas
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = true AND qs.createdAt < :hoursAgo")
    List<QuizSession> findActiveSessionsOlderThanHours(@Param("user") User user, @Param("hoursAgo") LocalDateTime hoursAgo);

    /**
     * Conta quantas sessões ativas um usuário possui
     */
    @Query("SELECT COUNT(qs) FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = true")
    long countActiveSessionsByUser(@Param("user") User user);

    /**
     * Busca sessões incompletas (ativas ou não finalizadas)
     * que não foram tocadas há mais de X tempo
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user " +
            "AND (qs.isActive = true OR qs.finishedAt IS NULL) " +
            "AND qs.createdAt < :cutoffDate")
    List<QuizSession> findIncompleteSessionsOlderThan(@Param("user") User user, @Param("cutoffDate") LocalDateTime cutoffDate);

    // MÉTODOS para o ScheduledCleanupService

    /**
     * Busca todas as sessões ativas criadas antes de uma data (para limpeza automática)
     */
    List<QuizSession> findByIsActiveTrueAndCreatedAtBefore(LocalDateTime cutoffDate);

    /**
     * Conta o total de sessões ativas no sistema (para monitoramento)
     */
    long countByIsActiveTrue();

    /**
     * Busca sessões ativas de todos os usuários criadas antes de uma data
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.isActive = true AND qs.createdAt < :cutoffDate")
    List<QuizSession> findAllActiveSessionsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    // MÉTODOS para limpeza de sessões com SCORE ZERO

    /**
     * Busca sessões por score específico
     */
    List<QuizSession> findByScore(Integer score);

    /**
     * Busca sessões finalizadas com score específico criadas antes de uma data
     */
    List<QuizSession> findByScoreAndIsActiveFalseAndCreatedAtBefore(Integer score, LocalDateTime cutoffDate);

    /**
     * Busca sessões ativas com score específico
     */
    List<QuizSession> findByScoreAndIsActiveTrue(Integer score);

    /**
     * Busca sessões ativas com score específico criadas antes de uma data
     */
    List<QuizSession> findByScoreAndIsActiveTrueAndCreatedAtBefore(Integer score, LocalDateTime cutoffDate);

    /**
     * Busca sessões com score específico criadas antes de uma data (independente do status)
     */
    List<QuizSession> findByScoreAndCreatedAtBefore(Integer score, LocalDateTime cutoffDate);

    /**
     * Conta sessões por score e status ativo
     */
    long countByScoreAndIsActiveTrue(Integer score);

    /**
     * Conta sessões por score e status inativo
     */
    long countByScoreAndIsActiveFalse(Integer score);

    /**
     * Conta sessões por score (qualquer status)
     */
    long countByScore(Integer score);
}