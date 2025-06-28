package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.enums.SessionStatus;
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

    /**
     * 🔧 ATUALIZADO: Limpeza diária de sessões interrompidas com score zero
     */
    @Scheduled(cron = "0 30 1 * * *") // Todo dia às 01:30
    @Transactional
    public void dailyCleanupZeroScoreSessions() {
        System.out.println("🕒 [SCHEDULER] Iniciando limpeza de sessões com score zero...");

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);

        // 🔧 CORRIGIDO: Busca sessões interrompidas com score 0 criadas há mais de 24h
        List<QuizSession> zeroScoreSessions = quizSessionRepository
                .findByScoreAndStatusAndCreatedAtBefore(0, SessionStatus.INTERRUPTED, cutoffTime);

        if (zeroScoreSessions.isEmpty()) {
            System.out.println("🗑️ [SCHEDULER] Nenhuma sessão interrompida com score zero encontrada para limpeza");
            return;
        }

        System.out.println("🗑️ [SCHEDULER] Removendo " + zeroScoreSessions.size() +
                " sessão(ões) interrompida(s) com score zero (24h+)");

        // Remove as sessões com score zero
        quizSessionRepository.deleteAll(zeroScoreSessions);

        // Também remove os scores zero associados (se existirem)
        cleanupZeroScoresFromDatabase();

        System.out.println("✅ [SCHEDULER] Limpeza de score zero concluída com sucesso");
    }



    /**
     * 🔧 ATUALIZADO: Limpeza diária de sessões abandonadas
     */
    @Scheduled(cron = "0 0 2 * * *") // Todo dia às 02:00
    @Transactional
    public void dailyCleanupAbandonedSessions() {
        System.out.println("🕒 [SCHEDULER] Iniciando limpeza automática de sessões abandonadas...");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);

        // 🔧 CORRIGIDO: Busca todas as sessões EM PROGRESSO criadas há mais de 7 dias
        List<QuizSession> abandonedSessions = quizSessionRepository
                .findByStatusAndCreatedAtBefore(SessionStatus.IN_PROGRESS, cutoffDate);

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

    /**
     * 🔧 ATUALIZADO: Limpeza semanal de sessões muito antigas
     */
    @Scheduled(cron = "0 0 3 * * SUN") // Todo domingo às 03:00
    @Transactional
    public void weeklyCleanupOldSessions() {
        System.out.println("🕒 [SCHEDULER] Iniciando limpeza semanal de sessões muito antigas...");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        // 🔧 CORRIGIDO: Busca sessões EM PROGRESSO há mais de 30 dias
        List<QuizSession> oldSessions = quizSessionRepository
                .findByStatusAndCreatedAtBefore(SessionStatus.IN_PROGRESS, cutoffDate);

        if (oldSessions.isEmpty()) {
            System.out.println("🧹 [SCHEDULER] Nenhuma sessão antiga encontrada para limpeza semanal");
            return;
        }

        System.out.println("🧹 [SCHEDULER] Removendo " + oldSessions.size() +
                " sessão(ões) muito antiga(s) (30+ dias)");

        quizSessionRepository.deleteAll(oldSessions);

        System.out.println("✅ [SCHEDULER] Limpeza semanal concluída com sucesso");
    }

    /**
     * 🔧 ATUALIZADO: Relatório de status de sessões a cada hora
     */
    @Scheduled(cron = "0 0 * * * *") // A cada hora
    public void hourlyStatusReport() {
        long inProgressSessionsCount = quizSessionRepository.countByStatus(SessionStatus.IN_PROGRESS);

        if (inProgressSessionsCount > 100) { // Limite de alerta
            System.out.println("⚠️ [ALERT] Muitas sessões em progresso detectadas: " + inProgressSessionsCount);
        } else if (inProgressSessionsCount > 50) {
            System.out.println("📊 [INFO] Sessões em progresso no sistema: " + inProgressSessionsCount);
        }
        // Se for menor que 50, não loga para evitar spam
    }

    /**
     * 🔧 ATUALIZADO: Limpeza manual de sessões antigas por horas
     */
    @Transactional
    public int cleanupSessionsOlderThanHours(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);

        List<QuizSession> abandonedSessions = quizSessionRepository
                .findByStatusAndCreatedAtBefore(SessionStatus.IN_PROGRESS, cutoffTime);

        if (!abandonedSessions.isEmpty()) {
            System.out.println("🧹 Limpeza manual: removendo " + abandonedSessions.size() +
                    " sessão(ões) em progresso há mais de " + hours + " hora(s)");

            quizSessionRepository.deleteAll(abandonedSessions);
        }

        return abandonedSessions.size();
    }

    /**
     * 🔧 ATUALIZADO: Limpeza de sessões interrompidas com score zero por horas
     */
    @Transactional
    public int cleanupZeroScoreSessionsOlderThanHours(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);

        List<QuizSession> zeroScoreSessions = quizSessionRepository
                .findByScoreAndStatusAndCreatedAtBefore(0, SessionStatus.INTERRUPTED, cutoffTime);

        if (!zeroScoreSessions.isEmpty()) {
            System.out.println("🗑️ Limpeza manual score zero: removendo " + zeroScoreSessions.size() +
                    " sessão(ões) interrompida(s) com score 0 há mais de " + hours + " hora(s)");

            quizSessionRepository.deleteAll(zeroScoreSessions);
            cleanupZeroScoresFromDatabase();
        }

        return zeroScoreSessions.size();
    }

    /**
     * 🔧 ATUALIZADO: Limpeza de todas as sessões com score zero
     */
    @Transactional
    public int cleanupAllZeroScoreSessions() {
        List<QuizSession> allZeroScoreSessions = quizSessionRepository.findByScoreAndStatus(0, SessionStatus.INTERRUPTED);

        if (!allZeroScoreSessions.isEmpty()) {
            System.out.println("🗑️ Limpeza agressiva manual: removendo TODAS as " + allZeroScoreSessions.size() +
                    " sessão(ões) interrompida(s) com score zero");

            quizSessionRepository.deleteAll(allZeroScoreSessions);
            cleanupZeroScoresFromDatabase();
        }

        return allZeroScoreSessions.size();
    }

    /**
     * 🔧 ATUALIZADO: Limpeza de sessões em progresso com score zero
     */
    @Transactional
    public int cleanupActiveZeroScoreSessions() {
        List<QuizSession> inProgressZeroSessions = quizSessionRepository
                .findByScoreAndStatus(0, SessionStatus.IN_PROGRESS);

        if (!inProgressZeroSessions.isEmpty()) {
            System.out.println("🗑️ Limpeza de abandonos: removendo " + inProgressZeroSessions.size() +
                    " sessão(ões) em progresso com score zero");

            quizSessionRepository.deleteAll(inProgressZeroSessions);
        }

        return inProgressZeroSessions.size();
    }

    /**
     * 🔧 ATUALIZADO: Limpeza inteligente de score zero
     */
    @Transactional
    public int intelligentZeroScoreCleanup() {
        LocalDateTime abandonedCutoff = LocalDateTime.now().minusHours(2);
        LocalDateTime finishedCutoff = LocalDateTime.now().minusHours(24);

        // Sessões em progresso abandonadas há mais de 2h com score 0
        List<QuizSession> abandonedZeroSessions = quizSessionRepository
                .findByScoreAndStatusAndCreatedAtBefore(0, SessionStatus.IN_PROGRESS, abandonedCutoff);

        // Sessões interrompidas há mais de 24h com score 0
        List<QuizSession> finishedZeroSessions = quizSessionRepository
                .findByScoreAndStatusAndCreatedAtBefore(0, SessionStatus.INTERRUPTED, finishedCutoff);

        int totalDeleted = 0;

        if (!abandonedZeroSessions.isEmpty()) {
            System.out.println("🗑️ Limpeza inteligente: removendo " + abandonedZeroSessions.size() +
                    " sessão(ões) em progresso abandonada(s) com score zero (2h+)");
            quizSessionRepository.deleteAll(abandonedZeroSessions);
            totalDeleted += abandonedZeroSessions.size();
        }

        if (!finishedZeroSessions.isEmpty()) {
            System.out.println("🗑️ Limpeza inteligente: removendo " + finishedZeroSessions.size() +
                    " sessão(ões) interrompida(s) com score zero (24h+)");
            quizSessionRepository.deleteAll(finishedZeroSessions);
            totalDeleted += finishedZeroSessions.size();
        }

        if (totalDeleted > 0) {
            cleanupZeroScoresFromDatabase();
            System.out.println("✅ Limpeza inteligente concluída: " + totalDeleted + " sessões removidas");
        }

        return totalDeleted;
    }

    /**
     * Remove scores zero órfãos do banco de dados
     */
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

    /**
     * 🔧 ATUALIZADO: Relatório de sessões com score zero
     */
    public Map<String, Long> getZeroScoreSessionsReport() {
        long inProgressZeroScore = quizSessionRepository.countByScoreAndStatus(0, SessionStatus.IN_PROGRESS);
        long interruptedZeroScore = quizSessionRepository.countByScoreAndStatus(0, SessionStatus.INTERRUPTED);
        long completedZeroScore = quizSessionRepository.countByScoreAndStatus(0, SessionStatus.COMPLETED);
        long totalZeroScore = inProgressZeroScore + interruptedZeroScore + completedZeroScore;
        long zeroScoreRecords = scoreRepository.countByPoints(0);

        Map<String, Long> report = new HashMap<>();
        report.put("inProgressZeroScoreSessions", inProgressZeroScore);
        report.put("interruptedZeroScoreSessions", interruptedZeroScore);
        report.put("completedZeroScoreSessions", completedZeroScore);
        report.put("totalZeroScoreSessions", totalZeroScore);
        report.put("zeroScoreRecords", zeroScoreRecords);

        return report;
    }

    /**
     * 🆕 NOVO: Relatório geral de estatísticas de sessões por status
     */
    public Map<String, Object> getSessionStatusReport() {
        long inProgressSessions = quizSessionRepository.countByStatus(SessionStatus.IN_PROGRESS);
        long completedSessions = quizSessionRepository.countByStatus(SessionStatus.COMPLETED);
        long interruptedSessions = quizSessionRepository.countByStatus(SessionStatus.INTERRUPTED);
        long totalSessions = inProgressSessions + completedSessions + interruptedSessions;

        // Calcula percentuais
        double completionRate = totalSessions > 0 ? (double) completedSessions / totalSessions * 100.0 : 0.0;
        double interruptionRate = totalSessions > 0 ? (double) interruptedSessions / totalSessions * 100.0 : 0.0;
        double inProgressRate = totalSessions > 0 ? (double) inProgressSessions / totalSessions * 100.0 : 0.0;

        // Sessões abandonadas (em progresso há mais de 24h)
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
        List<QuizSession> abandonedSessions = quizSessionRepository
                .findByStatusAndCreatedAtBefore(SessionStatus.IN_PROGRESS, dayAgo);

        Map<String, Object> report = new HashMap<>();
        report.put("totalSessions", totalSessions);
        report.put("inProgressSessions", inProgressSessions);
        report.put("completedSessions", completedSessions);
        report.put("interruptedSessions", interruptedSessions);
        report.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        report.put("interruptionRate", Math.round(interruptionRate * 100.0) / 100.0);
        report.put("inProgressRate", Math.round(inProgressRate * 100.0) / 100.0);
        report.put("abandonedSessions", abandonedSessions.size());
        report.put("reportGeneratedAt", LocalDateTime.now());

        return report;
    }

    /**
     * 🆕 NOVO: Limpeza específica para sessões abandonadas há X dias
     */
    @Transactional
    public int cleanupAbandonedSessionsOlderThanDays(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        List<QuizSession> abandonedSessions = quizSessionRepository
                .findByStatusAndCreatedAtBefore(SessionStatus.IN_PROGRESS, cutoffDate);

        if (!abandonedSessions.isEmpty()) {
            System.out.println("🧹 Limpeza manual: removendo " + abandonedSessions.size() +
                    " sessão(ões) abandonada(s) há mais de " + days + " dia(s)");

            quizSessionRepository.deleteAll(abandonedSessions);
        }

        return abandonedSessions.size();
    }

    /**
     * 🆕 NOVO: Força interrupção de sessões em progresso antigas
     */
    @Transactional
    public int forceInterruptOldInProgressSessions(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);

        List<QuizSession> oldInProgressSessions = quizSessionRepository
                .findByStatusAndCreatedAtBefore(SessionStatus.IN_PROGRESS, cutoffTime);

        if (!oldInProgressSessions.isEmpty()) {
            System.out.println("⏹️ Forçando interrupção de " + oldInProgressSessions.size() +
                    " sessão(ões) em progresso há mais de " + hours + " hora(s)");

            oldInProgressSessions.forEach(session -> {
                session.interruptSession();
            });
            quizSessionRepository.saveAll(oldInProgressSessions);
        }

        return oldInProgressSessions.size();
    }
}