-- repository entry lifecycle
alter table o_repositoryentry_cycle add column r_defaultpubliccycle boolean not null default 0;