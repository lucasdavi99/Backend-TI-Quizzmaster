ALTER TABLE question ADD COLUMN difficulty VARCHAR(20) DEFAULT 'EASY';

UPDATE question SET difficulty = 'EASY';

ALTER TABLE question ALTER COLUMN difficulty SET NOT NULL;
