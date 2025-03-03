-- repository entry lifecycle
alter table o_repositoryentry_cycle add column r_defaultpubliccycle number default 0 not null;

-- badges
alter table o_badge_class modify b_description varchar(4000);
