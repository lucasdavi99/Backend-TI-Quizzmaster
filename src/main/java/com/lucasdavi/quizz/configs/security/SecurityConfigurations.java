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
                        // ENDPOINTS PÚBLICOS (sem autenticação)
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()

                        // ENDPOINTS EXCLUSIVOS PARA ADMIN
                        // Gerenciamento de perguntas e respostas
                        .requestMatchers(HttpMethod.POST, "/api/questions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/questions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/questions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/answers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/answers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/answers/**").hasRole("ADMIN")

                        // Endpoints de limpeza e manutenção
                        .requestMatchers(HttpMethod.DELETE, "/api/quiz-session/cleanup/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/quiz-session/finish-all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/quiz-session/report/**").hasRole("ADMIN")

                        // Gerenciamento direto de scores (criar/editar scores manualmente)
                        .requestMatchers(HttpMethod.POST, "/api/scores").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/scores/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/scores/**").hasRole("ADMIN")

                        //  ENDPOINTS PARA USUÁRIOS AUTENTICADOS
                        // Quiz sessions (jogar)
                        .requestMatchers("/api/quiz-session/start").authenticated()
                        .requestMatchers("/api/quiz-session/*/answer").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/quiz-session/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/quiz-session/history").authenticated()

                        // Visualização de perguntas (para jogar)
                        .requestMatchers(HttpMethod.GET, "/api/questions").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/questions/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/answers").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/answers/*").authenticated()

                        // Rankings e estatísticas (visualização)
                        .requestMatchers(HttpMethod.GET, "/api/scores/ranking/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/scores/stats").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/scores/my-stats").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/scores/my-position").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/scores/top-by-user").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/scores/best-scores").authenticated()

                        // NEGAR TUDO O RESTO
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
