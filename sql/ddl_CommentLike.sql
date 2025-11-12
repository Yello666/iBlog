create table comment_like
(
    clid       bigint auto_increment
        primary key,
    uid        bigint   not null,
    cid        bigint   not null,
    created_at datetime not null,
    constraint comment_like_pk
        unique (cid, uid)
);

