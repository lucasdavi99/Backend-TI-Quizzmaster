package com.lucasdavi.quizz.models;
import com.lucasdavi.quizz.models.Question;
import com.lucasdavi.quizz.models.User;
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

    // 🔧 CORREÇÃO: Usar LAZY loading e OrderBy para manter ordem consistente
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

    public void finishSession() {
        this.isActive = false;
        this.finishedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return currentQuestionIndex >= questions.size() || !isActive;
    }
}