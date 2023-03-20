alter table public.user_group
    add "parentId" integer
        constraint fk_user_group_parentid_id
            references public.user_group
            on update restrict on delete restrict

