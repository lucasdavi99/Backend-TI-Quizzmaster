package com.lucasdavi.quizz.models;

import com.lucasdavi.quizz.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "quiz_session_questions",
            joinColumns = @JoinColumn(name = "quiz_session_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    @OrderBy("id ASC") // 🚀 FORÇA ORDEM CONSISTENTE
    private List<Question> questions = new ArrayList<>();

    @Column(name = "current_question_index")
    private Integer currentQuestionIndex = 0;

    @Column(name = "score")
    private Integer score = 0;

    // Campo para controlar status da sessão
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    // 🔧 MANTIDO: Para compatibilidade, mas agora deriva do status
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    public Question getCurrentQuestion() {
        if (questions == null || questions.isEmpty()) {
            return null;
        }

        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            Question currentQ = questions.get(currentQuestionIndex);
            System.out.println("🔍 getCurrentQuestion() - Index: " + currentQuestionIndex +
                    ", Question ID: " + currentQ.getId() +
                    ", Content: " + currentQ.getContent().substring(0, Math.min(50, currentQ.getContent().length())) + "...");
            return currentQ;
        }
        return null;
    }

    public boolean hasNextQuestion() {
        return currentQuestionIndex < questions.size() - 1;
    }

    public void moveToNextQuestion() {
        if (hasNextQuestion()) {
            currentQuestionIndex++;
            System.out.println("🔍 moveToNextQuestion() - New Index: " + currentQuestionIndex);
        }
    }

    // Métodos para controlar status
    public void completeSession() {
        this.status = SessionStatus.COMPLETED;
        this.isActive = false;
        this.finishedAt = LocalDateTime.now();
        System.out.println("✅ Sessão COMPLETADA - ID: " + this.id + ", Score: " + this.score);
    }

    public void interruptSession() {
        this.status = SessionStatus.INTERRUPTED;
        this.isActive = false;
        this.finishedAt = LocalDateTime.now();
        System.out.println("❌ Sessão INTERROMPIDA - ID: " + this.id + ", Score: " + this.score);
    }

    // Método genérico que usa o status apropriado
    public void finishSession() {
        // Determina se foi completada ou interrompida baseado no progresso
        if (isFullyCompleted()) {
            completeSession();
        } else {
            interruptSession();
        }
    }

    // Verifica se o quiz foi totalmente completado
    public boolean isFullyCompleted() {
        return currentQuestionIndex >= questions.size();
    }

    // Usa o status para determinar se está completo
    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED;
    }

    // Verifica se foi interrompido
    public boolean isInterrupted() {
        return status == SessionStatus.INTERRUPTED;
    }

    // Verifica se está em progresso
    public boolean isInProgress() {
        return status == SessionStatus.IN_PROGRESS;
    }

    // Verifica se a sessão terminou (completa ou interrompida)
    public boolean isFinished() {
        return status.isFinished();
    }

    // 🔧 COMPATIBILIDADE: Mantém método antigo para não quebrar código existente
    @Deprecated
    public void finishSession(String reason) {
        if ("completed".equalsIgnoreCase(reason)) {
            completeSession();
        } else {
            interruptSession();
        }
    }

    // 🆕 UTILITÁRIO: Para logging e debug
    public String getStatusDescription() {
        return status.getDescription();
    }
}