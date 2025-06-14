CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGSERIAL NOT NULL,
    token VARCHAR(6) NOT NULL, -- Código de 6 dígitos
    email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Índices para performance e segurança
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_email ON password_reset_tokens(email);
CREATE INDEX idx_password_reset_expires ON password_reset_tokens(expires_at);
CREATE INDEX idx_password_reset_user_active ON password_reset_tokens(user_id, is_active);

-- Limpeza automática de tokens expirados (opcional)
CREATE INDEX idx_password_reset_cleanup ON password_reset_tokens(expires_at, is_active);