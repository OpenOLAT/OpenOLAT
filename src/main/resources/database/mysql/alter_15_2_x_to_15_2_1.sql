alter table o_user add constraint iuni_user_nickname_idx unique (u_nickname);

create index idx_user_nickname_idx on o_user (u_nickname);