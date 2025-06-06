package com.lucasdavi.quizz.repositories;

import com.lucasdavi.quizz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository extends JpaRepository<User, Long> {

    UserDetails findByUsername(String username);

    // 🆕 NOVO: Método para buscar por email (se necessário)
    User findByEmail(String email);

    // 🆕 NOVO: Verificar se email ou username existem
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
