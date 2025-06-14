package com.lucasdavi.quizz.repositories;

import com.lucasdavi.quizz.models.PasswordResetToken;
import com.lucasdavi.quizz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndIsActiveTrue(String token);

    Optional<PasswordResetToken> findByEmailAndIsActiveTrue(String email);

    List<PasswordResetToken> findByUserAndIsActiveTrue(User user);

    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken p SET p.isActive = false WHERE p.user = :user")
    void deactivateAllTokensByUser(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :cutoffTime")
    int deleteExpiredTokens(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT COUNT(p) FROM PasswordResetToken p WHERE p.email = :email AND p.createdAt > :since")
    long countRecentTokensByEmail(@Param("email") String email, @Param("since") LocalDateTime since);
}
