INSERT ACT_ID_USER (
  ID_,
  FIRST_,
  LAST_,
  EMAIL_,
  PWD_
) VALUES (
  'tstephen', 
  'Tim', 
  'Stephenson', 
  'tim@knowprocess.com',
  'tstephen'
);
  
INSERT INTO ACT_ID_GROUP (ID_,NAME_,TYPE_)
VALUES ('ADMIN','Admin',null);
INSERT INTO ACT_ID_GROUP (ID_,NAME_,TYPE_)
VALUES ('USER','User',null);

INSERT INTO ACT_ID_MEMBERSHIP (USER_ID_,GROUP_ID_)
VALUES ('tstephen','ADMIN');
INSERT INTO ACT_ID_MEMBERSHIP (USER_ID_,GROUP_ID_)
VALUES ('tstephen','USER');
