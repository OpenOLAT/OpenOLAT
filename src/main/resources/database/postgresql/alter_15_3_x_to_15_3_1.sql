-- Document editor
alter table o_de_access add column o_download bool default true;

-- Contact tracing
alter table o_ct_location add column l_seat_number boolean default false not null;
alter table o_ct_registration add column l_seat_number varchar(64);