package com.lucasdavi.quizz.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 500)
    private String content;
    @Column(name = "is_correct")
    private Boolean isCorrect = false;

    @ManyToOne
    @JoinColumn(name = "question_id" , nullable = false)
    @JsonBackReference
    private Question question;
}
