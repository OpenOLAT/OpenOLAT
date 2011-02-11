OLAT DATABASE
==================================

OLAT uses MySQL as its primary database for production.

For your installation take a binary distribution with MySQL that includes 
support for InnoDB. An example for InnoDB support is at the end
of this file.

Hibernate (http://www.hibernate.org/) is used as persistent layer
in the Java part of OLAT.

The database directory contains ready to use SQL scripts for MySQL and PostgreSQL
in the corresponding subdirectories.
You can also run org.olat.persistence.DatabaseSetup.main() to generate the olat 
database script for your hibernate dialect. The generated sql commands saved
to database/setupDatabase.sql.

See our docu for more information: http://www.olat.org/docu/


my.cnf InnoDB Sample Config 
---------------------------

# You can write your other MySQL server options here
# ...
#                                        Data file(s) must be able to
#                                        hold your data and indexes.
#                                        Make sure you have enough
#                                        free disk space.
innodb_data_file_path = ibdata1:10M:autoextend
#                                        Set buffer pool size to
#                                        50 - 80 % of your computer's
#                                        memory
set-variable = innodb_buffer_pool_size=70M
set-variable = innodb_additional_mem_pool_size=10M
#                                        Set the log file size to about
#                                        25 % of the buffer pool size
set-variable = innodb_log_file_size=20M
set-variable = innodb_log_buffer_size=8M
#                                        Set ..flush_log_at_trx_commit
#                                        to 0 if you can afford losing
#                                        some last transactions 
innodb_flush_log_at_trx_commit=1
