

-- evaluation forms
alter table o_eva_form_response add column e_no_response number default 0;

alter table o_eva_form_session add column e_resname varchar2(50);
alter table o_eva_form_session add column e_resid number(20);
alter table o_eva_form_session add column e_sub_ident varchar2(2048);

create index idx_eva_sess_ores_idx on o_eva_form_session (e_resid, e_resname, e_sub_ident);
