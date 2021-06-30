-- Task
update o_ex_task set e_status='ignore' where e_task like '<org.olat.admin.user.delete.service.DeleteUserDataTask>%';

-- Delete user process
create table o_user_data_delete (
   id bigserial,
   creationdate timestamp,
   lastmodified timestamp,
   u_user_data text,
   u_resource_ids text,
   u_current_resource_id varchar(64),
   primary key (id)
);




