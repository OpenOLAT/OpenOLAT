-- Document editor
alter table o_de_access add o_download number default 0;

-- Contact tracing
alter table o_ct_location add column l_seat_number number default 0 not null;
alter table o_ct_registration add column l_seat_number varchar2(64);