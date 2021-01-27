-- Teams
alter table o_teams_meeting add column t_open_participant bool not null default false;
