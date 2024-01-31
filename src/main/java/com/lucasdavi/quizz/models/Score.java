package com.lucasdavi.quizz.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "score")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Score implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;
    @Column
    private Integer score;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
