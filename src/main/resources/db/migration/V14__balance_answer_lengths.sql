-- =========================================================================================
-- BALANCEAMENTO DE RESPOSTAS (V14)
-- Objetivo: Evitar que a resposta correta seja óbvia pelo tamanho.
-- Estratégia: Encurtar corretas, alongar incorretas.
-- =========================================================================================

-- Q: Memory Leak (HARD)
-- Encurtar Correta
UPDATE answer 
SET content = 'Objetos obsoletos mantidos em memória por referências não intencionais' 
WHERE content LIKE 'Objetos não são mais utilizados pela aplicação%';

-- Alongar Incorreta
UPDATE answer 
SET content = 'Falha crítica na alocação de memória Heap devido à fragmentação excessiva do espaço endereçável virtual' 
WHERE content LIKE 'Quando uma variável é declarada mas nunca inicializada';


-- Q: Pipelining (HARD)
-- Encurtar Correta
UPDATE answer 
SET content = 'Execução simultânea de instruções em diferentes estágios de processamento' 
WHERE content LIKE 'Técnica para executar múltiplas instruções simultaneamente%';

-- Alongar Incorreta
UPDATE answer 
SET content = 'Técnica de roteamento dinâmico para otimizar o fluxo de pacotes em redes de alta latência e jitter' 
WHERE content LIKE 'Um protocolo de roteamento';


-- Q: Man-in-the-Middle (HARD)
-- Encurtar Correta
UPDATE answer 
SET content = 'Interceptação e adulteração secreta da comunicação entre duas partes' 
WHERE content LIKE 'Ataque onde o invasor intercepta e possivelmente altera%';

-- Alongar Incorreta
UPDATE answer 
SET content = 'Injeção de comandos DML maliciosos em campos de entrada para manipular consultas ao banco de dados' 
WHERE content LIKE 'Injeção de código SQL';


-- Q: Criptografia Assimétrica (HARD)
-- Encurtar Correta
UPDATE answer 
SET content = 'Uso de pares de chaves distintas (Pública/Privada) para cifragem' 
WHERE content LIKE 'Assimétrica usa um par de chaves%';

-- Alongar Incorreta
UPDATE answer 
SET content = 'Uso de algoritmo de hash unidirecional para verificar integridade de arquivos grandes e assinaturas digitais' 
WHERE content LIKE 'Simétrica é usada apenas para senhas%';


-- Q: Injeção de Dependência (MEDIUM)
-- Encurtar Correta
UPDATE answer 
SET content = 'Fornecimento externo de dependências a um objeto em vez de criação interna' 
WHERE content LIKE 'Um padrão onde as dependências de um objeto são fornecidas%';

-- Alongar Incorreta
UPDATE answer 
SET content = 'Vulnerabilidade crítica onde código malicioso (SQL/Script) é inserido através de inputs não sanitizados' 
WHERE content LIKE 'Uma vulnerabilidade de segurança onde código malicioso é injetado%';


-- Q: Load Balancer (MEDIUM)
-- Encurtar Correta
UPDATE answer 
SET content = 'Distribuição de tráfego entre múltiplos servidores para otimizar recursos' 
WHERE content LIKE 'Distribuir o tráfego de rede ou aplicação entre vários servidores';

-- Alongar Incorreta
UPDATE answer 
SET content = 'Compressão de arquivos estáticos em tempo real para reduzir o consumo de banda' 
WHERE content LIKE 'Comprimir arquivos para economizar banda';
UPDATE answer 
SET content = 'Gerenciamento automatizado de backups incrementais e snapshots de banco de dados' 
WHERE content LIKE 'Armazenar backups de banco de dados';


-- Q: Teorema CAP (HARD)
-- Alongar Incorreta
UPDATE answer 
SET content = 'Confiabilidade, Autenticidade, Performance e Escalabilidade Horizontal' 
WHERE content LIKE 'Confiabilidade, Autenticidade e Performance';
