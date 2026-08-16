ALTER TABLE payment
    ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT,
    ADD COLUMN IF NOT EXISTS updated_by_user_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_payment__created_by_user'
    ) THEN
        ALTER TABLE payment
            ADD CONSTRAINT fk_payment__created_by_user
            FOREIGN KEY (created_by_user_id)
            REFERENCES user_account (id)
            ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_payment__updated_by_user'
    ) THEN
        ALTER TABLE payment
            ADD CONSTRAINT fk_payment__updated_by_user
            FOREIGN KEY (updated_by_user_id)
            REFERENCES user_account (id)
            ON DELETE SET NULL;
    END IF;
END $$;
