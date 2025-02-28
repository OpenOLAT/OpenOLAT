-- repository entry lifecycle
alter table o_repositoryentry_cycle add column r_defaultpubliccycle number default 0 not null;