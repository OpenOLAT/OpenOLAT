-- repository entry lifecycle
alter table o_repositoryentry_cycle add column r_defaultpubliccycle bool not null default false;

-- badges
alter table o_badge_class alter column b_description type text;
