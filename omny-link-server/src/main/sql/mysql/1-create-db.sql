CREATE DATABASE crm_db;
CREATE USER 'crm'@'localhost' identified by 'crm';
GRANT ALL on crm_db.* to 'crm'@'localhost';

use crm_db;

INSERT INTO ACT_ID_USER VALUES('admin',1,'CRM','Admin','you@example.com','secret',null);

INSERT INTO ACT_ID_GROUP VALUES('user',1,'User',null);
INSERT INTO ACT_ID_GROUP VALUES('admin',1,'Admin',null);

INSERT INTO ACT_ID_MEMBERSHIP VALUES('admin','user');
INSERT INTO ACT_ID_MEMBERSHIP VALUES('admin','admin');

INSERT INTO ACT_ID_INFO VALUES(1,1,'admin','userinfo','tenant','omny', null, null);
