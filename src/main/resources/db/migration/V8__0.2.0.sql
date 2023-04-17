create table public.user2contact
(
    id      serial
        primary key,
    "user"  integer      not null
        constraint fk_user2contact_user_id
            references public."user"
            on update cascade
            on delete cascade,
    contact varchar(50)  not null
        constraint fk_user2contact_contact_id
            references public.user_contact
            on update cascade
            on delete cascade,
    value   varchar(100) not null
);

create unique index user2contact_contact_value_uindex
    on public.user2contact (contact, value);
