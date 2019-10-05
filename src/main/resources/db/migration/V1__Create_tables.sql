create table news(
    id bigint primary key auto_increment,
    title text,
    content text,
    url text,
    created_at timestamp default now(),
    modified_at timestamp default now()
);

create table LINKS_TO_BE_PROCESSED (link text);
create table LINKS_ALREADY_PROCESSED (link text);