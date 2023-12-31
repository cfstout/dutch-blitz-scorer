CREATE TABLE round_scores
(
    game_id               VARCHAR(255) NOT NULL,
    round_number          SMALLINT      NOT NULL,
    player_number         SMALLINT      NOT NULL,
    blitz_cards_remaining SMALLINT      NOT NULL,
    point_cards           SMALLINT      NOT NULL,
    PRIMARY KEY (game_id, round_number, player_number)
);
