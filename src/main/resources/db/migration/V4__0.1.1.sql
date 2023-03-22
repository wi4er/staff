create table public.permission_method
(
    id      serial
        primary key,
    method  integer not null,
    entity  integer not null,
    "group" integer not null
        constraint fk_permission_method_group_id
            references public.user_group
            on update CASCADE on delete CASCADE
);