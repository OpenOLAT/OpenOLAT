-- Lectures
alter table o_lecture_block add column fk_meeting int8 default null;
alter table o_lecture_block add column fk_teams int8 default null;

alter table o_lecture_block add constraint lecture_block_bbb_idx foreign key (fk_meeting) references o_bbb_meeting (id);
create index idx_lecture_block_bbb_idx on o_lecture_block(fk_meeting);
alter table o_lecture_block add constraint lecture_block_teams_idx foreign key (fk_teams) references o_teams_meeting (id);
create index idx_lecture_block_teams_idx on o_lecture_block(fk_teams);