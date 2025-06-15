package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.dtos.*;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

        Optional<QuizSession> activeSession = quizSessionRepository.findActiveSessionByUser(currentUser);
        if (activeSession.isPresent()) {
            QuizSession previousSession = activeSession.get();
            System.out.println("⚠️ Finalizando sessão anterior inativa - ID: " + previousSession.getId());

            previousSession.setIsActive(false);
            previousSession.setFinishedAt(LocalDateTime.now());
            quizSessionRepository.save(previousSession);

            System.out.println("✅ Sessão anterior finalizada automaticamente");
        }

        List<Question> allQuestions = questionRepository.findAll();
        if (allQuestions.size() < dto.numberOfQuestions()) {
            throw new RuntimeException("Not enough questions available");
        }

        Collections.shuffle(allQuestions);
        List<Question> selectedQuestions = allQuestions.subList(0, dto.numberOfQuestions());

        selectedQuestions.sort((q1, q2) -> q1.getId().compareTo(q2.getId()));

        // Cria nova sessão
        QuizSession session = new QuizSession();
        session.setUser(currentUser);
        session.setQuestions(selectedQuestions);
        session.setCurrentQuestionIndex(0);
        session.setScore(0);
        session.setIsActive(true);

        QuizSession savedSession = quizSessionRepository.save(session);
        System.out.println("🎮 Nova sessão criada - ID: " + savedSession.getId());

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

        activeSessions.forEach(session -> {
            session.setIsActive(false);
            session.setFinishedAt(LocalDateTime.now());
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

    private QuizSession getSessionWithOrderedQuestions(Long sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz session not found"));

        List<Question> orderedQuestions = session.getQuestions()
                .stream()
                .sorted((q1, q2) -> q1.getId().compareTo(q2.getId()))
                .collect(Collectors.toList());

        session.setQuestions(orderedQuestions);
        return session;
    }

    @Transactional
    public QuizSessionResultDTO answerQuestion(Long sessionId, AnswerQuestionDTO dto) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        // ✅ Forçar carregamento de todas as relações necessárias
        session.getQuestions().size(); // Carregar questions
        session.getQuestions().forEach(q -> q.getAnswers().size()); // Carregar answers de cada question
        
        // Verificar se a sessão pertence ao usuário atual
        User currentUser = getCurrentUser();
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to session");
        }

        if (!session.getIsActive()) {
            throw new RuntimeException("Session is not active");
        }

        Question currentQuestion = session.getCurrentQuestion();
        if (currentQuestion == null) {
            throw new RuntimeException("No current question available");
        }

        // Forçar carregamento das answers da pergunta atual
        currentQuestion.getAnswers().size();

        Answer selectedAnswer = answerRepository.findById(dto.answerId())
                .orElseThrow(() -> new EntityNotFoundException("Answer not found"));

        System.out.println("🔍 DEBUG AFTER FIX:");
        System.out.println("   Current Question ID: " + currentQuestion.getId());
        System.out.println("   Current Question Content: " + currentQuestion.getContent().substring(0, Math.min(50, currentQuestion.getContent().length())));
        System.out.println("   Selected Answer ID: " + selectedAnswer.getId());
        System.out.println("   Answer's Question ID: " + selectedAnswer.getQuestion().getId());
        System.out.println("   Session ID: " + session.getId());
        System.out.println("   Current Index: " + session.getCurrentQuestionIndex());

        if (!selectedAnswer.getQuestion().getId().equals(currentQuestion.getId())) {
            throw new RuntimeException("Answer does not belong to current question");
        }

        boolean isCorrect = selectedAnswer.getIsCorrect();
        if (isCorrect) {
            session.setScore(session.getScore() + 10);

            if (session.hasNextQuestion()) {
                session.moveToNextQuestion();
                quizSessionRepository.save(session);
                return null; // Continua o quiz
            } else {
                session.moveToNextQuestion(); // ✅ Incrementa para indicar completude
                session.finishSession();
                quizSessionRepository.save(session);
                saveScoreToDatabase(session);
                return createSuccessResult(session, "Parabéns! Você completou todo o quiz!");
            }
        } else {
            session.finishSession();
            quizSessionRepository.save(session);
            saveScoreToDatabase(session);
            return createFailureResult(session, "Resposta incorreta! Fim do jogo.");
        }
    }

    @Transactional(readOnly = true)
    public QuizSessionStateDTO getSessionState(Long sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        // ✅ Forçar carregamento de todas as relações
        session.getQuestions().size();
        session.getQuestions().forEach(q -> q.getAnswers().size());
        
        User currentUser = getCurrentUser();
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to session");
        }

        return convertToStateDTO(session);
    }

    public List<QuizSessionResultDTO> getUserQuizHistory() {
        User currentUser = getCurrentUser();
        List<QuizSession> completedSessions = quizSessionRepository.findCompletedSessionsByUserOrderByScoreDesc(currentUser);

        return completedSessions.stream()
                .map(this::convertToResultDTO)
                .toList();
    }

    private void saveScoreToDatabase(QuizSession session) {
        Score score = new Score();
        score.setUser(session.getUser());
        score.setPoints(session.getScore());
        scoreRepository.save(score);
    }

    private QuizSessionResultDTO createSuccessResult(QuizSession session, String message) {
        return new QuizSessionResultDTO(
                session.getId(),
                session.getScore(),
                session.getQuestions().size(),
                true,
                session.getCreatedAt(),
                session.getFinishedAt(),
                message
        );
    }

    private QuizSessionResultDTO createFailureResult(QuizSession session, String message) {
        return new QuizSessionResultDTO(
                session.getId(),
                session.getScore(),
                session.getQuestions().size(),
                false,
                session.getCreatedAt(),
                session.getFinishedAt(),
                message
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
                session.getIsActive(),
                session.isCompleted(),
                currentQuestion,
                session.getCreatedAt(),
                session.getFinishedAt()
        );
    }

    private QuizSessionResultDTO convertToResultDTO(QuizSession session) {
        boolean wasCompleted = session.getCurrentQuestionIndex() >= session.getQuestions().size()
                && session.getFinishedAt() != null;

        String message;
        if (wasCompleted) {
            message = "Quiz completado com sucesso!";
        } else if (session.getFinishedAt() != null) {
            message = "Quiz interrompido por resposta incorreta";
        } else {
            message = "Quiz em andamento";
        }

        return new QuizSessionResultDTO(
                session.getId(),
                session.getScore(),
                session.getQuestions().size(),
                wasCompleted,
                session.getCreatedAt(),
                session.getFinishedAt(),
                message
        );
    }
}