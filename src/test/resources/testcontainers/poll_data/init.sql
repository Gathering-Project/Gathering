CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    location VARCHAR(255) NULL,
    nickname VARCHAR(255) NULL,
    email VARCHAR(255) NULL,
    password VARCHAR(255) NULL,
    is_deleted BOOLEAN NULL,
    user_role ENUM('ROLE_USER', 'ROLE_ADMIN') NULL,
    PRIMARY KEY (user_id)
);

INSERT INTO users (nickname, email, password, user_role, is_deleted)
VALUES ('testNickname1', 'test1@example.com', '123456789a!', 'ROLE_USER', false), -- 유저 1: 소모임, 이벤트 개최자
('testNickname2', 'test2@example.com', '123456789a!', 'ROLE_USER', false), -- 유저 2: 소모임, 이벤트 참가자
('testNickname3', 'test3@example.com', '123456789a!', 'ROLE_USER', false); -- 유저 3: 미참가자


CREATE TABLE gatherings (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(30) NULL,
    description VARCHAR(100) NULL,
    user_id BIGINT NULL
);

INSERT INTO gatherings (title, description, user_id)
VALUES ('test1', 'test1', 1);

CREATE TABLE events (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    gathering_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(50) NULL,
    content VARCHAR(255) NULL
);

INSERT INTO events (gathering_id, user_id, title, content)
VALUES (1, 1, 'test1', 'test1');

CREATE TABLE participants (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL
);

INSERT INTO participants (event_id, user_id)
VALUES (1, 1), (1, 2);

CREATE TABLE polls (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    gathering_id BIGINT NOT NULL,
    agenda VARCHAR(255) NULL,
    is_active BOOLEAN NULL
);

INSERT INTO polls (event_id, gathering_id, agenda, is_active)
VALUES (1, 1, 'test1', true),
(1, 1, 'test2', true),
(1, 1, 'test3', false); -- 투표 3은 이미 마감된 투표

CREATE TABLE votes (
    poll_id BIGINT NOT NULL ,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    gathering_id BIGINT NOT NULL,
    is_done BOOLEAN NULL,
    selected_option INT NULL
);

INSERT INTO votes (poll_id, user_id, event_id, gathering_id, is_done, selected_option)
VALUES (1, 1, 1, 1, true, 0);

CREATE TABLE poll_options (
    option_num INT NOT NULL,
    poll_id BIGINT NOT NULL,
    name VARCHAR(255) NULL,
    vote_count INT NULL
);

INSERT INTO poll_options (option_num, poll_id, name, vote_count)
VALUES (0, 1, 'option1', 1),
       (1, 1, 'option2', 0),
       (2, 1, 'option3', 0);
