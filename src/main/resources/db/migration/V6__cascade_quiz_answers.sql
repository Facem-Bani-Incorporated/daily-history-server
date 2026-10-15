ALTER TABLE user_quiz_answers
    DROP CONSTRAINT user_quiz_answers_attempt_id_fkey,
    ADD CONSTRAINT user_quiz_answers_attempt_id_fkey
        FOREIGN KEY (attempt_id) REFERENCES user_quiz_attempts(id) ON DELETE CASCADE;

CREATE INDEX idx_answers_attempt ON user_quiz_answers(attempt_id);

CREATE INDEX idx_attempts_completed_at ON user_quiz_attempts(completed_at);
