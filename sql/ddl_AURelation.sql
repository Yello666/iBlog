-- 不使用了
CREATE TABLE au_relations
(
    aurid      BIGINT AUTO_INCREMENT NOT NULL,
    aid        BIGINT                NOT NULL,
    uid        BIGINT                NOT NULL,
    created_at datetime              NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at datetime              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_au_relations PRIMARY KEY (aurid),
    CONSTRAINT fk_au_relations_aid FOREIGN KEY (aid) REFERENCES articles(aid),
    CONSTRAINT fk_au_relations_uid FOREIGN KEY (uid) REFERENCES users(uid),
    INDEX idx_au_relations_aid (aid),        -- 单字段索引,可以加速查询，速度为O log n,不加的话就是On
    INDEX idx_au_relations_uid (uid),        -- 单字段索引
    INDEX idx_au_relations_aid_uid (aid, uid) -- 复合索引（最左前缀原则）
);