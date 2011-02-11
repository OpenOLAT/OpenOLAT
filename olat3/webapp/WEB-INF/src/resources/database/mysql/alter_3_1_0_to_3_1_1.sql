#
# the category table
# - renamed message_id -> id
# - renamed fk_olatresource -> fk_repoentry
# - changed not null constraint on fk_ownergroup
#
set foreign_key_checks=0;
drop table if exists o_catentry;
create table o_catentry (
   id bigint not null,
   lastmodified datetime not null,
   creationdate datetime,
   name varchar(100) not null,
   description varchar(255),
   externalURL varchar(255),
   fk_repoentry bigint,
   fk_ownergroup bigint unique,
   type integer not null,
   parent_id bigint,
   primary key (id)
);
alter table o_catentry type = InnoDB;
alter table o_catentry add index FKF4433C2C7B66B0D0 (parent_id), add constraint FKF4433C2C7B66B0D0 foreign key (parent_id) references o_catentry (id);
alter table o_catentry add index FKF4433C2CA1FAC766 (fk_ownergroup), add constraint FKF4433C2CA1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
alter table o_catentry add index FKF4433C2CDDD69946 (fk_repoentry), add constraint FKF4433C2CDDD69946 foreign key (fk_repoentry) references o_repositoryentry (repositoryentry_id);

set foreign_key_checks=1;
