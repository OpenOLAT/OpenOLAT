alter table o_note add ( temp clob );
update o_note set temp=notetext, notetext=null;
alter table o_note drop column notetext;
alter table o_note rename column temp to notetext;
