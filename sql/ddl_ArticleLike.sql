create table article_like
(
    alid       bigint auto_increment
        primary key,
    uid        bigint   not null,
    aid        bigint   not null,
    created_at datetime not null,
    constraint article_like_pk
        unique (aid, uid)
);

