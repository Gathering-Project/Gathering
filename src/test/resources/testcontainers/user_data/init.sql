create table users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    location VARCHAR(255) NULL,
    nickname VARCHAR(255) NULL,
    email VARCHAR(255) NULL,
    password VARCHAR(255) NULL,
    is_deleted BOOLEAN NULL,
    user_role ENUM('ROLE_USER', 'ROLE_ADMIN') NULL,
     PRIMARY KEY (user_id)
);

insert into users (user_id, nickname, email, password, user_role, is_deleted)
values(1, 'testNickname1', 'test1@example.com', '123456789a!', 'ROLE_USER', false);