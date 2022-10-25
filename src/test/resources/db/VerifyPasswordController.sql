INSERT INTO T_PASSWORD(id, account_id, value, salt, is_deleted, deleted)
    VALUES
        (100, 100, '12343', 'spicy stuff', false, null),
        (999, 100, '12343', 'hot pepper', true, now());
