-- Efficiency statment
alter table o_as_eff_statement add last_statement number default 1 not null;
alter table o_as_eff_statement add archive_path varchar(255);
alter table o_as_eff_statement add archive_certificate number(20);
alter table o_as_eff_statement add completion float(24);

alter table o_as_eff_statement drop constraint u_o_as_eff_statement;

alter table o_as_entry add a_run number(20) default 1 not null;

alter table o_as_user_course_infos add run number(20) default 1 not null;

-- repoEntry AuditLogs for status changes
create table o_repositoryentry_audit_log (
    id number(20) generated always as identity,
    creationdate date not null,
    r_action varchar(32) not null,
    r_val_before CLOB,
    r_val_after CLOB,
    fk_entry number(20) not null,
    fk_author number(20) not null,
    primary key (id)
);