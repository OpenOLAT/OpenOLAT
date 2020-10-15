-- User
alter table o_user add column u_linkedin varchar(255);

-- Ensure the constraint exists
alter table o_user drop constraint if exists iuni_user_nickname_idx;
alter table o_user add constraint iuni_user_nickname_idx unique (u_nickname);
