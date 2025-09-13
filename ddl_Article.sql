CREATE TABLE articles
(
    aid          BIGINT       NOT NULL,
    article_name VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL ,
    content      MEDIUMTEXT NULL,
    created_at   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_article PRIMARY KEY (aid)
);