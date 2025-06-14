package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.models.QuizSession;
import com.lucasdavi.quizz.repositories.QuizSessionRepository;
import com.lucasdavi.quizz.repositories.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduledCleanupService {

    @Autowired
    private QuizSessionRepository quizSessionRepository;

    @Autowired
    private ScoreRepository scoreRepository;


    @Scheduled(cron = "0 30 1 * * *") // Todo dia às 01:30 (antes da limpeza geral)
    @Transactional
    public void dailyCleanupZeroScoreSessions() {
        System.out.println("🕒 [SCHEDULER] Iniciando limpeza de sessões com score zero...");

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);

        // Busca sessões finalizadas (não ativas) com score 0 criadas há mais de 24h
        List<QuizSession> zeroScoreSessions = quizSessionRepository
                .findByScoreAndIsActiveFalseAndCreatedAtBefore(0, cutoffTime);

        if (zeroScoreSessions.isEmpty()) {
            System.out.println("🗑️ [SCHEDULER] Nenhuma sessão com score zero encontrada para limpeza");
            return;
        }

        System.out.println("🗑️ [SCHEDULER] Removendo " + zeroScoreSessions.size() +
                " sessão(ões) com score zero (24h+)");

        // Remove as sessões com score zero
        quizSessionRepository.deleteAll(zeroScoreSessions);

        // Também remove os scores zero associados (se existirem)
        cleanupZeroScoresFromDatabase();

        System.out.println("✅ [SCHEDULER] Limpeza de score zero concluída com sucesso");
    }


    @Scheduled(cron = "0 45 1 * * SUN") // Todo domingo às 01:45
    @Transactional
    public void weeklyAggressiveZeroScoreCleanup() {
        System.out.println("🕒 [SCHEDULER] Iniciando limpeza agressiva semanal de score zero...");

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);

        // Remove TODAS as sessões com score 0 (ativas ou não) mais antigas que 1h
        List<QuizSession> allZeroScoreSessions = quizSessionRepository
                .findByScoreAndCreatedAtBefore(0, cutoffTime);

        if (allZeroScoreSessions.isEmpty()) {
            System.out.println("🗑️ [SCHEDULER] Nenhuma sessão com score zero encontrada para limpeza agressiva");
            return;
        }

        System.out.println("🗑️ [SCHEDULER] Limpeza agressiva: removendo " + allZeroScoreSessions.size() +
                " sessão(ões) com score zero");

        quizSessionRepository.deleteAll(allZeroScoreSessions);
        cleanupZeroScoresFromDatabase();

        System.out.println("✅ [SCHEDULER] Limpeza agressiva de score zero concluída");
    }

    @Scheduled(cron = "0 0 2 * * *") // Todo dia às 02:00
    @Transactional
    public void dailyCleanupAbandonedSessions() {
        System.out.println("🕒 [SCHEDULER] Iniciando limpeza automática de sessões abandonadas...");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);

        // Busca todas as sessões ativas criadas há mais de 7 dias
        List<QuizSession> abandonedSessions = quizSessionRepository
                .findByIsActiveTrueAndCreatedAtBefore(cutoffDate);

        if (abandonedSessions.isEmpty()) {
            System.out.println("🧹 [SCHEDULER] Nenhuma sessão abandonada encontrada para limpeza");
            return;
        }

        System.out.println("🧹 [SCHEDULER] Removendo " + abandonedSessions.size() +
                " sessão(ões) abandonada(s) há mais de 7 dias");

        // Remove as sessões abandonadas
        quizSessionRepository.deleteAll(abandonedSessions);

        System.out.println("✅ [SCHEDULER] Limpeza automática concluída com sucesso");
    }


    @Scheduled(cron = "0 0 3 * * SUN") // Todo domingo às 03:00
    @Transactional
    public void weeklyCleanupOldSessions() {
        System.out.println("🕒 [SCHEDULER] Iniciando limpeza semanal de sessões muito antigas...");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        // Busca sessões abandonadas há mais de 30 dias
        List<QuizSession> oldSessions = quizSessionRepository
                .findByIsActiveTrueAndCreatedAtBefore(cutoffDate);

        if (oldSessions.isEmpty()) {
            System.out.println("🧹 [SCHEDULER] Nenhuma sessão antiga encontrada para limpeza semanal");
            return;
        }

        System.out.println("🧹 [SCHEDULER] Removendo " + oldSessions.size() +
                " sessão(ões) muito antiga(s) (30+ dias)");

        quizSessionRepository.deleteAll(oldSessions);

        System.out.println("✅ [SCHEDULER] Limpeza semanal concluída com sucesso");
    }


    @Scheduled(cron = "0 0 * * * *") // A cada hora
    public void hourlyStatusReport() {
        long activeSessionsCount = quizSessionRepository.countByIsActiveTrue();

        if (activeSessionsCount > 100) { // Limite de alerta
            System.out.println("⚠️ [ALERT] Muitas sessões ativas detectadas: " + activeSessionsCount);
        } else if (activeSessionsCount > 50) {
            System.out.println("📊 [INFO] Sessões ativas no sistema: " + activeSessionsCount);
        }
        // Se for menor que 50, não loga para evitar spam
    }


    @Transactional
    public int cleanupSessionsOlderThanHours(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);

        List<QuizSession> abandonedSessions = quizSessionRepository
                .findByIsActiveTrueAndCreatedAtBefore(cutoffTime);

        if (!abandonedSessions.isEmpty()) {
            System.out.println("🧹 Limpeza manual: removendo " + abandonedSessions.size() +
                    " sessão(ões) abandonada(s) há mais de " + hours + " hora(s)");

            quizSessionRepository.deleteAll(abandonedSessions);
        }

        return abandonedSessions.size();
    }

    @Transactional
    public int cleanupZeroScoreSessionsOlderThanHours(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);

        List<QuizSession> zeroScoreSessions = quizSessionRepository
                .findByScoreAndIsActiveFalseAndCreatedAtBefore(0, cutoffTime);

        if (!zeroScoreSessions.isEmpty()) {
            System.out.println("🗑️ Limpeza manual score zero: removendo " + zeroScoreSessions.size() +
                    " sessão(ões) com score 0 há mais de " + hours + " hora(s)");

            quizSessionRepository.deleteAll(zeroScoreSessions);
            cleanupZeroScoresFromDatabase();
        }

        return zeroScoreSessions.size();
    }


    @Transactional
    public int cleanupAllZeroScoreSessions() {
        List<QuizSession> allZeroScoreSessions = quizSessionRepository.findByScore(0);

        if (!allZeroScoreSessions.isEmpty()) {
            System.out.println("🗑️ Limpeza agressiva manual: removendo TODAS as " + allZeroScoreSessions.size() +
                    " sessão(ões) com score zero");

            quizSessionRepository.deleteAll(allZeroScoreSessions);
            cleanupZeroScoresFromDatabase();
        }

        return allZeroScoreSessions.size();
    }


    @Transactional
    public int cleanupActiveZeroScoreSessions() {
        List<QuizSession> activeZeroSessions = quizSessionRepository
                .findByScoreAndIsActiveTrue(0);

        if (!activeZeroSessions.isEmpty()) {
            System.out.println("🗑️ Limpeza de abandonos: removendo " + activeZeroSessions.size() +
                    " sessão(ões) ativa(s) com score zero");

            quizSessionRepository.deleteAll(activeZeroSessions);
        }

        return activeZeroSessions.size();
    }


    @Transactional
    public int intelligentZeroScoreCleanup() {
        LocalDateTime abandonedCutoff = LocalDateTime.now().minusHours(2);
        LocalDateTime finishedCutoff = LocalDateTime.now().minusHours(24);

        // Sessões ativas abandonadas há mais de 2h com score 0
        List<QuizSession> abandonedZeroSessions = quizSessionRepository
                .findByScoreAndIsActiveTrueAndCreatedAtBefore(0, abandonedCutoff);

        // Sessões finalizadas há mais de 24h com score 0
        List<QuizSession> finishedZeroSessions = quizSessionRepository
                .findByScoreAndIsActiveFalseAndCreatedAtBefore(0, finishedCutoff);

        int totalDeleted = 0;

        if (!abandonedZeroSessions.isEmpty()) {
            System.out.println("🗑️ Limpeza inteligente: removendo " + abandonedZeroSessions.size() +
                    " sessão(ões) abandonada(s) com score zero (2h+)");
            quizSessionRepository.deleteAll(abandonedZeroSessions);
            totalDeleted += abandonedZeroSessions.size();
        }

        if (!finishedZeroSessions.isEmpty()) {
            System.out.println("🗑️ Limpeza inteligente: removendo " + finishedZeroSessions.size() +
                    " sessão(ões) finalizada(s) com score zero (24h+)");
            quizSessionRepository.deleteAll(finishedZeroSessions);
            totalDeleted += finishedZeroSessions.size();
        }

        if (totalDeleted > 0) {
            cleanupZeroScoresFromDatabase();
            System.out.println("✅ Limpeza inteligente concluída: " + totalDeleted + " sessões removidas");
        }

        return totalDeleted;
    }

    private void cleanupZeroScoresFromDatabase() {
        try {
            int deletedScores = scoreRepository.deleteByPoints(0);
            if (deletedScores > 0) {
                System.out.println("🗑️ Removidos " + deletedScores + " registro(s) de score zero da tabela Score");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao limpar scores zero: " + e.getMessage());
        }
    }

    public Map<String, Long> getZeroScoreSessionsReport() {
        long activeZeroScore = quizSessionRepository.countByScoreAndIsActiveTrue(0);
        long finishedZeroScore = quizSessionRepository.countByScoreAndIsActiveFalse(0);
        long totalZeroScore = quizSessionRepository.countByScore(0);
        long zeroScoreRecords = scoreRepository.countByPoints(0);

        Map<String, Long> report = new HashMap<>();
        report.put("activeZeroScoreSessions", activeZeroScore);
        report.put("finishedZeroScoreSessions", finishedZeroScore);
        report.put("totalZeroScoreSessions", totalZeroScore);
        report.put("zeroScoreRecords", zeroScoreRecords);

        return report;
    }
}