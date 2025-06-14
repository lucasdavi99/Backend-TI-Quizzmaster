package com.lucasdavi.quizz.repositories;

import com.lucasdavi.quizz.models.Score;
import com.lucasdavi.quizz.models.User;
import com.lucasdavi.quizz.models.User;
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

    // 🆕 NOVOS MÉTODOS PARA RANKING

    /**
     * Busca todos os scores de um usuário específico
     */
    List<Score> findByUser(User user);

    /**
     * Busca todos os scores de um usuário específico ordenados por pontuação descendente
     */
    List<Score> findByUserOrderByPointsDesc(User user);

    /**
     * Busca o melhor score de um usuário
     */
    @Query("SELECT s FROM Score s WHERE s.user = :user ORDER BY s.points DESC")
    List<Score> findTopScoreByUser(@Param("user") User user);

    /**
     * Busca scores ordenados por pontuação descendente (para ranking global)
     */
    List<Score> findAllByOrderByPointsDesc();

    /**
     * Busca os N melhores scores globalmente
     */
    @Query("SELECT s FROM Score s ORDER BY s.points DESC")
    List<Score> findTopScores();

    /**
     * Conta total de scores de um usuário
     */
    long countByUser(User user);

    /**
     * Busca scores de usuários específicos
     */
    @Query("SELECT s FROM Score s WHERE s.user.id IN :userIds")
    List<Score> findByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * Calcula média de pontuação de um usuário
     */
    @Query("SELECT AVG(s.points) FROM Score s WHERE s.user = :user")
    Double getAverageScoreByUser(@Param("user") User user);

    /**
     * Calcula soma total de pontos de um usuário
     */
    @Query("SELECT SUM(s.points) FROM Score s WHERE s.user = :user")
    Long getTotalPointsByUser(@Param("user") User user);

    /**
     * Busca melhor score de cada usuário (para ranking)
     */
    @Query("SELECT s FROM Score s WHERE s.points = " +
            "(SELECT MAX(s2.points) FROM Score s2 WHERE s2.user = s.user) " +
            "GROUP BY s.user ORDER BY s.points DESC")
    List<Score> findBestScorePerUser();

    /**
     * Estatísticas globais - maior pontuação
     */
    @Query("SELECT MAX(s.points) FROM Score s")
    Integer findMaxScore();

    /**
     * Estatísticas globais - média de pontuação
     */
    @Query("SELECT AVG(s.points) FROM Score s")
    Double findAverageScore();

    /**
     * Estatísticas globais - total de pontos
     */
    @Query("SELECT SUM(s.points) FROM Score s")
    Long findTotalPoints();

    /**
     * Conta usuários únicos que têm scores
     */
    @Query("SELECT COUNT(DISTINCT s.user) FROM Score s")
    Long countDistinctUsers();

    /**
     * Busca usuário com maior pontuação
     */
    @Query("SELECT s.user FROM Score s WHERE s.points = (SELECT MAX(s2.points) FROM Score s2)")
    List<User> findUsersWithHighestScore();

    /**
     * Busca scores maiores que zero (jogos válidos)
     */
    @Query("SELECT s FROM Score s WHERE s.points > 0")
    List<Score> findValidScores();

    /**
     * Conta scores válidos (maior que zero) de um usuário
     */
    @Query("SELECT COUNT(s) FROM Score s WHERE s.user = :user AND s.points > 0")
    long countValidScoresByUser(@Param("user") User user);
}