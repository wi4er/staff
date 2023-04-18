create table public.lang
(
    id varchar(50) not null
        primary key
        constraint lang_id_unique
            unique
        constraint lang_id_size
            check (length((id)::text) > 1)
);