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
        // Busca o score existente pelo id
        Optional<Score> optionalExistingScore = scoreRepository.findById(id);

        if (optionalExistingScore.isPresent()) {
            Score existingScore = optionalExistingScore.get();

            // Verifica se os novos pontos são maiores que os atuais
            if (newScoreData.getPoints() > existingScore.getPoints()) {
                // Atualiza os pontos do score existente
                existingScore.setPoints(newScoreData.getPoints());

                // Salva o score existente com os pontos atualizados
                return scoreRepository.save(existingScore);
            }
        }
        // Retorna null ou pode optar por lançar uma exceção se o score não existir ou não necessitar atualização
        return null;
    }

}
