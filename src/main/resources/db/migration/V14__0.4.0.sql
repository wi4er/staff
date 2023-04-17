alter table property
    add constraint property_id_size
        check (length((id)::text) > 3);

alter table user_provider
    add constraint provider_id_size
        check (length((id)::text) > 3);

alter table directory
    add constraint directory_id_size
        check (length((id)::text) > 3);

alter table directory_point
    add constraint point_id_size
        check (length((id)::text) > 3);

alter table user_contact
    add constraint contact_id_size
        check (length((id)::text) > 3);