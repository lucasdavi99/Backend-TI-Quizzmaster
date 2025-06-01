package com.lucasdavi.quizz.repositories;

import com.lucasdavi.quizz.models.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    /**
     * Remove todos os registros de Score com pontuação específica
     * Útil para limpar scores zero após remover sessões
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Score s WHERE s.points = :points")
    int deleteByPoints(@Param("points") Integer points);

    /**
     * Conta quantos registros de Score existem com pontuação específica
     */
    long countByPoints(Integer points);

    /**
     * Busca todos os scores com pontuação específica
     */
    List<Score> findByPoints(Integer points);

    /**
     * Busca scores com pontuação específica de um usuário
     */
    @Query("SELECT s FROM Score s WHERE s.points = :points AND s.user.id = :userId")
    List<Score> findByPointsAndUserId(@Param("points") Integer points, @Param("userId") Long userId);

    /**
     * Remove scores zero de um usuário específico
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Score s WHERE s.points = 0 AND s.user.id = :userId")
    int deleteZeroScoresByUserId(@Param("userId") Long userId);

    /**
     * Conta scores por usuário e pontuação
     */
    @Query("SELECT COUNT(s) FROM Score s WHERE s.user.id = :userId AND s.points = :points")
    long countByUserIdAndPoints(@Param("userId") Long userId, @Param("points") Integer points);
}