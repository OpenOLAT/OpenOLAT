-- eliminate ORACLE reserved words
alter table o_repositoryentry rename column access_ to accesscode;
alter table o_checkpoint rename column mode_ to modestring;

drop index access_idx;
create index  access_idx on o_repositoryentry (accesscode);


-- info messages
CREATE TABLE o_info_message (
  info_id number(20)  NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  modificationdate date,
  title varchar2(2048 char),
  message varchar2(2048 char),
  resname varchar(50 char) NOT NULL,
  resid number(20) NOT NULL,
  ressubpath varchar2(2048 char),
  businesspath varchar2(2048 char),
  fk_author_id number(20),
  fk_modifier_id number(20),
  PRIMARY KEY (info_id)
);

create index imsg_resid_idx on o_info_message (resid);
create index imsg_author_idx on o_info_message (fk_author_id);
alter table o_info_message add constraint FKF85553465A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);

create index imsg_modifier_idx on o_info_message (fk_modifier_id);
alter table o_info_message add constraint FKF85553465A4FA5EF foreign key (fk_modifier_id) references o_bs_identity (id);
