package com.lucasdavi.quizz.repositories;

import com.lucasdavi.quizz.models.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long>{
}
