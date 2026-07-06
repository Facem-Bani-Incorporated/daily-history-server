CREATE TABLE friendships (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL REFERENCES users(id),
    addressee_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    responded_at TIMESTAMP,
    CONSTRAINT uq_friendship_pair UNIQUE (requester_id, addressee_id),
    CONSTRAINT chk_friendship_not_self CHECK (requester_id <> addressee_id)
);

CREATE INDEX idx_friendship_addressee ON friendships(addressee_id, status);
CREATE INDEX idx_friendship_requester ON friendships(requester_id, status);
