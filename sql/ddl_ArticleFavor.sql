create table article_favor
(
    afid       bigint auto_increment
        primary key,
    uid        bigint   not null,
    aid        bigint   not null,
    created_at datetime not null,
    constraint article_favor_pk
        unique (aid, uid)
);

