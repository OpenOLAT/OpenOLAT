SET FOREIGN_KEY_CHECKS = 0;

# add new column for waiting-list 
alter table o_gp_business add column waitinglist_enabled bit after maxparticipants;
alter table o_gp_business add column autocloseranks_enabled bit after waitinglist_enabled;
alter table o_gp_business add column fk_waitinggroup bigint unique after fk_partipiciantgroup;

# initilize existing value
update o_gp_business set waitinglist_enabled = 'FALSE' ;
update o_gp_business set autocloseranks_enabled = 'FALSE' ;

SET FOREIGN_KEY_CHECKS = 1;
