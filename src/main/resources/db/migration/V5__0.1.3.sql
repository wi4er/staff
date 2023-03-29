create table public."user-permission"
(
    id      serial
        primary key,
    "user"  integer not null
        constraint "fk_user-permission_user_id"
            references public."user"
            on update cascade on delete cascade,
    "group" integer not null
        constraint "fk_user-permission_group_id"
            references public.user_group
            on update cascade on delete cascade,
    method  integer not null
);