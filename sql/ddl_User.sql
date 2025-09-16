CREATE TABLE users
(
    uid      BIGINT      NOT NULL,
    user_name VARCHAR(20) NOT NULL,
    gender   CHAR(1)        NULL,
    age      INT         NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (uid),
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);