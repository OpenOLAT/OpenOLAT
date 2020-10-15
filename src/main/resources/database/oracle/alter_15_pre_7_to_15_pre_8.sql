-- ePortfolio V1
drop view o_ep_notifications_comment_v;
drop view o_ep_notifications_rating_v;
drop view o_ep_notifications_art_v;
drop view o_ep_notifications_struct_v;
drop table o_ep_struct_to_group;
drop table o_ep_struct_artefact_link;
drop table o_ep_struct_struct_link;
drop table o_ep_artefact;
drop table o_ep_collect_restriction;
drop table o_ep_struct_el;

-- Course disclaimer
create table o_course_disclaimer_consent(
	id number(20) generated always as identity,
	disc_1_accepted number not null,
	disc_2_accepted number not null, 
	creationdate timestamp not null, 
	lastmodified timestamp not null, 
	fk_repository_entry number(20) not null, 
	fk_identity number(20) not null,
	primary key (id)
);