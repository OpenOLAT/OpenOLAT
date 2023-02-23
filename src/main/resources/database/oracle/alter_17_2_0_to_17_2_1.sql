-- Task
alter table o_as_entry add a_coach_assignment_date date default null;
alter table o_as_entry add fk_coach number(20) default null;

alter table o_as_entry add constraint as_entry_to_coach_idx foreign key (fk_coach) references o_bs_identity (id);
create index idx_as_entry_to_coach_idx on o_as_entry (fk_coach);

alter table o_course_element add c_coach_assignment number default 0 not null;