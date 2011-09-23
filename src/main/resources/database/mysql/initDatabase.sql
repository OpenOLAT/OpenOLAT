CREATE DATABASE IF NOT EXISTS ${db.name};
GRANT ALL PRIVILEGES ON ${db.name}.* TO '${db.user}' IDENTIFIED BY '${db.pass}';
UPDATE mysql.user SET HOST='localhost' WHERE USER='${db.user}' AND HOST='%';
FLUSH PRIVILEGES;
