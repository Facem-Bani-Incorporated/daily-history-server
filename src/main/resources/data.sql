INSERT INTO roles(name) VALUES('USER');
INSERT INTO roles(name) VALUES('MODERATOR');
INSERT INTO roles(name) VALUES('ADMIN');

INSERT INTO users(id, username, email, password, auth_provider)
VALUES(100,'admin1', 'admin@admin.com', '$2a$10$YyK.hHmRuGALAF22./9bYOhSnoAN56V38Btz5kZQD1a3xd.RbdXeG', 'LOCAL');

INSERT INTO user_roles(user_id, role_id)
VALUES(100, 3);