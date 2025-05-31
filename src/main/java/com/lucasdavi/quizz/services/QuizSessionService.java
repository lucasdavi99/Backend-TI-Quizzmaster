package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.dtos.*;
import com.lucasdavi.quizz.exceptions.EntityNotFoundException;
import com.lucasdavi.quizz.models.*;
import com.lucasdavi.quizz.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

        // Verifica se já existe uma sessão ativa para o usuário
        Optional<QuizSession> activeSession = quizSessionRepository.findActiveSessionByUser(currentUser);
        if (activeSession.isPresent()) {
            throw new RuntimeException("User already has an active quiz session");
        }

        // Busca perguntas aleatórias
        List<Question> allQuestions = questionRepository.findAll();
        if (allQuestions.size() < dto.numberOfQuestions()) {
            throw new RuntimeException("Not enough questions available");
        }

        Collections.shuffle(allQuestions);
        List<Question> selectedQuestions = allQuestions.subList(0, dto.numberOfQuestions());

        // Cria nova sessão
        QuizSession session = new QuizSession();
        session.setUser(currentUser);
        session.setQuestions(selectedQuestions);
        session.setCurrentQuestionIndex(0);
        session.setScore(0);
        session.setIsActive(true);

        QuizSession savedSession = quizSessionRepository.save(session);
        return convertToStateDTO(savedSession);
    }

    @Transactional
    public QuizSessionResultDTO answerQuestion(Long sessionId, AnswerQuestionDTO dto) {
        User currentUser = getCurrentUser();

        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz session not found"));

        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied to this quiz session");
        }

        if (!session.getIsActive()) {
            throw new RuntimeException("Quiz session is not active");
        }

        Question currentQuestion = session.getCurrentQuestion();
        if (currentQuestion == null) {
            throw new RuntimeException("No current question available");
        }

        Answer selectedAnswer = answerRepository.findById(dto.answerId())
                .orElseThrow(() -> new EntityNotFoundException("Answer not found"));

        // Verifica se a resposta pertence à pergunta atual
        if (!selectedAnswer.getQuestion().getId().equals(currentQuestion.getId())) {
            throw new RuntimeException("Answer does not belong to current question");
        }

        boolean isCorrect = selectedAnswer.getIsCorrect();

        if (isCorrect) {
            // Resposta correta: aumenta pontuação e vai para próxima pergunta
            session.setScore(session.getScore() + 10);

            if (session.hasNextQuestion()) {
                session.moveToNextQuestion();
                quizSessionRepository.save(session);
                // Retorna null para indicar que a sessão continua
                return null;
            } else {
                // Última pergunta respondida corretamente
                session.finishSession();
                quizSessionRepository.save(session);
                saveScoreToDatabase(session);
                return createSuccessResult(session, "Parabéns! Você completou todo o quiz!");
            }
        } else {
            // Resposta incorreta: finaliza sessão
            session.finishSession();
            quizSessionRepository.save(session);
            saveScoreToDatabase(session);
            return createFailureResult(session, "Resposta incorreta! Fim do jogo.");
        }
    }

    public QuizSessionStateDTO getSessionState(Long sessionId) {
        User currentUser = getCurrentUser();

        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz session not found"));

        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied to this quiz session");
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
        return new QuizSessionResultDTO(
                session.getId(),
                session.getScore(),
                session.getQuestions().size(),
                session.getCurrentQuestionIndex() >= session.getQuestions().size(),
                session.getCreatedAt(),
                session.getFinishedAt(),
                session.getCurrentQuestionIndex() >= session.getQuestions().size() ?
                        "Quiz completado com sucesso!" : "Quiz interrompido"
        );
    }
}
