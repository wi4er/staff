alter table property
add column type varchar(50) NOT NULL;

create table public.user2string
(
    id       serial
        primary key,
    property varchar(50)  not null
        constraint fk_user2string_property_id
            references public.property
            on update cascade on delete cascade,
    "user"   integer      not null
        constraint fk_user2string_user_id
            references public."user"
            on update cascade on delete cascade,
    lang     varchar(50)
        constraint fk_user2string_lang_id
            references public.lang
            on update restrict on delete restrict,
    value    varchar(256) not null
);

create table public.user2point
(
    id       serial
        primary key,
    property varchar(50) not null
        constraint fk_user2point_property_id
            references public.property
            on update cascade on delete cascade,
    "user"   integer     not null
        constraint fk_user2point_user_id
            references public."user"
            on update cascade on delete cascade,
    point    varchar(50) not null
        constraint fk_user2point_point_id
            references public.directory_point
            on update cascade on delete cascade
);

create unique index user2point__uniq
    on public.user2point ("user", property, point);

create table public.user2user
(
    id       serial
        primary key,
    property varchar(50) not null
        constraint fk_user2user_property_id
            references public.property
            on update cascade
            on delete cascade,
    "user"   integer     not null
        constraint fk_user2user_user_id
            references public."user"
            on update cascade
            on delete cascade,
    child    integer     not null
        constraint fk_user2user_child_id
            references public."user"
            on update cascade
            on delete cascade
);

create unique index user2user_property_user_child_uindex
    on public.user2user (property, "user", child);