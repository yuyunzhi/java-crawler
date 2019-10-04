create table news(
id bigint primary key auto_increment,
title text,
content text,
url text,
created_at timestamp,
modified_at timestamp
);

create table LINKS_TO_BE_PROCESSED (link text);
create table LINKS_ALREADY_PROCESSED (link text);