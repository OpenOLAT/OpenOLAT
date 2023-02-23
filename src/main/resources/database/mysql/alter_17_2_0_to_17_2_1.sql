-- Task
alter table o_as_entry add column a_coach_assignment_date datetime default null;
alter table o_as_entry add column fk_coach bigint default null;

alter table o_as_entry add constraint as_entry_to_coach_idx foreign key (fk_coach) references o_bs_identity (id);

alter table o_course_element add column c_coach_assignment bool default false not null;