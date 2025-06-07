package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.dtos.UserProfileDTO;
import com.lucasdavi.quizz.models.User;
import com.lucasdavi.quizz.repositories.UserRepository;
import com.lucasdavi.quizz.repositories.ScoreRepository;
import com.lucasdavi.quizz.repositories.QuizSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private QuizSessionRepository quizSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtém o usuário atualmente autenticado
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            throw new RuntimeException("User not authenticated");
        }

        return (User) principal;
    }

    /**
     * Busca perfil do usuário atual
     */
    public UserProfileDTO getCurrentUserProfile() {
        User currentUser = getCurrentUser();

        return new UserProfileDTO(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getEmail(),
                currentUser.getCreatedAt(),
                "active"
        );
    }

    /**
     * Atualiza o username do usuário atual
     */
    @Transactional
    public UserProfileDTO updateUsername(String newUsername) {
        User currentUser = getCurrentUser();

        // Validação de formato do username
        if (!isValidUsername(newUsername)) {
            throw new RuntimeException("Username invalid format");
        }

        // Verifica se o username não é o mesmo atual
        if (currentUser.getUsername().equals(newUsername)) {
            throw new RuntimeException("New username is the same as current");
        }

        // Verifica se o username já existe
        if (userRepository.existsByUsername(newUsername)) {
            throw new RuntimeException("Username already exists");
        }

        // Atualiza o username
        currentUser.setUsername(newUsername);
        User updatedUser = userRepository.save(currentUser);

        return new UserProfileDTO(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getCreatedAt(),
                "active"
        );
    }

    /**
     * Altera a senha do usuário atual
     */
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        User currentUser = getCurrentUser();

        // Verifica se a senha atual está correta
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validação da nova senha
        if (newPassword.length() < 6) {
            throw new RuntimeException("New password is too short");
        }

        // Verifica se a nova senha não é igual à atual
        if (passwordEncoder.matches(newPassword, currentUser.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        // Criptografa e salva a nova senha
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        currentUser.setPassword(encodedNewPassword);
        userRepository.save(currentUser);
    }

    /**
     * Exclui a conta do usuário atual
     */
    @Transactional
    public void deleteAccount(String currentPassword) {
        User currentUser = getCurrentUser();

        // Verifica se a senha está correta
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Remove todos os dados relacionados ao usuário
        // Os scores e sessões serão removidos automaticamente devido ao CASCADE
        userRepository.delete(currentUser);
    }

    /**
     * Busca estatísticas detalhadas do usuário
     */
    public Map<String, Object> getUserDetailedStats() {
        User currentUser = getCurrentUser();

        Map<String, Object> stats = new HashMap<>();

        // Estatísticas básicas de games
        long totalGames = scoreRepository.countByUser(currentUser);
        Integer bestScore = scoreRepository.findTopScoreByUser(currentUser)
                .stream()
                .findFirst()
                .map(score -> score.getPoints())
                .orElse(0);

        Double averageScore = scoreRepository.getAverageScoreByUser(currentUser);
        if (averageScore == null) averageScore = 0.0;

        Long totalPoints = scoreRepository.getTotalPointsByUser(currentUser);
        if (totalPoints == null) totalPoints = 0L;

        // Estatísticas de sessões
        long activeSessions = quizSessionRepository.countActiveSessionsByUser(currentUser);

        // Calcula posição no ranking
        long rankingPosition = calculateUserRankingPosition(currentUser);

        stats.put("totalGames", totalGames);
        stats.put("bestScore", bestScore);
        stats.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        stats.put("totalPoints", totalPoints);
        stats.put("rankingPosition", rankingPosition);
        stats.put("activeSessions", activeSessions);
        stats.put("username", currentUser.getUsername());
        stats.put("memberSince", currentUser.getCreatedAt());

        return stats;
    }

    /**
     * Valida formato do username
     */
    private boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String trimmed = username.trim();
        return trimmed.length() >= 3 &&
                trimmed.length() <= 20 &&
                trimmed.matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * Calcula posição no ranking (simplificado)
     */
    private long calculateUserRankingPosition(User user) {
        try {
            Integer userBestScore = scoreRepository.findTopScoreByUser(user)
                    .stream()
                    .findFirst()
                    .map(score -> score.getPoints())
                    .orElse(0);

            // Conta quantos usuários têm score melhor
            long betterScores = scoreRepository.findAll()
                    .stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            score -> score.getUser(),
                            java.util.stream.Collectors.maxBy(
                                    java.util.Comparator.comparing(score -> score.getPoints())
                            )
                    ))
                    .values()
                    .stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .mapToInt(score -> score.getPoints())
                    .filter(score -> score > userBestScore)
                    .count();

            return betterScores + 1;
        } catch (Exception e) {
            return 0; // Retorna 0 se houver erro
        }
    }
}