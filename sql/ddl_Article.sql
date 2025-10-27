CREATE TABLE articles
(
    aid          BIGINT       NOT NULL,
    uid          BIGINT       NOT NULL,
    title varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
    content      MEDIUMTEXT NULL,
    created_at   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    likes_count int NULL DEFAULT 0,
    favor_count int NULL DEFAULT 0,
    comments_count int NULL DEFAULT 0,
    CONSTRAINT pk_article PRIMARY KEY (aid)
);
