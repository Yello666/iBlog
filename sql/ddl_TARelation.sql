create table tag_article_relations
(
    ta_relation_id bigint auto_increment
        primary key,
    tid            bigint not null,
    aid            bigint not null,
    constraint uq_tid_aid
        unique (tid, aid)
);

create index idx_aid
    on tag_article_relations (aid);

create index idx_tid
    on tag_article_relations (tid);


