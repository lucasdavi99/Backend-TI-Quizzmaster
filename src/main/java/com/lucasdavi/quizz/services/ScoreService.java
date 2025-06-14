package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.dtos.RankingDTO;
import com.lucasdavi.quizz.dtos.UserStatsDTO;
import com.lucasdavi.quizz.models.Score;
import com.lucasdavi.quizz.models.User;
import com.lucasdavi.quizz.repositories.ScoreRepository;
import com.lucasdavi.quizz.repositories.QuizSessionRepository;
import com.lucasdavi.quizz.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private QuizSessionRepository quizSessionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtém o usuário atualmente autenticado
     * @return User objeto do usuário atual
     * @throws RuntimeException se o usuário não estiver autenticado
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
     * Verifica se existe um usuário autenticado
     * @return true se há usuário autenticado, false caso contrário
     */
    private boolean isUserAuthenticated() {
        try {
            getCurrentUser();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Salva um novo score (requer autenticação)
     */
    public Score saveScore(Score score) {
        User currentUser = getCurrentUser();
        score.setUser(currentUser);
        return scoreRepository.save(score);
    }

    public Score getScoreById(Long id) {
        return scoreRepository.findById(id).orElse(null);
    }

    public List<Score> getAllScores() {
        return scoreRepository.findAll();
    }

    public Score updateScoreById(Long id, Score newScoreData) {
        Optional<Score> optionalExistingScore = scoreRepository.findById(id);

        if (optionalExistingScore.isPresent()) {
            Score existingScore = optionalExistingScore.get();

            if (newScoreData.getPoints() > existingScore.getPoints()) {
                existingScore.setPoints(newScoreData.getPoints());
                return scoreRepository.save(existingScore);
            } else {
                throw new RuntimeException("Score not updated - new score must be higher");
            }
        }
        return null;
    }

    // ========================================
    // MÉTODOS PÚBLICOS (não requerem autenticação)
    // ========================================

    /**
     * Obtém ranking dos top jogadores - MÉTODO PÚBLICO
     * Pode ser chamado por usuários não autenticados
     */
    public List<RankingDTO> getTopPlayersRanking(int limit) {
        List<Score> allScores = scoreRepository.findValidScores(); // Apenas scores > 0

        if (allScores.isEmpty()) {
            return new ArrayList<>();
        }

        // Agrupa scores por usuário e calcula estatísticas
        Map<User, List<Score>> scoresByUser = allScores.stream()
                .collect(Collectors.groupingBy(Score::getUser));

        List<RankingDTO> rankings = scoresByUser.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    List<Score> userScores = entry.getValue();

                    Integer bestScore = userScores.stream()
                            .mapToInt(Score::getPoints)
                            .max()
                            .orElse(0);

                    Integer totalGames = userScores.size();

                    Double averageScore = userScores.stream()
                            .mapToInt(Score::getPoints)
                            .average()
                            .orElse(0.0);

                    Integer totalPoints = userScores.stream()
                            .mapToInt(Score::getPoints)
                            .sum();

                    return new RankingDTO(
                            user.getId(),
                            user.getUsername(),
                            bestScore,
                            totalGames,
                            Math.round(averageScore * 100.0) / 100.0,
                            totalPoints,
                            0L // Posição será definida depois
                    );
                })
                .sorted((r1, r2) -> Integer.compare(r2.bestScore(), r1.bestScore()))
                .limit(limit)
                .collect(Collectors.toList());

        // Adiciona posições no ranking
        for (int i = 0; i < rankings.size(); i++) {
            RankingDTO ranking = rankings.get(i);
            rankings.set(i, ranking.withPosition((long) (i + 1)));
        }

        return rankings;
    }

    /**
     * Obtém estatísticas globais - MÉTODO PÚBLICO
     * Pode ser chamado por usuários não autenticados
     */
    public Map<String, Object> getGlobalStats() {
        List<Score> validScores = scoreRepository.findValidScores(); // Apenas scores > 0

        Map<String, Object> stats = new HashMap<>();

        if (validScores.isEmpty()) {
            // Retorna estatísticas zeradas se não há scores válidos
            stats.put("totalPlayers", 0L);
            stats.put("totalGames", 0L);
            stats.put("highestScore", 0);
            stats.put("averageScore", 0.0);
            stats.put("totalPoints", 0L);
            stats.put("topPlayer", "Nenhum");
            return stats;
        }

        // Total de jogadores únicos que jogaram
        long totalPlayers = validScores.stream()
                .map(score -> score.getUser().getId())
                .distinct()
                .count();

        // Total de partidas válidas
        long totalGames = validScores.size();

        // Maior pontuação
        int highestScore = validScores.stream()
                .mapToInt(Score::getPoints)
                .max()
                .orElse(0);

        // Pontuação média
        double averageScore = validScores.stream()
                .mapToInt(Score::getPoints)
                .average()
                .orElse(0.0);

        // Total de pontos
        long totalPoints = validScores.stream()
                .mapToLong(Score::getPoints)
                .sum();

        // Melhor jogador (por melhor score)
        String topPlayer = validScores.stream()
                .max(Comparator.comparing(Score::getPoints))
                .map(score -> score.getUser().getUsername())
                .orElse("Nenhum");

        stats.put("totalPlayers", totalPlayers);
        stats.put("totalGames", totalGames);
        stats.put("highestScore", highestScore);
        stats.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        stats.put("totalPoints", totalPoints);
        stats.put("topPlayer", topPlayer);

        return stats;
    }

    /**
     * Top scores por usuário - MÉTODO PÚBLICO
     */
    public List<RankingDTO> getTopScoresByUser() {
        return getTopPlayersRanking(Integer.MAX_VALUE);
    }

    /**
     * Melhores scores por usuário (limitado) - MÉTODO PÚBLICO
     */
    public List<RankingDTO> getBestScoresByUser(int limit) {
        return getTopPlayersRanking(limit);
    }

    // ========================================
    // MÉTODOS PRIVADOS (requerem autenticação)
    // ========================================

    /**
     * Obtém estatísticas do usuário atual - REQUER AUTENTICAÇÃO
     */
    public UserStatsDTO getCurrentUserStats() {
        User currentUser = getCurrentUser();

        List<Score> currentUserScores = scoreRepository.findByUser(currentUser);

        if (currentUserScores.isEmpty()) {
            return new UserStatsDTO(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    0, 0, 0.0, 0, 0L, 0, 0, 0.0,
                    null, null
            );
        }

        Integer totalGames = currentUserScores.size();
        Integer bestScore = currentUserScores.stream()
                .mapToInt(Score::getPoints)
                .max()
                .orElse(0);

        Double averageScore = currentUserScores.stream()
                .mapToInt(Score::getPoints)
                .average()
                .orElse(0.0);

        Integer totalPoints = currentUserScores.stream()
                .mapToInt(Score::getPoints)
                .sum();

        // Calcular posição no ranking
        Long rankingPosition = calculateUserRankingPosition(currentUser.getId());

        // Jogos ganhos (assumindo que ganhar = score > 0)
        Integer gamesWon = (int) currentUserScores.stream()
                .filter(score -> score.getPoints() > 0)
                .count();

        Integer gamesLost = totalGames - gamesWon;
        Double winRate = totalGames > 0 ? (gamesWon.doubleValue() / totalGames) * 100 : 0.0;

        return new UserStatsDTO(
                currentUser.getId(),
                currentUser.getUsername(),
                totalGames,
                bestScore,
                Math.round(averageScore * 100.0) / 100.0,
                totalPoints,
                rankingPosition,
                gamesWon,
                gamesLost,
                Math.round(winRate * 100.0) / 100.0,
                LocalDateTime.now(),
                null
        );
    }

    /**
     * Ranking paginado - MÉTODO PÚBLICO com informações extras para usuários autenticados
     */
    public Map<String, Object> getPaginatedRanking(int page, int size) {
        List<RankingDTO> fullRanking = getTopPlayersRanking(Integer.MAX_VALUE);

        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, fullRanking.size());

        List<RankingDTO> pageContent = fullRanking.subList(startIndex, endIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("content", pageContent);
        result.put("currentPage", page);
        result.put("totalPages", (int) Math.ceil((double) fullRanking.size() / size));
        result.put("totalElements", fullRanking.size());
        result.put("size", size);
        result.put("hasNext", endIndex < fullRanking.size());
        result.put("hasPrevious", page > 0);

        // Adiciona informações do usuário se estiver autenticado
        if (isUserAuthenticated()) {
            try {
                User currentUser = getCurrentUser();
                Long userPosition = calculateUserRankingPosition(currentUser.getId());
                result.put("currentUserPosition", userPosition);
                result.put("currentUserId", currentUser.getId());
            } catch (Exception e) {
                // Ignora erros e apenas não adiciona as informações do usuário
            }
        }

        return result;
    }

    /**
     * Posição do usuário atual no ranking - REQUER AUTENTICAÇÃO
     */
    public Map<String, Object> getCurrentUserRankingPosition() {
        User currentUser = getCurrentUser();
        Long position = calculateUserRankingPosition(currentUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", currentUser.getId());
        result.put("username", currentUser.getUsername());
        result.put("position", position);
        result.put("totalPlayers", getTotalPlayersInRanking());

        return result;
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    /**
     * Calcula posição do usuário no ranking
     */
    private Long calculateUserRankingPosition(Long userId) {
        List<RankingDTO> fullRanking = getTopPlayersRanking(Integer.MAX_VALUE);

        for (int i = 0; i < fullRanking.size(); i++) {
            if (fullRanking.get(i).userId().equals(userId)) {
                return (long) (i + 1);
            }
        }
        return 0L; // Usuário não encontrado no ranking
    }

    /**
     * Conta total de jogadores no ranking
     */
    private Long getTotalPlayersInRanking() {
        List<Score> validScores = scoreRepository.findValidScores();
        return validScores.stream()
                .map(score -> score.getUser().getId())
                .distinct()
                .count();
    }
}