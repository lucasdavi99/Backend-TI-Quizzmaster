-- =========================================================================================
-- MELHORIA E EXPANSÃO DO BANCO DE PERGUNTAS (V13)
-- =========================================================================================

-- 1. REESCRITA E RECLASSIFICAÇÃO DE PERGUNTAS EXISTENTES

-- Q: Algoritmo (EASY -> MEDIUM + Texto melhor)
UPDATE question 
SET content = 'No contexto da Ciência da Computação, como podemos definir formalmente um Algoritmo e qual seu papel na resolução de problemas?', 
    difficulty = 'MEDIUM' 
WHERE content = 'O que é um algoritmo?';

-- Q: Git Push (EASY -> MEDIUM + Texto melhor)
UPDATE question 
SET content = 'Ao utilizar o sistema de versionamento Git, qual é a consequência técnica exata da execução do comando "git push" em um repositório configurado?', 
    difficulty = 'MEDIUM' 
WHERE content = 'Qual é a função do comando "git push" no Git?';

-- Q: HTML (EASY -> Texto melhor)
UPDATE question 
SET content = 'O HTML (HyperText Markup Language) é a espinha dorsal da web. Tecnicamente, ele é classificado como:' 
WHERE content = 'O que é o HTML em uma página web?';

-- Q: HTTP vs HTTPS (EASY -> MEDIUM)
UPDATE question SET difficulty = 'MEDIUM' WHERE content = 'Qual é a diferença entre HTTP e HTTPS?';

-- Q: POO (EASY -> MEDIUM)
UPDATE question SET difficulty = 'MEDIUM' WHERE content = 'O que é a programação orientada a objetos?';

-- Q: Firewall (EASY -> MEDIUM)
UPDATE question SET difficulty = 'MEDIUM' WHERE content = 'O que é um firewall?';

-- Q: JS vs Java (EASY -> MEDIUM)
UPDATE question SET difficulty = 'MEDIUM' WHERE content = 'Qual é a diferença entre JavaScript e Java?';

-- Q: BD Relacional (EASY -> MEDIUM)
UPDATE question SET difficulty = 'MEDIUM' WHERE content = 'O que é um banco de dados relacional?';

-- Q: SQL (EASY -> MEDIUM)
UPDATE question SET difficulty = 'MEDIUM' WHERE content = 'O que é SQL e para que é comumente utilizado?';

-- Q: DNS (EASY -> HARD)
UPDATE question SET difficulty = 'HARD' WHERE content = 'O que é um DNS?';

-- Q: Alto/Baixo Nível (EASY -> HARD)
UPDATE question SET difficulty = 'HARD' WHERE content = 'Qual é a diferença entre linguagens de programação de alto nível e baixo nível?';


-- 2. INSERÇÃO DE NOVAS PERGUNTAS (HARD)

-- Q1 HARD: Teorema CAP
INSERT INTO question (content, difficulty) VALUES 
('Em sistemas distribuídos, o Teorema CAP afirma que é impossível garantir simultaneamente quais três propriedades?', 'HARD');
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Consistência, Disponibilidade e Tolerância a Partição', true, id FROM question WHERE content = 'Em sistemas distribuídos, o Teorema CAP afirma que é impossível garantir simultaneamente quais três propriedades?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Confiabilidade, Autenticidade e Performance', false, id FROM question WHERE content = 'Em sistemas distribuídos, o Teorema CAP afirma que é impossível garantir simultaneamente quais três propriedades?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Controle, Acesso e Persistência', false, id FROM question WHERE content = 'Em sistemas distribuídos, o Teorema CAP afirma que é impossível garantir simultaneamente quais três propriedades?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Cache, Assincronismo e Processamento', false, id FROM question WHERE content = 'Em sistemas distribuídos, o Teorema CAP afirma que é impossível garantir simultaneamente quais três propriedades?';

-- Q2 HARD: SOLID (Liskov)
INSERT INTO question (content, difficulty) VALUES 
('No acrônimo SOLID de design orientado a objetos, o que o princípio de Liskov Substitution (L) estabelece?', 'HARD');
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Objetos de uma superclasse devem ser substituíveis por objetos de suas subclasses sem quebrar a aplicação', true, id FROM question WHERE content = 'No acrônimo SOLID de design orientado a objetos, o que o princípio de Liskov Substitution (L) estabelece?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Classes devem ter apenas uma única responsabilidade', false, id FROM question WHERE content = 'No acrônimo SOLID de design orientado a objetos, o que o princípio de Liskov Substitution (L) estabelece?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Interfaces específicas são melhores que uma interface de propósito geral', false, id FROM question WHERE content = 'No acrônimo SOLID de design orientado a objetos, o que o princípio de Liskov Substitution (L) estabelece?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Módulos de alto nível não devem depender de módulos de baixo nível', false, id FROM question WHERE content = 'No acrônimo SOLID de design orientado a objetos, o que o princípio de Liskov Substitution (L) estabelece?';

-- Q3 HARD: Criptografia Assimétrica
INSERT INTO question (content, difficulty) VALUES 
('Qual a principal diferença matemática e funcional entre criptografia Simétrica e Assimétrica?', 'HARD');
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Assimétrica usa um par de chaves (pública/privada), enquanto Simétrica usa uma única chave compartilhada', true, id FROM question WHERE content = 'Qual a principal diferença matemática e funcional entre criptografia Simétrica e Assimétrica?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Simétrica é usada apenas para senhas, enquanto Assimétrica é para arquivos', false, id FROM question WHERE content = 'Qual a principal diferença matemática e funcional entre criptografia Simétrica e Assimétrica?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Assimétrica é mais rápida e menos segura que a Simétrica', false, id FROM question WHERE content = 'Qual a principal diferença matemática e funcional entre criptografia Simétrica e Assimétrica?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Simétrica não utiliza algoritmos matemáticos, apenas substituição de caracteres', false, id FROM question WHERE content = 'Qual a principal diferença matemática e funcional entre criptografia Simétrica e Assimétrica?';

-- Q4 HARD: Memory Leaks
INSERT INTO question (content, difficulty) VALUES 
('Em linguagens com Garbage Collection como Java, o que caracteriza um "Memory Leak"?', 'HARD');
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Objetos não são mais utilizados pela aplicação mas continuam referenciados, impedindo a coleta pelo GC', true, id FROM question WHERE content = 'Em linguagens com Garbage Collection como Java, o que caracteriza um "Memory Leak"?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'O sistema operacional encerra o programa por falta de memória RAM física', false, id FROM question WHERE content = 'Em linguagens com Garbage Collection como Java, o que caracteriza um "Memory Leak"?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Quando uma variável é declarada mas nunca inicializada', false, id FROM question WHERE content = 'Em linguagens com Garbage Collection como Java, o que caracteriza um "Memory Leak"?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Perda de dados ao desligar o computador repentinamente', false, id FROM question WHERE content = 'Em linguagens com Garbage Collection como Java, o que caracteriza um "Memory Leak"?';

-- Q5 HARD: Normalização (3NF)
INSERT INTO question (content, difficulty) VALUES 
('Qual o objetivo principal da Terceira Forma Normal (3NF) em modelagem de dados relacionais?', 'HARD');
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Eliminar dependências transitivas, garantindo que atributos dependam apenas da chave primária', true, id FROM question WHERE content = 'Qual o objetivo principal da Terceira Forma Normal (3NF) em modelagem de dados relacionais?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Garantir que todas as tabelas tenham uma chave primária composta', false, id FROM question WHERE content = 'Qual o objetivo principal da Terceira Forma Normal (3NF) em modelagem de dados relacionais?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Eliminar grupos repetitivos de dados em uma única coluna', false, id FROM question WHERE content = 'Qual o objetivo principal da Terceira Forma Normal (3NF) em modelagem de dados relacionais?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Garantir que não existam tabelas com mais de 10 colunas', false, id FROM question WHERE content = 'Qual o objetivo principal da Terceira Forma Normal (3NF) em modelagem de dados relacionais?';

-- Q6 HARD: REST Idempotência
INSERT INTO question (content, difficulty) VALUES 
('Em uma API RESTful, o que significa dizer que um método HTTP é idempotente?', 'HARD');
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Múltiplas requisições idênticas têm o mesmo efeito no servidor que uma única requisição', true, id FROM question WHERE content = 'Em uma API RESTful, o que significa dizer que um método HTTP é idempotente?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'O método pode ser executado em paralelo sem travamentos', false, id FROM question WHERE content = 'Em uma API RESTful, o que significa dizer que um método HTTP é idempotente?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'O servidor responde instantaneamente sem processamento assíncrono', false, id FROM question WHERE content = 'Em uma API RESTful, o que significa dizer que um método HTTP é idempotente?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'O método nunca retorna erro, mesmo com parâmetros inválidos', false, id FROM question WHERE content = 'Em uma API RESTful, o que significa dizer que um método HTTP é idempotente?';

-- Q7 HARD: Deadlock (Coffman)
INSERT INTO question (content, difficulty) VALUES 
('Quais são as quatro condições necessárias (Coffman conditions) para que ocorra um Deadlock em um sistema operacional?', 'HARD');
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Exclusão Mútua, Posse e Espera, Não Preempção, Espera Circular', true, id FROM question WHERE content = 'Quais são as quatro condições necessárias (Coffman conditions) para que ocorra um Deadlock em um sistema operacional?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Alta CPU, Baixa Memória, Excesso de I/O, Falha de Rede', false, id FROM question WHERE content = 'Quais são as quatro condições necessárias (Coffman conditions) para que ocorra um Deadlock em um sistema operacional?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Latência, Jitter, Perda de Pacotes, Banda Insuficiente', false, id FROM question WHERE content = 'Quais são as quatro condições necessárias (Coffman conditions) para que ocorra um Deadlock em um sistema operacional?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Leitura Suja, Leitura Fantasma, Escrita não Repetível, Perda de Atualização', false, id FROM question WHERE content = 'Quais são as quatro condições necessárias (Coffman conditions) para que ocorra um Deadlock em um sistema operacional?';

-- Q8 HARD: Circuit Breaker
INSERT INTO question (content, difficulty) VALUES 
('Qual o padrão de projeto "Circuit Breaker" e para que serve em arquitetura de microsserviços?', 'HARD');
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Previne falhas em cascata interrompendo chamadas a um serviço que está falhando ou lento', true, id FROM question WHERE content = 'Qual o padrão de projeto "Circuit Breaker" e para que serve em arquitetura de microsserviços?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Distribui a carga de trabalho uniformemente entre vários servidores', false, id FROM question WHERE content = 'Qual o padrão de projeto "Circuit Breaker" e para que serve em arquitetura de microsserviços?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Garante que uma transação seja executada com sucesso ou totalmente revertida', false, id FROM question WHERE content = 'Qual o padrão de projeto "Circuit Breaker" e para que serve em arquitetura de microsserviços?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Monitora o tráfego de rede para detectar intrusões', false, id FROM question WHERE content = 'Qual o padrão de projeto "Circuit Breaker" e para que serve em arquitetura de microsserviços?';


-- 3. INSERÇÃO DE NOVAS PERGUNTAS (MEDIUM)

-- Q9 MEDIUM: Load Balancer
INSERT INTO question (content, difficulty) VALUES 
('Em infraestrutura web, qual é a função primária de um Load Balancer (Balanceador de Carga)?', 'MEDIUM');
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Distribuir o tráfego de rede ou aplicação entre vários servidores', true, id FROM question WHERE content = 'Em infraestrutura web, qual é a função primária de um Load Balancer (Balanceador de Carga)?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Comprimir arquivos para economizar banda', false, id FROM question WHERE content = 'Em infraestrutura web, qual é a função primária de um Load Balancer (Balanceador de Carga)?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Armazenar backups de banco de dados', false, id FROM question WHERE content = 'Em infraestrutura web, qual é a função primária de um Load Balancer (Balanceador de Carga)?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Gerar certificados SSL automaticamente', false, id FROM question WHERE content = 'Em infraestrutura web, qual é a função primária de um Load Balancer (Balanceador de Carga)?';

-- Q10 MEDIUM: TCP vs UDP
INSERT INTO question (content, difficulty) VALUES 
('Qual a principal diferença entre os protocolos de transporte TCP e UDP?', 'MEDIUM');
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'TCP é orientado a conexão e garante entrega; UDP é sem conexão e não garante entrega', true, id FROM question WHERE content = 'Qual a principal diferença entre os protocolos de transporte TCP e UDP?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'UDP é mais lento que o TCP por ser mais seguro', false, id FROM question WHERE content = 'Qual a principal diferença entre os protocolos de transporte TCP e UDP?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'TCP é usado apenas para streaming de vídeo', false, id FROM question WHERE content = 'Qual a principal diferença entre os protocolos de transporte TCP e UDP?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Não há diferença significativa, são apenas marcas diferentes', false, id FROM question WHERE content = 'Qual a principal diferença entre os protocolos de transporte TCP e UDP?';

-- Q11 MEDIUM: Injeção de Dependência
INSERT INTO question (content, difficulty) VALUES 
('O que é Injeção de Dependência (Dependency Injection)?', 'MEDIUM');
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Um padrão onde as dependências de um objeto são fornecidas externamente ao invés de criadas internamente', true, id FROM question WHERE content = 'O que é Injeção de Dependência (Dependency Injection)?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Uma vulnerabilidade de segurança onde código malicioso é injetado no banco de dados', false, id FROM question WHERE content = 'O que é Injeção de Dependência (Dependency Injection)?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Uma técnica para instalar bibliotecas npm automaticamente', false, id FROM question WHERE content = 'O que é Injeção de Dependência (Dependency Injection)?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Um método para conectar dois bancos de dados diferentes', false, id FROM question WHERE content = 'O que é Injeção de Dependência (Dependency Injection)?';

-- Q12 MEDIUM: Docker vs VM
INSERT INTO question (content, difficulty) VALUES 
('Qual a principal diferença arquitetural entre um Container Docker e uma Máquina Virtual (VM)?', 'MEDIUM');
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Containers compartilham o kernel do OS host, enquanto VMs incluem um OS completo (Guest OS)', true, id FROM question WHERE content = 'Qual a principal diferença arquitetural entre um Container Docker e uma Máquina Virtual (VM)?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'VMs são mais leves e iniciam mais rápido que containers', false, id FROM question WHERE content = 'Qual a principal diferença arquitetural entre um Container Docker e uma Máquina Virtual (VM)?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Containers só rodam em Linux, VMs só em Windows', false, id FROM question WHERE content = 'Qual a principal diferença arquitetural entre um Container Docker e uma Máquina Virtual (VM)?';
INSERT INTO answer (content, is_correct, question_id) 
SELECT 'Não é possível rodar bancos de dados em containers', false, id FROM question WHERE content = 'Qual a principal diferença arquitetural entre um Container Docker e uma Máquina Virtual (VM)?';
