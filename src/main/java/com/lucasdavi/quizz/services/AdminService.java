package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.dtos.AdminUserDTO;
import com.lucasdavi.quizz.dtos.CreateAdminDTO;
import com.lucasdavi.quizz.enums.SessionStatus;
import com.lucasdavi.quizz.enums.UserRole;
import com.lucasdavi.quizz.models.QuizSession;
import com.lucasdavi.quizz.models.Score;
import com.lucasdavi.quizz.models.User;
import com.lucasdavi.quizz.repositories.QuizSessionRepository;
import com.lucasdavi.quizz.repositories.ScoreRepository;
import com.lucasdavi.quizz.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private QuizSessionRepository quizSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ScheduledCleanupService scheduledCleanupService;

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
     * Verifica se o usuário atual é administrador
     */
    private void checkAdminAccess() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("User not authorized - admin access required");
        }
    }

    /**
     * Lista todos os usuários do sistema com suas estatísticas
     */
    public List<AdminUserDTO> getAllUsers() {
        checkAdminAccess();

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(this::convertToAdminUserDTO)
                .sorted((u1, u2) -> u2.createdAt().compareTo(u1.createdAt())) // Mais recentes primeiro
                .collect(Collectors.toList());
    }

    /**
     * Busca um usuário específico por ID
     */
    public AdminUserDTO getUserById(Long userId) {
        checkAdminAccess();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return convertToAdminUserDTO(user);
    }

    /**
     * Promove um usuário a administrador
     */
    @Transactional
    public AdminUserDTO promoteUserToAdmin(Long userId) {
        checkAdminAccess();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("User is already admin");
        }

        user.setRole(UserRole.ADMIN);
        User updatedUser = userRepository.save(user);

        System.out.println("👑 Usuário " + user.getUsername() + " promovido a ADMIN");

        return convertToAdminUserDTO(updatedUser);
    }

    /**
     * Rebaixa um administrador a usuário comum
     */
    @Transactional
    public AdminUserDTO demoteAdminToUser(Long userId) {
        checkAdminAccess();
        User currentUser = getCurrentUser();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("User is not admin");
        }

        // Impede que o admin rebaixe a si mesmo
        if (user.getId().equals(currentUser.getId())) {
            throw new RuntimeException("Admin cannot demote self");
        }

        user.setRole(UserRole.USER);
        User updatedUser = userRepository.save(user);

        System.out.println("👤 Administrador " + user.getUsername() + " rebaixado a USER");

        return convertToAdminUserDTO(updatedUser);
    }

    /**
     * Cria um novo administrador
     */
    @Transactional
    public AdminUserDTO createAdmin(CreateAdminDTO createAdminDTO) {
        checkAdminAccess();

        // Verifica se username já existe
        if (userRepository.existsByUsername(createAdminDTO.username())) {
            throw new RuntimeException("Username already exists");
        }

        // Verifica se email já existe
        if (userRepository.existsByEmail(createAdminDTO.email())) {
            throw new RuntimeException("Email already exists");
        }

        // Cria novo usuário com role ADMIN
        User newAdmin = new User();
        newAdmin.setUsername(createAdminDTO.username());
        newAdmin.setEmail(createAdminDTO.email());
        newAdmin.setPassword(passwordEncoder.encode(createAdminDTO.password()));
        newAdmin.setRole(UserRole.ADMIN);
        newAdmin.setCreatedAt(LocalDateTime.now());

        User savedAdmin = userRepository.save(newAdmin);

        System.out.println("👑 Novo administrador criado: " + savedAdmin.getUsername());

        return convertToAdminUserDTO(savedAdmin);
    }

    /**
     * Deleta um usuário e todos os seus dados
     * CUIDADO: Operação irreversível!
     */
    @Transactional
    public String deleteUser(Long userId) {
        checkAdminAccess();
        User currentUser = getCurrentUser();

        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Impede que o admin delete a si mesmo
        if (userToDelete.getId().equals(currentUser.getId())) {
            throw new RuntimeException("Admin cannot delete self");
        }

        String deletedUsername = userToDelete.getUsername();

        System.out.println("🗑️ Iniciando exclusão completa do usuário: " + deletedUsername);

        // 1. Remove todas as sessões de quiz do usuário
        List<QuizSession> userSessions = quizSessionRepository.findByUserOrderByCreatedAtDesc(userToDelete);
        if (!userSessions.isEmpty()) {
            System.out.println("🗑️ Removendo " + userSessions.size() + " sessão(ões) de quiz");
            quizSessionRepository.deleteAll(userSessions);
        }

        // 2. Remove todos os scores do usuário
        List<Score> userScores = scoreRepository.findByUser(userToDelete);
        if (!userScores.isEmpty()) {
            System.out.println("🗑️ Removendo " + userScores.size() + " score(s)");
            scoreRepository.deleteAll(userScores);
        }

        // 3. Por último, remove o usuário
        userRepository.delete(userToDelete);

        System.out.println("✅ Usuário " + deletedUsername + " excluído completamente pelo admin!");

        return deletedUsername;
    }

    /**
     * Busca estatísticas detalhadas de um usuário específico
     */
    public Map<String, Object> getUserDetailedStats(Long userId) {
        checkAdminAccess();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> stats = new HashMap<>();

        // Estatísticas básicas de games
        long totalGames = scoreRepository.countByUser(user);
        Integer bestScore = scoreRepository.findTopScoreByUser(user)
                .stream()
                .findFirst()
                .map(Score::getPoints)
                .orElse(0);

        Double averageScore = scoreRepository.getAverageScoreByUser(user);
        if (averageScore == null) averageScore = 0.0;

        Long totalPoints = scoreRepository.getTotalPointsByUser(user);
        if (totalPoints == null) totalPoints = 0L;

        // 🔧 CORRIGIDO: Estatísticas de sessões usando o novo sistema
        long inProgressSessions = quizSessionRepository.countByUserAndStatus(user, SessionStatus.IN_PROGRESS);
        long completedSessions = quizSessionRepository.countByUserAndStatus(user, SessionStatus.COMPLETED);
        long interruptedSessions = quizSessionRepository.countByUserAndStatus(user, SessionStatus.INTERRUPTED);
        long totalSessions = inProgressSessions + completedSessions + interruptedSessions;

        List<QuizSession> allSessions = quizSessionRepository.findByUserOrderByCreatedAtDesc(user);

        LocalDateTime lastGameDate = allSessions.stream()
                .map(QuizSession::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Calcula posição no ranking
        long rankingPosition = calculateUserRankingPosition(user);

        stats.put("userId", user.getId());
        stats.put("username", user.getUsername());
        stats.put("email", user.getEmail());
        stats.put("role", user.getRole());
        stats.put("totalGames", totalGames);
        stats.put("bestScore", bestScore);
        stats.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        stats.put("totalPoints", totalPoints);
        stats.put("rankingPosition", rankingPosition);

        // 🆕 NOVO: Estatísticas detalhadas por status
        stats.put("totalSessions", totalSessions);
        stats.put("inProgressSessions", inProgressSessions);
        stats.put("completedSessions", completedSessions);
        stats.put("interruptedSessions", interruptedSessions);

        // Taxa de completude
        double completionRate = totalSessions > 0 ? (double) completedSessions / totalSessions * 100.0 : 0.0;
        stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

        stats.put("lastGameDate", lastGameDate);
        stats.put("memberSince", user.getCreatedAt());

        return stats;
    }

    /**
     * Busca estatísticas gerais do sistema
     */
    public Map<String, Object> getSystemStats() {
        checkAdminAccess();

        Map<String, Object> stats = new HashMap<>();

        // Contadores básicos
        long totalUsers = userRepository.count();
        long totalAdmins = userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .count();

        // 🔧 CORRIGIDO: Usa os novos métodos baseados em SessionStatus
        long totalSessions = quizSessionRepository.count();
        long inProgressSessions = quizSessionRepository.countByStatus(SessionStatus.IN_PROGRESS);
        long completedSessions = quizSessionRepository.countByStatus(SessionStatus.COMPLETED);
        long interruptedSessions = quizSessionRepository.countByStatus(SessionStatus.INTERRUPTED);

        long totalScores = scoreRepository.count();

        // Estatísticas de scores
        Integer highestScore = scoreRepository.findMaxScore();
        Double averageScore = scoreRepository.findAverageScore();
        Long totalPoints = scoreRepository.findTotalPoints();

        // Usuários ativos (que jogaram pelo menos uma vez)
        long activeUsers = scoreRepository.countDistinctUsers();

        stats.put("totalUsers", totalUsers);
        stats.put("totalAdmins", totalAdmins);
        stats.put("totalSessions", totalSessions);
        stats.put("inProgressSessions", inProgressSessions);
        stats.put("completedSessions", completedSessions);
        stats.put("interruptedSessions", interruptedSessions);
        stats.put("totalScores", totalScores);
        stats.put("highestScore", highestScore != null ? highestScore : 0);
        stats.put("averageScore", averageScore != null ? Math.round(averageScore * 100.0) / 100.0 : 0.0);
        stats.put("totalPoints", totalPoints != null ? totalPoints : 0L);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", totalUsers - activeUsers);

        // 🆕 NOVO: Taxa de completude global
        double globalCompletionRate = totalSessions > 0 ? (double) completedSessions / totalSessions * 100.0 : 0.0;
        stats.put("globalCompletionRate", Math.round(globalCompletionRate * 100.0) / 100.0);

        return stats;
    }

    /**
     * Busca relatório de saúde do sistema
     */
    public Map<String, Object> getSystemHealth() {
        checkAdminAccess();

        Map<String, Object> health = new HashMap<>();

        // 🔧 CORRIGIDO: Usa novos métodos para verificar saúde do sistema
        long inProgressSessionsCount = quizSessionRepository.countByStatus(SessionStatus.IN_PROGRESS);
        long interruptedSessionsCount = quizSessionRepository.countByStatus(SessionStatus.INTERRUPTED);
        long completedSessionsCount = quizSessionRepository.countByStatus(SessionStatus.COMPLETED);

        // Busca sessões em progresso antigas (potencialmente abandonadas)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<QuizSession> oldInProgressSessions = quizSessionRepository.findInProgressSessionsOlderThan(weekAgo);

        // Determina status geral do sistema
        String systemStatus = "healthy";
        List<String> warnings = new ArrayList<>();
        List<String> issues = new ArrayList<>();

        if (inProgressSessionsCount > 100) {
            issues.add("Muitas sessões em progresso: " + inProgressSessionsCount);
            systemStatus = "critical";
        } else if (inProgressSessionsCount > 50) {
            warnings.add("Sessões em progresso elevadas: " + inProgressSessionsCount);
            if (systemStatus.equals("healthy")) systemStatus = "warning";
        }

        if (interruptedSessionsCount > 200) {
            issues.add("Muitas sessões interrompidas: " + interruptedSessionsCount);
            systemStatus = "critical";
        } else if (interruptedSessionsCount > 100) {
            warnings.add("Sessões interrompidas elevadas: " + interruptedSessionsCount);
            if (systemStatus.equals("healthy")) systemStatus = "warning";
        }

        if (!oldInProgressSessions.isEmpty()) {
            warnings.add("Sessões abandonadas antigas: " + oldInProgressSessions.size());
            if (systemStatus.equals("healthy")) systemStatus = "warning";
        }

        health.put("systemStatus", systemStatus);
        health.put("inProgressSessions", inProgressSessionsCount);
        health.put("completedSessions", completedSessionsCount);
        health.put("interruptedSessions", interruptedSessionsCount);
        health.put("oldAbandonedSessions", oldInProgressSessions.size());
        health.put("warnings", warnings);
        health.put("issues", issues);
        health.put("lastCheck", LocalDateTime.now());
        health.put("needsCleanup", interruptedSessionsCount > 50 || inProgressSessionsCount > 20);

        // Recomendações
        List<String> recommendations = new ArrayList<>();
        if (interruptedSessionsCount > 100) {
            recommendations.add("Execute limpeza de sessões interrompidas");
        }
        if (inProgressSessionsCount > 50) {
            recommendations.add("Execute limpeza de sessões abandonadas");
        }
        if (oldInProgressSessions.size() > 10) {
            recommendations.add("Execute limpeza de sessões antigas");
        }

        health.put("recommendations", recommendations);

        return health;
    }

    /**
     * Executa limpeza forçada completa do sistema
     * CUIDADO: Operação agressiva!
     */
    @Transactional
    public Map<String, Object> forceSystemCleanup() {
        checkAdminAccess();

        System.out.println("⚠️ ADMIN executando limpeza forçada completa do sistema!");

        Map<String, Object> results = new HashMap<>();

        try {
            // 1. Limpeza inteligente usando o ScheduledCleanupService
            int zeroScoreDeleted = scheduledCleanupService.intelligentZeroScoreCleanup();
            results.put("zeroScoreSessionsDeleted", zeroScoreDeleted);

            // 2. 🔧 CORRIGIDO: Limpeza de sessões abandonadas usando novos métodos
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            List<QuizSession> abandonedSessions = quizSessionRepository.findInProgressSessionsOlderThan(oneDayAgo);
            if (!abandonedSessions.isEmpty()) {
                quizSessionRepository.deleteAll(abandonedSessions);
            }
            results.put("abandonedSessionsDeleted", abandonedSessions.size());

            // 3. Finaliza sessões em progresso muito antigas (mais de 7 dias)
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            List<QuizSession> veryOldSessions = quizSessionRepository.findInProgressSessionsOlderThan(weekAgo);
            veryOldSessions.forEach(session -> {
                session.interruptSession(); // 🔧 Usa novo método
            });
            if (!veryOldSessions.isEmpty()) {
                quizSessionRepository.saveAll(veryOldSessions);
            }
            results.put("oldSessionsFinished", veryOldSessions.size());

            // 4. Remove scores zero órfãos do banco
            int orphanScoresDeleted = scoreRepository.deleteByPoints(0);
            results.put("orphanScoresDeleted", orphanScoresDeleted);

            results.put("success", true);
            results.put("timestamp", LocalDateTime.now());
            results.put("executedBy", getCurrentUser().getUsername());

            System.out.println("✅ Limpeza forçada concluída com sucesso!");
            System.out.println("   - Sessões score zero: " + zeroScoreDeleted);
            System.out.println("   - Sessões abandonadas: " + abandonedSessions.size());
            System.out.println("   - Sessões antigas finalizadas: " + veryOldSessions.size());
            System.out.println("   - Scores órfãos removidos: " + orphanScoresDeleted);

        } catch (Exception e) {
            System.err.println("❌ Erro durante limpeza forçada: " + e.getMessage());
            results.put("success", false);
            results.put("error", e.getMessage());
            throw new RuntimeException("Erro durante limpeza forçada: " + e.getMessage());
        }

        return results;
    }

    /**
     * Converte User para AdminUserDTO com estatísticas
     */
    private AdminUserDTO convertToAdminUserDTO(User user) {
        // Estatísticas básicas
        long totalGames = scoreRepository.countByUser(user);
        Integer bestScore = scoreRepository.findTopScoreByUser(user)
                .stream()
                .findFirst()
                .map(Score::getPoints)
                .orElse(0);

        Double averageScore = scoreRepository.getAverageScoreByUser(user);
        if (averageScore == null) averageScore = 0.0;

        Integer totalPoints = scoreRepository.getTotalPointsByUser(user) != null
                ? scoreRepository.getTotalPointsByUser(user).intValue() : 0;

        long rankingPosition = calculateUserRankingPosition(user);

        // 🔧 CORRIGIDO: Usa novo método para contar sessões em progresso
        long activeSessions = quizSessionRepository.countByUserAndStatus(user, SessionStatus.IN_PROGRESS);

        // Data do último jogo
        LocalDateTime lastGameDate = quizSessionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .findFirst()
                .map(QuizSession::getCreatedAt)
                .orElse(null);

        // Status do usuário
        String status = "active";
        if (totalGames == 0) {
            status = "new";
        } else if (lastGameDate != null && lastGameDate.isBefore(LocalDateTime.now().minusDays(30))) {
            status = "inactive";
        }

        return new AdminUserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                (int) totalGames,
                bestScore,
                Math.round(averageScore * 100.0) / 100.0,
                totalPoints,
                rankingPosition,
                lastGameDate,
                (int) activeSessions,
                status
        );
    }

    /**
     * Calcula posição do usuário no ranking
     */
    private long calculateUserRankingPosition(User user) {
        try {
            Integer userBestScore = scoreRepository.findTopScoreByUser(user)
                    .stream()
                    .findFirst()
                    .map(Score::getPoints)
                    .orElse(0);

            // Conta quantos usuários têm score melhor
            long betterScores = scoreRepository.findAll()
                    .stream()
                    .collect(Collectors.groupingBy(
                            Score::getUser,
                            Collectors.maxBy(Comparator.comparing(Score::getPoints))
                    ))
                    .values()
                    .stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .mapToInt(Score::getPoints)
                    .filter(score -> score > userBestScore)
                    .count();

            return betterScores + 1;
        } catch (Exception e) {
            return 0; // Retorna 0 se houver erro
        }
    }
}