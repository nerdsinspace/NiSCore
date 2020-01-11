-- add last private message from and last private message to field to player

-- add column
ALTER TABLE Player ADD
    player_last_pm_to int;
ALTER TABLE Player ADD
    CONSTRAINT fk_plpmt FOREIGN KEY (player_last_pm_to)
        REFERENCES Player(id)
            -- [jooq ignore start]
            ON DELETE SET NULL
            ON UPDATE NO ACTION
            -- [jooq ignore stop]
;

ALTER TABLE Player ADD
    player_last_pm_from int;
ALTER TABLE Player ADD
    CONSTRAINT fk_plpmf FOREIGN KEY (player_last_pm_from)
        REFERENCES Player(id)
            -- [jooq ignore start]
            ON DELETE SET NULL
            ON UPDATE NO ACTION
            -- [jooq ignore stop]
;