INSERT INTO roles(name) VALUES('USER');
INSERT INTO roles(name) VALUES('MODERATOR');
INSERT INTO roles(name) VALUES('ADMIN');

INSERT INTO users(id, username, email, password, auth_provider)
VALUES(100,'admin1', 'admin@admin.com', '$2a$10$YyK.hHmRuGALAF22./9bYOhSnoAN56V38Btz5kZQD1a3xd.RbdXeG', 'LOCAL');

INSERT INTO user_roles(user_id, role_id)
VALUES(100, 3);


INSERT INTO daily_content (id, date_processed)
VALUES (1, '2026-03-07');

INSERT INTO translations (id, en, ro, es, de, fr)
VALUES (
    1,
    'Battle of Exampletown',
    'Batalia de la Exampletown',
    'Batalla de Exampletown',
    'Schlacht von Exampletown',
    'Bataille d''Exampletown'
     );

INSERT INTO translations (id, en, ro, es, de, fr)
VALUES (
    2,
    'A short narrative in English about the event.',
    'O naratiune scurta in romana despre eveniment.',
    'Una narrativa corta en espanol sobre el evento.',
    'Eine kurze Erzahlung pe Deutsch uber das Ereignis.',
    'Un court recit en francais sur l''evenement.'
    );

INSERT INTO events (
    id,
    daily_content_id,
    category,
    title_translations_id,
    narrative_translations_id,
    event_date,
    impact_score,
    source_url,
    page_views_30d
)
VALUES (
    1,
    1,
    'WAR_CONFLICT',
    1,
    2,
    '1916-08-15',
    82.5,
    'https://example.com/events/battle-of-exampletown',
    15320
    );

INSERT INTO event_gallery (event_id, image_url)
VALUES
    (1, 'https://cdn.example.com/images/exampletown/1.jpg'),
    (1, 'https://cdn.example.com/images/exampletown/2.jpg');