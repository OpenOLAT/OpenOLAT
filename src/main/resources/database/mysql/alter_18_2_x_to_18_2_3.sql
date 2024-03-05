-- JupyterHub
alter table o_jup_hub add column j_ram_guarantee varchar(255);
alter table o_jup_hub add column j_cpu_guarantee bigint;
alter table o_jup_hub add column j_additional_fields mediumtext;
