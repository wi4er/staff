create table public."user-contact-permission"
(
    id      serial
        primary key,
    contact  varchar(50) not null
        constraint "fk_user-contact-permission_contact_id"
            references public."user-contact"
            on update CASCADE on delete CASCADE ,
    "group" integer     not null
        constraint "fk_user-contact-permission_group_id"
            references public.user_group
            on update CASCADE on delete CASCADE ,
    method  integer     not null
);