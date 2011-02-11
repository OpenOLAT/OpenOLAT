SET FOREIGN_KEY_CHECKS = 0;

# change DECIMAL precision from DECIMAL(78,36) to standard DECIMAL(65,30) if upgrading from <5 to MySQL versions >=5.0.6
# alter table o_qtiresult modify score DECIMAL(65,30);
# alter table o_qtiresultset modify score DECIMAL(65,30);
# alter table o_property modify floatvalue DECIMAL(65,30);

SET FOREIGN_KEY_CHECKS = 1;
