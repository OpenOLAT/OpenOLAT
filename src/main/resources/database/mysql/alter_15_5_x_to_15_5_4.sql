-- Task
update o_ex_task set e_status='ignore' where e_task like '<org.olat.admin.user.delete.service.DeleteUserDataTask>%';

-- Delete user process
create table o_user_data_delete (
   id bigint not null auto_increment,
   creationdate datetime,
   lastmodified datetime,
   u_user_data mediumtext,
   u_resource_ids mediumtext,
   u_current_resource_id varchar(64),
   primary key (id)
);
alter table o_user_data_delete ENGINE = InnoDB;




