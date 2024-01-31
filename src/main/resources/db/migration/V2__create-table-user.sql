-- Criação da tabela User
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Criação da tabela Score
CREATE TABLE score (
    id BIGSERIAL PRIMARY KEY,
    points INTEGER NOT NULL,
    user_id BIGSERIAL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);