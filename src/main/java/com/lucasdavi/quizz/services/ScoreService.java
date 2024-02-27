package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.models.Score;
import com.lucasdavi.quizz.models.User;
import com.lucasdavi.quizz.repositories.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ScoreService {

    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private AnswerService answerService;

    public Score saveScore(Score score) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails currentUser)) {
            throw new RuntimeException("User not authenticated");
        }

        score.setUser((User) currentUser);
        return this.scoreRepository.save(score);
    }



    public Score getScoreById(Long id) {
        return this.scoreRepository.findById(id).orElse(null);
    }

    public List<Score> getAllScores() {
        return this.scoreRepository.findAll();
    }

    public Score updateScoreById(Long id, Score newScoreData) {

        Optional<Score> optionalExistingScore = scoreRepository.findById(id);

        if (optionalExistingScore.isPresent()) {
            Score existingScore = optionalExistingScore.get();


            if (newScoreData.getPoints() > existingScore.getPoints()) {

                existingScore.setPoints(newScoreData.getPoints());

                return scoreRepository.save(existingScore);
            }else {
                throw new RuntimeException("Score not updated");
            }
        }
        return null;
    }

}
