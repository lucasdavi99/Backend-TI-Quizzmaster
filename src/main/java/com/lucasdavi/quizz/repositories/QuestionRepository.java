package com.lucasdavi.quizz.repositories;

import com.lucasdavi.quizz.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Método otimizado que busca Questions com Answers em uma única query
    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.answers")
    List<Question> findAllWithAnswers();

    // Método otimizado para buscar uma question específica com answers
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers WHERE q.id = :id")
    Question findByIdWithAnswers(Long id);
}