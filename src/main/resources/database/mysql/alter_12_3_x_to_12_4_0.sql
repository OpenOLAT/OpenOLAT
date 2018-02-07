
-- Evaluation forms
alter table o_eva_form_response drop column e_responsedatatype;
alter table o_eva_form_response add column e_file_response_path varchar(4000);

-- access control
alter table o_ac_offer add column confirmation_email bit default 0;