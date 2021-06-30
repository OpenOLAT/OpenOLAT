-- Task
update o_ex_task set e_status='ignore' where e_task like '<org.olat.admin.user.delete.service.DeleteUserDataTask>%';

-- Delete user process
create table o_user_data_delete (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date,
   lastmodified date,
   u_user_data CLOB,
   u_resource_ids CLOB,
   u_current_resource_id varchar2(64 char),
   primary key (id)
);




