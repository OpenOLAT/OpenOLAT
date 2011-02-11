--
-- convert OLAT 5 style date values (yyyy-mm-dd) with new style (yyyymmdd)
update o_userproperty set propvalue=replace(propvalue, '-', '') where propname='birthDay' and propvalue like '%-%';
