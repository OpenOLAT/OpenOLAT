alter table o_feed add ( temp clob );
update o_feed set temp=f_description, f_description=null;
alter table o_feed drop column f_description;
alter table o_feed rename column temp to f_description;