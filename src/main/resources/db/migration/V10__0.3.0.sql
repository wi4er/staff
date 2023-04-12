create table public.user2provider
(
    "user"   integer      not null
        constraint fk_user2provider_user_id
            references public."user"
            on update CASCADE
            on delete CASCADE ,
    provider varchar(50)  not null
        constraint fk_user2provider_provider_id
            references public.provider
            on update CASCADE
            on delete CASCADE ,
    hash     varchar(256) not null
);