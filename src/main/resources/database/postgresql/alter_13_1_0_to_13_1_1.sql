-- clean setup errors
create index idx_dc_to_gen_idx on o_qual_data_collection(fk_generator);
