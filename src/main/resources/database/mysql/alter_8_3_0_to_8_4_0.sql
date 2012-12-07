-- instant messaging
create table if not exists o_im_message (
   id bigint not null,
   creationdate datetime,
   msgbody longtext,
   fk_from_identity_id bigint not null,
   primary key (id)
);
alter table o_im_message add constraint idx_im_msg_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);

create table if not exists o_im_preferences (
   id bigint not null,
   creationdate datetime,
   visible_to_others bit default 0,
   online_time_visible bit default 0,
   roster_def_status varchar(12),
   fk_from_identity_id bigint not null,
   primary key (id)
);
alter table o_im_preferences add constraint idx_im_prfs_to_id foreign key (fk_from_identity_id) references o_bs_identity (id);


