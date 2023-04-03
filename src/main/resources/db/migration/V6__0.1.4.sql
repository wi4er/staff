create table public."user-contact"
(
    id   varchar(50) not null
        primary key
        constraint "user-contact_id_unique"
            unique,
    type varchar(50) not null
);