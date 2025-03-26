-- Lectures
alter table o_lecture_block add column fk_meeting bigint default null;
alter table o_lecture_block add column fk_teams bigint default null;

alter table o_lecture_block add constraint lecture_block_bbb_idx foreign key (fk_meeting) references o_bbb_meeting (id);
alter table o_lecture_block add constraint lecture_block_teams_idx foreign key (fk_teams) references o_teams_meeting (id);
