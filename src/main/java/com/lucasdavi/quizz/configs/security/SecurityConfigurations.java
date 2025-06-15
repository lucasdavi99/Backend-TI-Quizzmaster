package com.lucasdavi.quizz.configs.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    @Autowired
    SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // ========================================
                        // ENDPOINTS PÚBLICOS (sem autenticação) - PRIMEIRO
                        // ========================================
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()

                        // Rankings e estatísticas PÚBLICAS
                        .requestMatchers(HttpMethod.GET, "/api/scores/ranking").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/scores/ranking/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/scores/stats").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/scores/best-scores").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/scores/top-by-user").permitAll()

                        // ========================================
                        // 🔧 ADMIN ENDPOINTS - SEGUNDO (mais específico primeiro)
                        // ========================================

                        // 🔧 CRÍTICO: Perguntas e respostas ADMIN - PRIMEIRO
                        .requestMatchers(HttpMethod.GET, "/api/questions/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/questions").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/questions").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/questions/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/questions/**").hasAuthority("ROLE_ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/answers/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/answers").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/answers").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/answers/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/answers/**").hasAuthority("ROLE_ADMIN")

                        // Gerenciamento de usuários ADMIN
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // Endpoints de limpeza e manutenção ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/quiz-session/cleanup/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/quiz-session/finish-all").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/quiz-session/report/**").hasAuthority("ROLE_ADMIN")

                        // Gerenciamento direto de scores ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/scores").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/scores/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/scores/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/scores").hasAuthority("ROLE_ADMIN")

                        // ========================================
                        // ENDPOINTS PARA USUÁRIOS AUTENTICADOS - TERCEIRO
                        // ========================================

                        // Quiz sessions (jogar)
                        .requestMatchers("/api/quiz-session/start").authenticated()
                        .requestMatchers("/api/quiz-session/*/answer").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/quiz-session/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/quiz-session/history").authenticated()

                        // Estatísticas PESSOAIS
                        .requestMatchers(HttpMethod.GET, "/api/scores/my-stats").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/scores/my-position").authenticated()

                        // ========================================
                        // ENDPOINTS DE PERFIL DE USUÁRIO - QUARTO
                        // ========================================
                        .requestMatchers(HttpMethod.GET, "/api/profile/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/profile/stats").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/profile/username").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/profile/password").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/profile/account").authenticated()

                        // ========================================
                        // NEGAR TUDO O RESTO - ÚLTIMO
                        // ========================================
                        .anyRequest().denyAll())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}