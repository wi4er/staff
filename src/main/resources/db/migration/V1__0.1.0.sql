create table public."user"
(
    id    serial
        primary key,
    login varchar(100) not null
        constraint user_login_unique
            unique
        constraint login_size
            check (length((login)::text) > 5)
);