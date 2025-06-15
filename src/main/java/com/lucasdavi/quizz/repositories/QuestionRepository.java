package com.lucasdavi.quizz.repositories;

import com.lucasdavi.quizz.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {


    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.answers ORDER BY q.id")
    List<Question> findAllWithAnswers();

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers WHERE q.id = :id")
    Question findByIdWithAnswers(@Param("id") Long id);

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers WHERE q.id = :id")
    Optional<Question> findByIdWithAnswersOptional(@Param("id") Long id);

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.answers WHERE LOWER(q.content) LIKE LOWER(CONCAT('%', :content, '%'))")
    List<Question> findByContentContainingIgnoreCase(@Param("content") String content);

    @Query("SELECT COUNT(q) FROM Question q")
    long countAllQuestions();

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.answers a WHERE EXISTS (SELECT 1 FROM Answer ans WHERE ans.question = q AND ans.isCorrect = true)")
    List<Question> findQuestionsWithCorrectAnswers();

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.answers WHERE NOT EXISTS (SELECT 1 FROM Answer a WHERE a.question = q AND a.isCorrect = true)")
    List<Question> findQuestionsWithoutCorrectAnswers();
}