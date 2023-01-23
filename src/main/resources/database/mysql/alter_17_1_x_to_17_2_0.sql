alter table o_repositoryentry add column canindexmetadata bool default false not null;
alter table o_lic_license_type add column l_type_oer bool default false not null;