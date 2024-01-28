package com.lucasdavi.quizz.repositories;

import com.lucasdavi.quizz.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long>{
}
