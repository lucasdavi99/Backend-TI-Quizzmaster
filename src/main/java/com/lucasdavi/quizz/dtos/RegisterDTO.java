package com.lucasdavi.quizz.dtos;

import com.lucasdavi.quizz.enums.UserRole;

public record RegisterDTO(String login, String password, String email) {
}
