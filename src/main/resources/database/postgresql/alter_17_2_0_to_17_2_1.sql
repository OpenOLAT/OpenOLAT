-- Task
alter table o_as_entry add column a_coach_assignment_date timestamp default null;
alter table o_as_entry add column fk_coach int8 default null;

alter table o_as_entry add constraint as_entry_to_coach_idx foreign key (fk_coach) references o_bs_identity (id);
create index idx_as_entry_to_coach_idx on o_as_entry (fk_coach);

alter table o_course_element add column c_coach_assignment bool default false not null;