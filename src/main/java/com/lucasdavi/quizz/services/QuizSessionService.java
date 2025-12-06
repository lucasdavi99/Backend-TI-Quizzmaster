package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.dtos.*;
import com.lucasdavi.quizz.enums.SessionStatus;
import com.lucasdavi.quizz.enums.Difficulty; // Novo import
import com.lucasdavi.quizz.exceptions.EntityNotFoundException;
import com.lucasdavi.quizz.models.*;
import com.lucasdavi.quizz.repositories.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizSessionService {

    @Autowired
    private QuizSessionRepository quizSessionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new RuntimeException("User not authenticated");
        }
        return (User) principal;
    }

    @Transactional
    public QuizSessionStateDTO startNewSession(StartQuizSessionDTO dto) {
        User currentUser = getCurrentUser();

        // 🔧 ATUALIZADO: Busca sessões ativas usando o novo status
        Optional<QuizSession> activeSession = quizSessionRepository.findActiveSessionByUser(currentUser);
        if (activeSession.isPresent()) {
            QuizSession previousSession = activeSession.get();
            System.out.println("⚠️ Finalizando sessão anterior ativa - ID: " + previousSession.getId());

            // 🆕 NOVO: Usa o novo método para interromper sessão anterior
            previousSession.interruptSession();
            quizSessionRepository.save(previousSession);

            System.out.println("✅ Sessão anterior interrompida automaticamente");
        }

        List<Question> allQuestions = questionRepository.findAll();
        if (allQuestions.size() < dto.numberOfQuestions()) {
            throw new RuntimeException("Not enough questions available");
        }

        // --- SISTEMA DE DIFICULDADE PROGRESSIVA ---
        List<Question> easyQuestions = allQuestions.stream()
                .filter(q -> q.getDifficulty() == Difficulty.EASY)
                .collect(Collectors.toList());
        List<Question> mediumQuestions = allQuestions.stream()
                .filter(q -> q.getDifficulty() == Difficulty.MEDIUM)
                .collect(Collectors.toList());
        List<Question> hardQuestions = allQuestions.stream()
                .filter(q -> q.getDifficulty() == Difficulty.HARD)
                .collect(Collectors.toList());
        // Se alguma categoria estiver vazia (para retrocompatibilidade), trate como EASY
        List<Question> defaultQuestions = allQuestions.stream()
                .filter(q -> q.getDifficulty() == null)
                .collect(Collectors.toList());
        easyQuestions.addAll(defaultQuestions);

        Collections.shuffle(easyQuestions);
        Collections.shuffle(mediumQuestions);
        Collections.shuffle(hardQuestions);

        List<Question> selectedQuestions = new ArrayList<>();
        // 🚀 MODO TODAS AS PERGUNTAS: Adiciona tudo em ordem progressiva
        selectedQuestions.addAll(easyQuestions);
        selectedQuestions.addAll(mediumQuestions);
        selectedQuestions.addAll(hardQuestions);
        
        
        // ORDENAÇÃO POR DIFICULDADE PRA JOGABILIDADE
        // Easy -> Medium -> Hard
        sortQuestionsByDifficulty(selectedQuestions);

        // 🆕 NOVO: Cria sessão com status IN_PROGRESS


        // 🆕 NOVO: Cria sessão com status IN_PROGRESS
        QuizSession session = new QuizSession();
        session.setUser(currentUser);
        session.setQuestions(selectedQuestions);
        session.setCurrentQuestionIndex(0);
        session.setScore(0);
        session.setStatus(SessionStatus.IN_PROGRESS);
        session.setIsActive(true);

        QuizSession savedSession = quizSessionRepository.save(session);
        System.out.println("🎮 Nova sessão criada - ID: " + savedSession.getId() +
                ", Status: " + savedSession.getStatus().getDescription());

        return convertToStateDTO(savedSession);
    }

    @Transactional
    public int cleanupAbandonedSessions() {
        User currentUser = getCurrentUser();

        List<QuizSession> abandonedSessions = quizSessionRepository.findActiveSessionsByUser(currentUser);

        if (abandonedSessions.isEmpty()) {
            System.out.println("🧹 Nenhuma sessão abandonada encontrada para limpeza");
            return 0;
        }

        System.out.println("🧹 Removendo " + abandonedSessions.size() + " sessão(ões) abandonada(s)");

        quizSessionRepository.deleteAll(abandonedSessions);

        return abandonedSessions.size();
    }

    @Transactional
    public int finishAllActiveSessions() {
        User currentUser = getCurrentUser();

        List<QuizSession> activeSessions = quizSessionRepository.findActiveSessionsByUser(currentUser);

        if (activeSessions.isEmpty()) {
            return 0;
        }

        System.out.println("⏹️ Finalizando " + activeSessions.size() + " sessão(ões) ativa(s)");

        // 🆕 NOVO: Usa o novo método para interromper sessões
        activeSessions.forEach(session -> {
            session.interruptSession();
        });

        quizSessionRepository.saveAll(activeSessions);

        return activeSessions.size();
    }

    @Transactional
    public int cleanupOldAbandonedSessions(int daysOld) {
        User currentUser = getCurrentUser();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);

        List<QuizSession> oldAbandonedSessions = quizSessionRepository
                .findActiveSessionsOlderThan(currentUser, cutoffDate);

        if (oldAbandonedSessions.isEmpty()) {
            return 0;
        }

        System.out.println("🧹 Removendo " + oldAbandonedSessions.size() +
                " sessão(ões) abandonada(s) há mais de " + daysOld + " dia(s)");

        quizSessionRepository.deleteAll(oldAbandonedSessions);

        return oldAbandonedSessions.size();
    }

    @Transactional
    public QuizSessionResultDTO answerQuestion(Long sessionId, AnswerQuestionDTO dto) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // ✅ Forçar carregamento de todas as relações necessárias
        session.getQuestions().size(); // Carregar questions
        session.getQuestions().forEach(q -> q.getAnswers().size()); // Carregar answers de cada question

        // 🔥 CRÍTICO: Garantir ordenação por dificuldade para alinhar com frontend
        sortQuestionsByDifficulty(session.getQuestions());

        // Verificar se a sessão pertence ao usuário atual
        User currentUser = getCurrentUser();
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to session");
        }

        // 🆕 NOVO: Verifica se a sessão está em progresso
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new RuntimeException("Session is not in progress - Status: " + session.getStatus().getDescription());
        }

        Question currentQuestion = session.getCurrentQuestion();
        if (currentQuestion == null) {
            throw new RuntimeException("No current question available");
        }

        // Forçar carregamento das answers da pergunta atual
        currentQuestion.getAnswers().size();

        Answer selectedAnswer = answerRepository.findById(dto.answerId())
                .orElseThrow(() -> new EntityNotFoundException("Answer not found"));

        System.out.println("🔍 PROCESSANDO RESPOSTA:");
        System.out.println("   Current Question ID: " + currentQuestion.getId());
        System.out.println("   Selected Answer ID: " + selectedAnswer.getId());
        System.out.println("   Session ID: " + session.getId());
        System.out.println("   Current Index: " + session.getCurrentQuestionIndex());
        System.out.println("   Total Questions: " + session.getQuestions().size());

        if (!selectedAnswer.getQuestion().getId().equals(currentQuestion.getId())) {
            throw new RuntimeException("Answer does not belong to current question");
        }

        boolean isCorrect = selectedAnswer.getIsCorrect();

        if (isCorrect) {
            // ✅ Resposta correta
            session.setScore(session.getScore() + 10);
            session.moveToNextQuestion();

            // 🆕 NOVO: Verifica se completou todas as perguntas
            if (session.isFullyCompleted()) {
                // 🎉 QUIZ COMPLETADO COM SUCESSO!
                session.completeSession();
                quizSessionRepository.save(session);
                saveScoreToDatabase(session);

                System.out.println("🎉 QUIZ COMPLETADO! Score final: " + session.getScore());
                return createCompletedResult(session);
            } else {
                // Continue para próxima pergunta
                quizSessionRepository.save(session);
                System.out.println("➡️ Próxima pergunta - Index: " + session.getCurrentQuestionIndex());
                return null; // Continua o quiz
            }
        } else {
            // ❌ Resposta incorreta - interrompe o quiz
            session.completeSession();
            quizSessionRepository.save(session);
            saveScoreToDatabase(session);

            return createInterruptedResult(session, "Resposta incorreta! Quiz finalizado.");
        }
    }

    @Transactional(readOnly = true)
    public QuizSessionStateDTO getSessionState(Long sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // ✅ Forçar carregamento de todas as relações
        session.getQuestions().size();
        session.getQuestions().forEach(q -> q.getAnswers().size());

        // 🔥 CRÍTICO: Garantir ordenação por dificuldade
        sortQuestionsByDifficulty(session.getQuestions());

        User currentUser = getCurrentUser();
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to session");
        }

        return convertToStateDTO(session);
    }

    @Transactional(readOnly = true)
    public List<QuizSessionResultDTO> getUserQuizHistory() {
        try {
            User currentUser = getCurrentUser();

            // 🔧 ATUALIZADO: Busca sessões finalizadas (completas ou interrompidas)
            List<QuizSession> finishedSessions = quizSessionRepository.findByUserOrderByCreatedAtDesc(currentUser)
                    .stream()
                    .filter(session -> session.getStatus().isFinished())
                    .sorted((s1, s2) -> s2.getScore().compareTo(s1.getScore())) // Ordena por score DESC
                    .collect(Collectors.toList());

            return finishedSessions.stream()
                    .map(this::convertToResultDTO)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("❌ Service error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveScoreToDatabase(QuizSession session) {
        Score score = new Score();
        score.setUser(session.getUser());
        score.setPoints(session.getScore());
        scoreRepository.save(score);

        System.out.println("💾 Score salvo no banco: " + session.getScore() + " pontos");
    }

    // 🆕 NOVO: Cria resultado para quiz completado
    private QuizSessionResultDTO createCompletedResult(QuizSession session) {
        double completionRate = 100.0; // Quiz completado = 100%

        return new QuizSessionResultDTO(
                session.getId(),
                session.getScore(),
                session.getQuestions().size(),
                true, // wasCompleted = true
                session.getCreatedAt(),
                session.getFinishedAt(),
                "Parabéns! Você completou todo o quiz com " + session.getScore() + " pontos!",
                session.getStatus().getValue(),        // Status
                session.getStatus().getDescription(),  // Descrição do status
                completionRate                         // 100% de completude
        );
    }

    // 🆕 NOVO: Cria resultado para quiz interrompido
    private QuizSessionResultDTO createInterruptedResult(QuizSession session, String message) {
        // Calcula percentual de completude baseado no progresso
        double completionRate = session.getQuestions().isEmpty() ? 0.0 :
                (double) session.getCurrentQuestionIndex() / session.getQuestions().size() * 100.0;

        return new QuizSessionResultDTO(
                session.getId(),
                session.getScore(),
                session.getQuestions().size(),
                false, // wasCompleted = false
                session.getCreatedAt(),
                session.getFinishedAt(),
                message,
                session.getStatus().getValue(),        // Status
                session.getStatus().getDescription(),  // Descrição do status
                Math.round(completionRate * 100.0) / 100.0 // Taxa de completude
        );
    }

    private QuizSessionStateDTO convertToStateDTO(QuizSession session) {
        QuestionForSessionDTO currentQuestion = null;

        if (session.getCurrentQuestion() != null) {
            Question q = session.getCurrentQuestion();
            List<AnswerForSessionDTO> answers = q.getAnswers().stream()
                    .map(answer -> new AnswerForSessionDTO(answer.getId(), answer.getContent()))
                    .toList();
            currentQuestion = new QuestionForSessionDTO(q.getId(), q.getContent(), answers);
        }

        return new QuizSessionStateDTO(
                session.getId(),
                session.getCurrentQuestionIndex(),
                session.getQuestions().size(),
                session.getScore(),
                session.getStatus() == SessionStatus.IN_PROGRESS, // isActive baseado no status
                session.getStatus() == SessionStatus.COMPLETED,   // isCompleted baseado no status
                currentQuestion,
                session.getCreatedAt(),
                session.getFinishedAt(),
                session.getStatus().getValue(),        // 🆕 Status como string
                session.getStatus().getDescription()   // 🆕 Descrição do status
        );
    }

    private QuizSessionResultDTO convertToResultDTO(QuizSession session) {
        // 🆕 NOVO: Usa o SessionStatus para determinar se foi completado
        boolean wasCompleted = session.getStatus() == SessionStatus.COMPLETED;

        String message;
        switch (session.getStatus()) {
            case COMPLETED -> message = "Quiz completado com sucesso! Score: " + session.getScore();
            case INTERRUPTED -> message = "Quiz interrompido. Score final: " + session.getScore();
            case IN_PROGRESS -> message = "Quiz em andamento...";
            default -> message = "Status desconhecido";
        }

        // 🆕 NOVO: Calcula taxa de completude
        double completionRate = session.getQuestions().isEmpty() ? 0.0 :
                (double) session.getCurrentQuestionIndex() / session.getQuestions().size() * 100.0;

        return new QuizSessionResultDTO(
                session.getId(),
                session.getScore(),
                session.getQuestions().size(),
                wasCompleted,
                session.getCreatedAt(),
                session.getFinishedAt(),
                message,
                session.getStatus().getValue(),           // 🆕 Status como string
                session.getStatus().getDescription(),     // 🆕 Descrição do status
                Math.round(completionRate * 100.0) / 100.0 // 🆕 Taxa de completude
        );
    }
    private void addQuestions(List<Question> target, List<Question> source, int count) {
        for (int i = 0; i < count && !source.isEmpty(); i++) {
            target.add(source.remove(0)); // Remove da source para não repetir se precisarmos preencher dps
        }
    }

    private void sortQuestionsByDifficulty(List<Question> questions) {
        questions.sort((q1, q2) -> {
            int d1 = getDifficultyWeight(q1.getDifficulty());
            int d2 = getDifficultyWeight(q2.getDifficulty());
            // Se dificuldade igual, desempata por ID para consistência absoluta
            if (d1 == d2) {
                return q1.getId().compareTo(q2.getId());
            }
            return Integer.compare(d1, d2);
        });
    }

    private int getDifficultyWeight(Difficulty difficulty) {
        if (difficulty == null) return 0; // Trata null como mais fácil que easy
        return switch (difficulty) {
            case EASY -> 1;
            case MEDIUM -> 2;
            case HARD -> 3;
        };
    }
}