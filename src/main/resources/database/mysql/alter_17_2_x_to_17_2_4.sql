-- Efficiency statment
alter table o_as_eff_statement add column last_statement bool default true not null;
alter table o_as_eff_statement add column archive_path varchar(255);
alter table o_as_eff_statement add column archive_certificate bigint;
alter table o_as_eff_statement add column completion float(65,30);

alter table o_as_eff_statement drop index eff_statement_id_cstr;

alter table o_as_entry add column a_run bigint default 1 not null;

alter table o_as_user_course_infos add column run bigint default 1 not null;

-- repoEntry AuditLogs for status changes
create table o_repositoryentry_audit_log (
    id bigint not null auto_increment,
    creationdate datetime not null,
    r_action varchar(32) not null,
    r_val_before longtext,
    r_val_after longtext,
    fk_entry bigint,
    fk_author bigint,
    primary key (id)
);