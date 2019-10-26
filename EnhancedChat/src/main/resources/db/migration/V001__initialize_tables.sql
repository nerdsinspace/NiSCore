-- initialize tables

CREATE TABLE Player (
    id integer PRIMARY KEY AUTOINCREMENT
    ,uuid_lsb bigint NOT NULL
    ,uuid_msb bigint NOT NULL
    ,username varchar NOT NULL
    ,ignore_count int DEFAULT 0
    ,UNIQUE (uuid_lsb, uuid_msb)
);

CREATE TABLE Ignored_Player (
    subject_id integer NOT NULL
    ,target_id integer NOT NULL
    ,ignore_time bigint NOT NULL
    ,CONSTRAINT fk_subject
        FOREIGN KEY (subject_id)
            REFERENCES Player(id)
            -- [jooq ignore start]
                ON DELETE CASCADE
            -- [jooq ignore stop]
    ,CONSTRAINT fk_target
        FOREIGN KEY (target_id)
            REFERENCES Player(id)
            -- [jooq ignore start]
                ON DELETE CASCADE
            -- [jooq ignore stop]
    ,PRIMARY KEY (subject_id, target_id)
);
