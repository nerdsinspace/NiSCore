-- initialize tables

CREATE TABLE Player (
    id integer PRIMARY KEY AUTO_INCREMENT
    ,uuid uuid UNIQUE NOT NULL
    ,username varchar_ignorecase NOT NULL
    ,ignore_count int DEFAULT 0
    ,CHECK (LENGTH(username) < 17)
);

CREATE TABLE Ignored_Player (
    subject_id integer NOT NULL
    ,target_id integer NOT NULL
    ,ignore_time timestamp DEFAULT CURRENT_TIMESTAMP()
    ,CONSTRAINT fk_subject FOREIGN KEY (subject_id)
        REFERENCES Player(id)
        -- [jooq ignore start]
            ON DELETE CASCADE
            ON UPDATE NO ACTION
        -- [jooq ignore stop]
    ,CONSTRAINT fk_target FOREIGN KEY (target_id)
        REFERENCES Player(id)
        -- [jooq ignore start]
            ON DELETE CASCADE
            ON UPDATE NO ACTION
        -- [jooq ignore stop]
    ,PRIMARY KEY (subject_id, target_id)
);
