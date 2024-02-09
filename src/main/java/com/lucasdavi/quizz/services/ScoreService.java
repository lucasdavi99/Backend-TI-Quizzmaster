package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.models.Answer;
import com.lucasdavi.quizz.models.Score;
import com.lucasdavi.quizz.models.User;
import com.lucasdavi.quizz.repositories.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreService {

    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private AnswerService answerService;

    public Score saveScore(Score score, Long answerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails currentUser)) {
            throw new RuntimeException("User not authenticated");
        }

        Answer answer = answerService.getAnswerById(answerId).orElseThrow(() -> new RuntimeException("Answer not found"));

        score.setUser((User) currentUser);
        score.setPoints(answer.getIsCorrect() ? 10 : 0);

        return this.scoreRepository.save(score);
    }



    public Score getScoreById(Long id) {
        return this.scoreRepository.findById(id).orElse(null);
    }

    public List<Score> getAllScores() {
        return this.scoreRepository.findAll();
    }

    public Score updateScoreById(Long id, Score score) {
        if(score.getPoints() > scoreRepository.getReferenceById(id).getPoints()){
            score.setPoints(score.getPoints());
            return this.scoreRepository.save(score);
        }
        return null;
    }
}
