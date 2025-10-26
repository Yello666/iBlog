create table tags
(
    tid      bigint       not null primary key,
    tag_name varchar(255) not null,
    constraint tag_name unique (tag_name)
);


