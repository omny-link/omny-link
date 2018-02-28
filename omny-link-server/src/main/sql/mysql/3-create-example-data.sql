INSERT INTO ACT_GE_PROPERTY VALUES('schema.version','5.18.0.0',1); 
INSERT INTO ACT_GE_PROPERTY VALUES('next.dbid','2',1);

INSERT INTO ACT_ID_USER VALUES('you@example.com',1,'CRM','Admin','you@example.com','secret',null);

INSERT INTO ACT_ID_GROUP VALUES('user',1,'User',null);
INSERT INTO ACT_ID_GROUP VALUES('admin',1,'Admin',null);
INSERT INTO ACT_ID_GROUP VALUES('bot',1,'Bot',null);

INSERT INTO ACT_ID_MEMBERSHIP VALUES('you@example.com','user');
INSERT INTO ACT_ID_MEMBERSHIP VALUES('you@example.com','admin');

INSERT INTO ACT_ID_INFO VALUES(1,1,'you@example.com','userinfo','tenant','omny', null, null);

INSERT INTO OL_TENANT VALUES('omny','Omny Link',null,'active');
