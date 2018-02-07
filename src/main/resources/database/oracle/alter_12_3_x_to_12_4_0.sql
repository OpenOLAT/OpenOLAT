
-- Evaluation forms
alter table o_eva_form_response drop column e_responsedatatype;
alter table o_eva_form_response add column e_file_response_path varchar(4000);