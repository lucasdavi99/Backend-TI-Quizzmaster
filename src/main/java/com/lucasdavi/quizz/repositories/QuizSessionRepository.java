package com.lucasdavi.quizz.repositories;

import com.lucasdavi.quizz.enums.SessionStatus;
import com.lucasdavi.quizz.models.QuizSession;
import com.lucasdavi.quizz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {

    // MÉTODOS EXISTENTES (mantidos para compatibilidade)
    List<QuizSession> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = true")
    Optional<QuizSession> findActiveSessionByUser(@Param("user") User user);

    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = false ORDER BY qs.score DESC")
    List<QuizSession> findCompletedSessionsByUserOrderByScoreDesc(@Param("user") User user);

    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = true")
    List<QuizSession> findActiveSessionsByUser(@Param("user") User user);

    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = true AND qs.createdAt < :cutoffDate")
    List<QuizSession> findActiveSessionsOlderThan(@Param("user") User user, @Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(qs) FROM QuizSession qs WHERE qs.user = :user AND qs.isActive = true")
    long countActiveSessionsByUser(@Param("user") User user);

    // 🆕 NOVOS MÉTODOS COM SessionStatus

    /**
     * Busca sessões por usuário e status específico
     */
    List<QuizSession> findByUserAndStatusOrderByCreatedAtDesc(User user, SessionStatus status);

    /**
     * Busca sessões em progresso de um usuário
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.status = 'IN_PROGRESS'")
    List<QuizSession> findInProgressSessionsByUser(@Param("user") User user);

    /**
     * Busca sessões completadas de um usuário ordenadas por score
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.status = 'COMPLETED' ORDER BY qs.score DESC")
    List<QuizSession> findCompletedSessionsByUserOrderByScore(@Param("user") User user);

    /**
     * Busca sessões interrompidas de um usuário
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.status = 'INTERRUPTED' ORDER BY qs.createdAt DESC")
    List<QuizSession> findInterruptedSessionsByUser(@Param("user") User user);

    /**
     * Busca sessões finalizadas (completas ou interrompidas) de um usuário
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.status IN ('COMPLETED', 'INTERRUPTED') ORDER BY qs.createdAt DESC")
    List<QuizSession> findFinishedSessionsByUser(@Param("user") User user);

    /**
     * Conta sessões por status de um usuário
     */
    long countByUserAndStatus(User user, SessionStatus status);

    /**
     * Busca sessões por status criadas antes de uma data
     */
    List<QuizSession> findByStatusAndCreatedAtBefore(SessionStatus status, LocalDateTime cutoffDate);

    /**
     * Busca sessões em progresso antigas (potencialmente abandonadas)
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.status = 'IN_PROGRESS' AND qs.createdAt < :cutoffDate")
    List<QuizSession> findAbandonedInProgressSessions(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Estatísticas: conta total de sessões por status
     */
    long countByStatus(SessionStatus status);

    /**
     * Busca sessões em progresso de um usuário criadas antes de uma data
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.status = 'IN_PROGRESS' AND qs.createdAt < :cutoffDate")
    List<QuizSession> findUserInProgressSessionsOlderThan(@Param("user") User user, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Busca as melhores sessões completadas (por score) globalmente
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.status = 'COMPLETED' ORDER BY qs.score DESC")
    List<QuizSession> findTopCompletedSessionsByScore();

    /**
     * Estatísticas de completude por usuário
     */
    @Query("SELECT COUNT(qs) as total, " +
            "SUM(CASE WHEN qs.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
            "SUM(CASE WHEN qs.status = 'INTERRUPTED' THEN 1 ELSE 0 END) as interrupted, " +
            "SUM(CASE WHEN qs.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgress " +
            "FROM QuizSession qs WHERE qs.user = :user")
    Object[] getUserSessionStats(@Param("user") User user);

    /**
     * Busca sessões com score específico e status específico
     */
    List<QuizSession> findByScoreAndStatus(Integer score, SessionStatus status);

    // MÉTODOS DE LIMPEZA ATUALIZADOS

    /**
     * Busca sessões em progresso criadas antes de uma data (para limpeza automática)
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.status = 'IN_PROGRESS' AND qs.createdAt < :cutoffDate")
    List<QuizSession> findInProgressSessionsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Conta sessões em progresso no sistema
     */
    @Query("SELECT COUNT(qs) FROM QuizSession qs WHERE qs.status = 'IN_PROGRESS'")
    long countInProgressSessions();

    // MÉTODOS DE COMPATIBILIDADE (podem ser removidos no futuro)

    /**
     * @deprecated Use findByStatusAndCreatedAtBefore com SessionStatus.IN_PROGRESS
     */
    @Deprecated
    List<QuizSession> findByIsActiveTrueAndCreatedAtBefore(LocalDateTime cutoffDate);

    /**
     * @deprecated Use countInProgressSessions
     */
    @Deprecated
    long countByIsActiveTrue();

    /**
     * @deprecated Use findInProgressSessionsOlderThan
     */
    @Deprecated
    @Query("SELECT qs FROM QuizSession qs WHERE qs.isActive = true AND qs.createdAt < :cutoffDate")
    List<QuizSession> findAllActiveSessionsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    // MÉTODOS PARA SCORE ZERO (atualizados)

    /**
     * Busca sessões por score específico e status específico
     */
    List<QuizSession> findByScoreAndStatusAndCreatedAtBefore(Integer score, SessionStatus status, LocalDateTime cutoffDate);

    /**
     * Conta sessões por score e status
     */
    long countByScoreAndStatus(Integer score, SessionStatus status);

    /**
     * Busca sessões interrompidas com score zero (para limpeza)
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.score = 0 AND qs.status = 'INTERRUPTED' AND qs.createdAt < :cutoffDate")
    List<QuizSession> findZeroScoreInterruptedSessionsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}