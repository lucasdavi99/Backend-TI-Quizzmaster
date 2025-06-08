-- Criação da tabela quiz_session
CREATE TABLE quiz_session (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGSERIAL NOT NULL,
    current_question_index INTEGER DEFAULT 0,
    score INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Criação da tabela de relacionamento quiz_session_questions
CREATE TABLE quiz_session_questions (
    quiz_session_id BIGSERIAL NOT NULL,
    question_id BIGSERIAL NOT NULL,
    PRIMARY KEY (quiz_session_id, question_id),
    FOREIGN KEY (quiz_session_id) REFERENCES quiz_session(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
);

-- Criação de índices para melhor performance
CREATE INDEX idx_quiz_session_user_active ON quiz_session(user_id, is_active);
CREATE INDEX idx_quiz_session_created_at ON quiz_session(created_at);
CREATE INDEX idx_quiz_session_score ON quiz_session(score DESC);