#
# adding long value to property table
#
alter table o_property add longvalue bigint after floatvalue;

#
# TODO copy old values...
#
# 1) all forum keys are stored as longs, not as floats
update o_property set longvalue=floatvalue, floatvalue=NULL where resourceTypeName='BusinessGroup' and category='collabtools' and name='forumKey';
update o_property set longvalue=floatvalue, floatvalue=NULL where resourceTypeName='CourseModule' and category like 'NID:%' and name='forumKey';
# 2) all visited forum messages are stored as longs, not as floats
update o_property set longvalue=floatvalue, floatvalue=NULL where resourceTypeName='Forum' and category='rvst' and name='mid';
# 3) all node attempts are stored as longs, not as floats
update o_property set longvalue=floatvalue, floatvalue=NULL where resourceTypeName='CourseModule' and category like 'NID:%' and name='ATTEMPTS';
# 4) update bookmark question decision recaller
update o_property set longvalue=floatvalue, floatvalue=NULL where resourceTypeName='RepositoryEntry' and category='rvst';
# 5) update group member visibility configuration
update o_property set longvalue=floatvalue, floatvalue=NULL where resourceTypeName='BusinessGroup' and category='config' and name='displayMembers';


# cleanup old data, make sure you did the alters in 3.0.1->3.1.0... otherwhise you need to alter this two lines first.
delete from o_property where resourceTypeName='Course';
delete from o_property where resourceTypeName='repoEntry';

