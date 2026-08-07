-- OO-9596
alter table o_gr_grade_system add column g_default bool default false not null;


-- Certificates
alter table o_cer_certificate add column c_generation_retries int8 default 0 not null;
alter table o_cer_certificate add column c_generation_next_date timestamp;
alter table o_cer_certificate add column c_generation_data text;
