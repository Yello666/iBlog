CREATE TABLE comment
(
    cid        BIGINT AUTO_INCREMENT NOT NULL,
    contents   VARCHAR(500) NOT NULL,
    aid        BIGINT       NOT NULL,
    uid        BIGINT       NOT NULL,
    parent_id  BIGINT       NULL,
    created_at datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    likes_count int NULL DEFAULT 0,
    INDEX idx_aid (aid), -- 索引就是让aid排到第一列，然后后面的再填相关信息，其实就是主键的查询功能，之前是cid排到第一列
    INDEX idx_parent_id (parent_id),
    INDEX idx_aid_created_at (aid, created_at), -- 实现文章评论按时间排序的索引

    -- 但是索引越多，插入和删除越慢，因为创建的时候不仅要插入原来的表，还要插入索引表，特别是TEXT和VARCHAR
    -- 并且索引表也要占硬盘存储空间
    CONSTRAINT pk_comment PRIMARY KEY (cid)

);
