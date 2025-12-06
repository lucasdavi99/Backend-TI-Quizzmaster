-- Perguntas Fáceis (EASY)
INSERT INTO question (content, difficulty) VALUES 
('Qual componente é considerado o "cérebro" do computador?', 'EASY'),
('O que significa a sigla "RAM" em computação?', 'EASY'),
('Qual dispositivo é usado para entrada de dados em um computador?', 'EASY');

-- Respostas para Perguntas Fáceis
-- Q: Cérebro do computador
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'CPU (Unidade Central de Processamento)', true, id FROM question WHERE content = 'Qual componente é considerado o "cérebro" do computador?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'HD (Disco Rígido)', false, id FROM question WHERE content = 'Qual componente é considerado o "cérebro" do computador?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Monitor', false, id FROM question WHERE content = 'Qual componente é considerado o "cérebro" do computador?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Teclado', false, id FROM question WHERE content = 'Qual componente é considerado o "cérebro" do computador?';

-- Q: RAM
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Random Access Memory', true, id FROM question WHERE content = 'O que significa a sigla "RAM" em computação?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Read Access Memory', false, id FROM question WHERE content = 'O que significa a sigla "RAM" em computação?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Run Access Memory', false, id FROM question WHERE content = 'O que significa a sigla "RAM" em computação?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Real Access Memory', false, id FROM question WHERE content = 'O que significa a sigla "RAM" em computação?';

-- Q: Dispositivo de entrada
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Mouse', true, id FROM question WHERE content = 'Qual dispositivo é usado para entrada de dados em um computador?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Impressora', false, id FROM question WHERE content = 'Qual dispositivo é usado para entrada de dados em um computador?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Monitor', false, id FROM question WHERE content = 'Qual dispositivo é usado para entrada de dados em um computador?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Caixa de Som', false, id FROM question WHERE content = 'Qual dispositivo é usado para entrada de dados em um computador?';


-- Perguntas Médias (MEDIUM)
INSERT INTO question (content, difficulty) VALUES 
('Qual paradigma de programação utiliza classes e objetos?', 'MEDIUM'),
('O que é "Docker" no contexto de desenvolvimento de software?', 'MEDIUM'),
('Qual o comando SQL para remover uma tabela do banco de dados?', 'MEDIUM');

-- Respostas para Perguntas Médias
-- Q: POO
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Programação Orientada a Objetos (POO)', true, id FROM question WHERE content = 'Qual paradigma de programação utiliza classes e objetos?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Programação Funcional', false, id FROM question WHERE content = 'Qual paradigma de programação utiliza classes e objetos?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Programação Estruturada', false, id FROM question WHERE content = 'Qual paradigma de programação utiliza classes e objetos?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Programação Lógica', false, id FROM question WHERE content = 'Qual paradigma de programação utiliza classes e objetos?';

-- Q: Docker
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Plataforma de containerização', true, id FROM question WHERE content = 'O que é "Docker" no contexto de desenvolvimento de software?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Linguagem de programação', false, id FROM question WHERE content = 'O que é "Docker" no contexto de desenvolvimento de software?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Sistema Operacional', false, id FROM question WHERE content = 'O que é "Docker" no contexto de desenvolvimento de software?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Banco de Dados', false, id FROM question WHERE content = 'O que é "Docker" no contexto de desenvolvimento de software?';

-- Q: SQL Drop
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'DROP TABLE', true, id FROM question WHERE content = 'Qual o comando SQL para remover uma tabela do banco de dados?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'DELETE TABLE', false, id FROM question WHERE content = 'Qual o comando SQL para remover uma tabela do banco de dados?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'REMOVE TABLE', false, id FROM question WHERE content = 'Qual o comando SQL para remover uma tabela do banco de dados?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'ERASE TABLE', false, id FROM question WHERE content = 'Qual o comando SQL para remover uma tabela do banco de dados?';


-- Perguntas Difíceis (HARD)
INSERT INTO question (content, difficulty) VALUES 
('Em arquitetura de computadores, o que é "Pipelining"?', 'HARD'),
('Qual a complexidade de tempo média do algoritmo QuickSort?', 'HARD'),
('O que é um ataque "Man-in-the-Middle"?', 'HARD');

-- Respostas para Perguntas Difíceis
-- Q: Pipelining
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Técnica para executar múltiplas instruções simultaneamente em estágios diferentes', true, id FROM question WHERE content = 'Em arquitetura de computadores, o que é "Pipelining"?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Um tipo de cabo de rede de alta velocidade', false, id FROM question WHERE content = 'Em arquitetura de computadores, o que é "Pipelining"?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Um protocolo de roteamento', false, id FROM question WHERE content = 'Em arquitetura de computadores, o que é "Pipelining"?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Um sistema de refrigeração líquida', false, id FROM question WHERE content = 'Em arquitetura de computadores, o que é "Pipelining"?';

-- Q: QuickSort
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'O(n log n)', true, id FROM question WHERE content = 'Qual a complexidade de tempo média do algoritmo QuickSort?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'O(n²)', false, id FROM question WHERE content = 'Qual a complexidade de tempo média do algoritmo QuickSort?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'O(n)', false, id FROM question WHERE content = 'Qual a complexidade de tempo média do algoritmo QuickSort?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'O(1)', false, id FROM question WHERE content = 'Qual a complexidade de tempo média do algoritmo QuickSort?';

-- Q: MitM
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Ataque onde o invasor intercepta e possivelmente altera a comunicação entre duas partes', true, id FROM question WHERE content = 'O que é um ataque "Man-in-the-Middle"?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Ataque de negação de serviço distribuído', false, id FROM question WHERE content = 'O que é um ataque "Man-in-the-Middle"?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Injeção de código SQL', false, id FROM question WHERE content = 'O que é um ataque "Man-in-the-Middle"?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Quebra de senha por força bruta', false, id FROM question WHERE content = 'O que é um ataque "Man-in-the-Middle"?';
