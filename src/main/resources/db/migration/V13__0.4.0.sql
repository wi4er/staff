create table public.directory
(
    id varchar(50) not null
        constraint pk_directory
            primary key
);

create table public.directory_point
(
    id        varchar(50) not null
        primary key
        constraint directory_point_id_unique
            unique,
    directory varchar(50) not null
        constraint fk_directory_point_directory_id
            references public.directory
            on update CASCADE
            on delete CASCADE
);