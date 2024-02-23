package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.models.Score;
import com.lucasdavi.quizz.models.User;
import com.lucasdavi.quizz.repositories.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ScoreServiceTest {

    @InjectMocks
    private ScoreService scoreService;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private UserService userService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);

        // Mock the Authentication object
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("testUser");

        // Set the mock Authentication in the SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
public void testSaveScore() {
    // Arrange
    Integer points = 10;
    String username = "testUser";
    User user = new User();
    user.setUsername(username);
    Score score = new Score();
    score.setUser(user);
    score.setPoints(points);

    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getName()).thenReturn(username);
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(userService.findByUsername(username)).thenReturn(user);
    when(scoreRepository.save(any(Score.class))).thenReturn(score);

    // Act
    Score result = scoreService.saveScore(points);

    // Assert
    assertEquals(points, result.getPoints());
    assertEquals(user, result.getUser());

    verify(auth, times(1)).getName();
    verify(userService, times(1)).findByUsername(username);
    verify(scoreRepository, times(1)).save(any(Score.class));
}
}