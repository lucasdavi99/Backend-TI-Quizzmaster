-- Migração para adicionar campo status à tabela quiz_session
-- V10__add-session-status-enum.sql - VERSÃO UPPERCASE

-- 1. Adiciona a coluna status como VARCHAR (valor default em UPPERCASE)
ALTER TABLE quiz_session
ADD COLUMN status VARCHAR(20) DEFAULT 'IN_PROGRESS';

-- 2. Atualiza registros existentes baseado na lógica atual (valores em UPPERCASE)
UPDATE quiz_session
SET status = CASE
    WHEN is_active = true THEN 'IN_PROGRESS'
    WHEN is_active = false AND finished_at IS NOT NULL AND current_question_index >= (
        SELECT COUNT(*) FROM quiz_session_questions WHERE quiz_session_id = quiz_session.id
    ) THEN 'COMPLETED'
    WHEN is_active = false AND finished_at IS NOT NULL THEN 'INTERRUPTED'
    ELSE 'IN_PROGRESS'
END;

-- 3. Torna a coluna NOT NULL após popular os dados
ALTER TABLE quiz_session
ALTER COLUMN status SET NOT NULL;

-- 4. Adiciona constraint para garantir valores válidos (em UPPERCASE)
ALTER TABLE quiz_session
ADD CONSTRAINT chk_session_status
CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'INTERRUPTED'));

-- 5. Cria índice para performance
CREATE INDEX idx_quiz_session_status ON quiz_session(status);

-- 6. Cria índice composto para consultas comuns
CREATE INDEX idx_quiz_session_user_status ON quiz_session(user_id, status);