package com.lucasdavi.quizz.services;

import com.lucasdavi.quizz.models.User;
import com.lucasdavi.quizz.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User findByUsername(String username) {
        return (User) this.userRepository.findByUsername(username);
    }
}
