CREATE TABLE users
(
    uid      BIGINT      NOT NULL,
    user_name VARCHAR(20) NOT NULL,
    gender   CHAR(1)        NULL,
    age      INT         NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20) NOT NULL ,
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

     CONSTRAINT pk_user PRIMARY KEY (uid),
    UNIQUE KEY `uk_user_name` (user_name)
);