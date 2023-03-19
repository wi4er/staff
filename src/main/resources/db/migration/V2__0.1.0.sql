create table public.user_group
(
    id serial
        primary key
);

create table public.user2usergroup
(
    "user"  integer not null
        constraint fk_user2usergroup_user_id
            references public."user"
            on update cascade on delete cascade,
    "group" integer not null
        constraint fk_user2usergroup_group_id
            references public.user_group
            on update cascade on delete cascade
);

create unique index user2usergroup__uniq
    on user2usergroup ("user", "group");