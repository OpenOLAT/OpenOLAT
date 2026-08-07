-- OO-9596
alter table o_gr_grade_system add g_default number default 0 not null;

-- Certificates
alter table o_cer_certificate add c_generation_retries number(20) default 0 not null;
alter table o_cer_certificate add c_generation_next_date date;
alter table o_cer_certificate add c_generation_data CLOB;

