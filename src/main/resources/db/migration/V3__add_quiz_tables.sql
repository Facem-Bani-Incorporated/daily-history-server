CREATE TABLE quizzes (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL UNIQUE REFERENCES events(id)
);

CREATE TABLE quiz_questions (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id),
    question_key VARCHAR(20) NOT NULL,
    language VARCHAR(5) NOT NULL,
    question_text TEXT NOT NULL,
    explanation TEXT,
    correct_option_id VARCHAR(10) NOT NULL
);

CREATE INDEX idx_quiz_lang ON quiz_questions(quiz_id, language);

CREATE TABLE quiz_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES quiz_questions(id),
    option_id VARCHAR(10) NOT NULL,
    text TEXT NOT NULL
);

CREATE TABLE user_quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    correct_answers INT NOT NULL,
    total_questions INT NOT NULL,
    xp_earned INT NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_event_quiz UNIQUE (user_id, event_id)
);

CREATE INDEX idx_attempts_user ON user_quiz_attempts(user_id);

CREATE TABLE user_quiz_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL REFERENCES user_quiz_attempts(id),
    question_key VARCHAR(20) NOT NULL,
    selected_option_id VARCHAR(10) NOT NULL,
    is_correct BOOLEAN NOT NULL
);
