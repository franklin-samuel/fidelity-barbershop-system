INSERT INTO settings (id, haircuts_for_free, created_at, modified_at)
VALUES (
           'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'::uuid,
           10,
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       )
    ON CONFLICT (id) DO NOTHING;


INSERT INTO users (id, created_at, modified_at, email, name, password)
VALUES (
        'fc806316-cf9f-4fdb-9a17-11af3ce0b063',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        'admin@email.com',
        'admin',
        '$2a$12$tvwttM44OmQHYW8.QJ8GZOOFUZZxbo6zcZZhsLqaopY7a4QsKoDim'
       )
ON CONFLICT (email) DO NOTHING;