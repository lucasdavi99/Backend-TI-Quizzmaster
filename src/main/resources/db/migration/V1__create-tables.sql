CREATE TABLE question (
    id BIGSERIAL PRIMARY KEY,
    content VARCHAR(255) NOT NULL
);

CREATE TABLE answer (
    id BIGSERIAL PRIMARY KEY,
    content VARCHAR(255) NOT NULL,
    is_correct BOOLEAN NOT NULL,
    question_id BIGSERIAL,
    FOREIGN KEY (question_id) REFERENCES question(id)
);