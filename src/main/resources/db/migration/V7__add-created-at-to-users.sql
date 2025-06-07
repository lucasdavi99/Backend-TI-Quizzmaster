-- Adiciona campo created_at à tabela users
ALTER TABLE users ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Para usuários existentes, define a data de criação como agora
UPDATE users SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;