#
# the repositoryentry table
# - added canCopy flag
#
alter table o_repositoryentry add (canCopy bit not null);

#
# Add index on user attributes used by repository search
#
create index firstname_idx on o_user (firstname);
create index lastname_idx on o_user (lastname);
