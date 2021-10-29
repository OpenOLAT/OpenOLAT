-- Business group 
alter table o_gp_business add status varchar(32) default 'active';

alter table o_gp_business add inactivationdate date;
alter table o_gp_business add inactivationemaildate date;
alter table o_gp_business add reactivationdate date;

alter table o_gp_business add softdeleteemaildate date;
alter table o_gp_business add softdeletedate date;

alter table o_gp_business add fk_inactivatedby_id number(20);
alter table o_gp_business add fk_softdeletedby_id number(20);

alter table o_gp_business add constraint gb_bus_inactivateby_idx foreign key (fk_inactivatedby_id) references o_bs_identity (id);
create index idx_gb_bus_inactivateby_idx on o_gp_business (fk_inactivatedby_id);

alter table o_gp_business add constraint gb_bus_softdeletedby_idx foreign key (fk_softdeletedby_id) references o_bs_identity (id);
create index idx_gb_bus_softdeletedby_idx on o_gp_business (fk_softdeletedby_id);


-- Assessment
alter table o_as_entry add a_obligation_inherited varchar2(50);
alter table o_as_entry add a_obligation_evaluated varchar2(50);
alter table o_as_entry add a_obligation_config varchar2(50);
alter table o_as_entry add a_max_score decimal;

create table o_as_score_accounting_trigger (
   id number(20) generated always as identity,
   creationdate date not null,
   e_identifier varchar2(64) not null,
   e_business_group_key number(20),
   e_organisation_key number(20),
   e_curriculum_element_key number(20),
   e_user_property_name varchar2(64),
   e_user_property_value varchar2(128),
   fk_entry number(20) not null,
   e_subident varchar2(64) not null,
   primary key (id)
);

alter table o_as_score_accounting_trigger add constraint satrigger_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_satrigger_re_idx on o_as_score_accounting_trigger (fk_entry);
create index idx_satrigger_bs_group_idx on o_as_score_accounting_trigger (e_business_group_key);
create index idx_satrigger_org_idx on o_as_score_accounting_trigger (e_organisation_key);
create index idx_satrigger_curle_idx on o_as_score_accounting_trigger (e_curriculum_element_key);
create index idx_satrigger_userprop_idx on o_as_score_accounting_trigger (e_user_property_value, e_user_property_name);
