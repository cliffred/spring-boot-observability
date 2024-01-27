create table if not exists customer
(
    id         serial primary key,
    first_name varchar(50)  not null,
    last_name  varchar(50)  not null,
    email      varchar(255) not null
);

create table if not exists users
(
    username varchar_ignorecase(50) not null primary key,
    password varchar_ignorecase(500) not null,
    enabled boolean not null
);

create table if not exists authorities
(
    username varchar_ignorecase(50) not null,
    authority varchar_ignorecase(50) not null,
    constraint fk_authorities_users foreign key (username) references users (username)
);

create unique index ix_auth_username on authorities (username, authority);
