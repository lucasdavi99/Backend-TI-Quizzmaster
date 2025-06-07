-- Migração para adicionar CASCADE DELETE nas chaves estrangeiras
-- Resolve o problema de exclusão de usuário com quiz_sessions relacionadas

-- 1. Remove a constraint atual (se existir)
ALTER TABLE quiz_session DROP CONSTRAINT IF EXISTS fk3vxr0hkfy3y0f00o21f9m5l5l;

-- 2. Remove outras possíveis constraints de user_id
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    -- Busca todas as constraints de FK na coluna user_id da tabela quiz_session
    FOR constraint_name IN
        SELECT tc.constraint_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
            ON tc.constraint_name = kcu.constraint_name
        WHERE tc.table_name = 'quiz_session'
            AND kcu.column_name = 'user_id'
            AND tc.constraint_type = 'FOREIGN KEY'
    LOOP
        EXECUTE 'ALTER TABLE quiz_session DROP CONSTRAINT IF EXISTS ' || constraint_name;
        RAISE NOTICE 'Removida constraint: %', constraint_name;
    END LOOP;
END $$;

-- 3. Adiciona nova constraint com CASCADE DELETE
ALTER TABLE quiz_session
ADD CONSTRAINT fk_quiz_session_user
FOREIGN KEY (user_id) REFERENCES users(id)
ON DELETE CASCADE ON UPDATE CASCADE;

-- 4. Faz o mesmo para a tabela quiz_session_questions se existir
ALTER TABLE quiz_session_questions DROP CONSTRAINT IF EXISTS fk_quiz_session_questions_session;
ALTER TABLE quiz_session_questions
ADD CONSTRAINT fk_quiz_session_questions_session
FOREIGN KEY (quiz_session_id) REFERENCES quiz_session(id)
ON DELETE CASCADE ON UPDATE CASCADE;

-- 5. Verifica se a constraint de score também tem CASCADE (já deveria ter)
ALTER TABLE score DROP CONSTRAINT IF EXISTS fk_score_user;
ALTER TABLE score
ADD CONSTRAINT fk_score_user
FOREIGN KEY (user_id) REFERENCES users(id)
ON DELETE CASCADE ON UPDATE CASCADE;

-- Log das mudanças
DO $$
BEGIN
    RAISE NOTICE '✅ CASCADE DELETE configurado para:';
    RAISE NOTICE '   - quiz_session.user_id → users.id';
    RAISE NOTICE '   - quiz_session_questions.quiz_session_id → quiz_session.id';
    RAISE NOTICE '   - score.user_id → users.id';
    RAISE NOTICE '🛡️ Agora é seguro deletar usuários sem erros de FK!';
END $$;