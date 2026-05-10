INSERT INTO roles (name) VALUES ('USER')      ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('MODERATOR') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN')     ON CONFLICT (name) DO NOTHING;

INSERT INTO users (id, username, email, password, auth_provider)
VALUES (1, 'admin1', 'admin@admin.com', '$2a$10$YyK.hHmRuGALAF22./9bYOhSnoAN56V38Btz5kZQD1a3xd.RbdXeG', 'LOCAL')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT 1, id FROM roles WHERE name = 'ADMIN'
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('users', 'id'), GREATEST(2, (SELECT MAX(id) FROM users)));
