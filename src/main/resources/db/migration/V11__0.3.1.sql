create table public.property
(
    id varchar(50) not null
        primary key
        constraint property_id_unique
            unique
);