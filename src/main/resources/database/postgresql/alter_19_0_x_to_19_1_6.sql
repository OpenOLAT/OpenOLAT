-- repository entry lifecycle
alter table o_repositoryentry_cycle add column r_defaultpubliccycle bool not null default false;