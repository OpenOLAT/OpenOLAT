-- Curriculum
alter table o_cur_curriculum_element add column fk_implementation int8;

alter table o_cur_curriculum_element add constraint cur_el_to_impl_el_idx foreign key (fk_implementation) references o_cur_curriculum_element (id);
create index idx_cur_el_to_impl_el_idx on o_cur_curriculum_element (fk_implementation);


