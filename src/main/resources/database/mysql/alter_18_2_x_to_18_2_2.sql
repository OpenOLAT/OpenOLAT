-- Projects
alter table o_proj_activity drop foreign key activity_artefact_idx;
alter table o_proj_activity drop foreign key activity_artefact_ref_idx;
create index idx_activity_artefact_idx on o_proj_activity (fk_artefact);
create index idx_activity_artefact_reference_idx on o_proj_activity (fk_artefact_reference);
