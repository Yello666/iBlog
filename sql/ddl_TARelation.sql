create table tag_article_relations
(
    ta_relation_id bigint auto_increment
        primary key,
    tag_name varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
    aid            bigint not null

);

create index idx_aid
    on tag_article_relations (aid);

create index idx_tid
    on tag_article_relations (tid);


