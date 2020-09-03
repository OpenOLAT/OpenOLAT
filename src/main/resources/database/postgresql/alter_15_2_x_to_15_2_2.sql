alter table o_catentry add column add_entry_position int;
alter table o_catentry add column add_category_position int;


-- Authentication
create index low_authusername_idx on o_bs_authentication (lower(authusername));