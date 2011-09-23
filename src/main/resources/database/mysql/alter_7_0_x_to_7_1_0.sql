-- eportfolio arteafcts
create table if not exists o_ep_artefact (
  artefact_id bigint not null,
  artefact_type varchar(32) not null,
  version mediumint unsigned not null,
  creationdate datetime,
  collection_date datetime,
  title varchar(512),
  description varchar(4000),
  signature mediumint default 0,
  businesspath varchar(2048),
  fulltextcontent longtext,
  reflexion longtext,
  source varchar(2048),
  add_prop1 varchar(2048),
  add_prop2 varchar(2048),
  add_prop3 varchar(2048),
  fk_struct_el_id bigint,
  fk_artefact_auth_id bigint not null,
  primary key (artefact_id)
);
alter table o_ep_artefact type = InnoDB;
alter table o_ep_artefact add constraint FKF26C8375236F28X foreign key (fk_artefact_auth_id) references o_bs_identity (id);

-- eportfolio collect restrictions
create table if not exists o_ep_collect_restriction (
  collect_id bigint not null,
  version mediumint unsigned not null,
  creationdate datetime,
  artefact_type varchar(256),
  amount mediumint not null default -1,
  restriction varchar(32),
  pos mediumint unsigned not null default 0,
  fk_struct_el_id bigint,
  primary key (collect_id)
);
alter table o_ep_collect_restriction type = InnoDB;

-- eportfolio structure element
create table if not exists o_ep_struct_el (
  structure_id bigint not null,
  structure_type varchar(32) not null,
  version mediumint unsigned not null,
  creationdate datetime,
  returndate datetime default null,
  copydate datetime default null,
  lastsyncheddate datetime default null,
  deadline datetime default null,
  title varchar(512),
  description varchar(2048),
  struct_el_source bigint,
  target_resname varchar(50),
  target_resid bigint,
  target_ressubpath varchar(2048),
  target_businesspath varchar(2048),
  style varchar(128),  
  status varchar(32),
  viewmode varchar(32),
  fk_struct_root_id bigint,
  fk_struct_root_map_id bigint,
  fk_map_source_id bigint,
  fk_ownergroup bigint,
  fk_olatresource bigint not null,
  primary key (structure_id)  
);
alter table o_ep_struct_el type = InnoDB;
alter table o_ep_struct_el add constraint FKF26C8375236F26X foreign key (fk_olatresource) references o_olatresource (resource_id);
alter table o_ep_struct_el add constraint FKF26C8375236F29X foreign key (fk_ownergroup) references o_bs_secgroup (id);
alter table o_ep_struct_el add constraint FK4ECC1C8D636191A1 foreign key (fk_map_source_id) references o_ep_struct_el (structure_id);
alter table o_ep_struct_el add constraint FK4ECC1C8D76990817 foreign key (fk_struct_root_id) references o_ep_struct_el (structure_id);
alter table o_ep_struct_el add constraint FK4ECC1C8D76990818 foreign key (fk_struct_root_map_id) references o_ep_struct_el (structure_id);

alter table o_ep_artefact add constraint FKA0070D12316A97B4 foreign key (fk_struct_el_id) references o_ep_struct_el (structure_id);

alter table o_ep_collect_restriction add constraint FKA0070D12316A97B5 foreign key (fk_struct_el_id) references o_ep_struct_el (structure_id);

-- eportfolio structure to structure link
create table if not exists o_ep_struct_struct_link (
  link_id bigint not null,
  version mediumint unsigned not null,
  creationdate datetime,
  pos mediumint unsigned not null default 0,
  fk_struct_parent_id bigint not null,
  fk_struct_child_id bigint not null,
  primary key (link_id)
);
alter table o_ep_struct_struct_link type = InnoDB;
alter table o_ep_struct_struct_link add constraint FKF26C8375236F22X foreign key (fk_struct_parent_id) references o_ep_struct_el (structure_id);
alter table o_ep_struct_struct_link add constraint FKF26C8375236F23X foreign key (fk_struct_child_id) references o_ep_struct_el (structure_id);

-- eportfolio structure to artefact link
create table if not exists o_ep_struct_artefact_link (
  link_id bigint not null,
  version mediumint unsigned not null,
  creationdate datetime,
  pos mediumint unsigned not null default 0,
  reflexion longtext,
  fk_auth_id bigint,
  fk_struct_id bigint not null,
  fk_artefact_id bigint not null,
  primary key (link_id)
);

alter table o_ep_struct_artefact_link type = InnoDB;
alter table o_ep_struct_artefact_link add constraint FKF26C8375236F24X foreign key (fk_struct_id) references o_ep_struct_el (structure_id);
alter table o_ep_struct_artefact_link add constraint FKF26C8375236F25X foreign key (fk_artefact_id) references o_ep_artefact (artefact_id);
alter table o_ep_struct_artefact_link add constraint FKF26C8375236F26Y foreign key (fk_auth_id) references o_bs_identity (id);


-- invitation
create table if not exists o_bs_invitation (
   id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   token varchar(64) not null,
   first_name varchar(64),
   last_name varchar(64),
   mail varchar(128),
   fk_secgroup bigint,
   primary key (id)
);
alter table o_bs_invitation type = InnoDB;
alter table o_bs_invitation add constraint FKF26C8375236F27X foreign key (fk_secgroup) references o_bs_secgroup (id);



-- tagging
create table if not exists o_tag (
  tag_id bigint not null,
  version mediumint unsigned not null,
  creationdate datetime,
  tag varchar(128) not null,
  resname varchar(50) not null,
  resid bigint not null,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  fk_author_id bigint not null,
  primary key (tag_id)
);

alter table o_tag type = InnoDB;
alter table o_tag add constraint FK6491FCA5A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);

-- update policy

alter table o_bs_policy add column apply_from datetime default null;
alter table o_bs_policy add column apply_to datetime default null;

-- eliminate ORACLE reserved words
alter table o_repositoryentry change access accesscode integer not null default 0;
alter table o_checkpoint change mode modestring varchar(64) not null;

drop index access_idx on o_repositoryentry;
create index  access_idx on o_repositoryentry (accesscode);


-- info messages
create table if not exists o_info_message (
  info_id bigint  NOT NULL,
  version mediumint NOT NULL,
  creationdate datetime,
  modificationdate datetime,
  title varchar(2048),
  message varchar(2048),
  resname varchar(50) NOT NULL,
  resid bigint NOT NULL,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  fk_author_id bigint,
  fk_modifier_id bigint,
  primary key (info_id)
);
alter table o_info_message type = InnoDB;

create index imsg_resid_idx on o_info_message (resid);
create index imsg_author_idx on o_info_message (fk_author_id);
alter table o_info_message add constraint FKF85553465A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);

create index imsg_modifier_idx on o_info_message (fk_modifier_id);
alter table o_info_message add constraint FKF85553465A4FA5EF foreign key (fk_modifier_id) references o_bs_identity (id);

--projectbroker.project.title was too short for title with äöü
alter table o_projectbroker_project modify title varchar(150);