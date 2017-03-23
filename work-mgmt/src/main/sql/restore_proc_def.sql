-- check proc def missing and find next version that does have def
select * from ACT_GE_BYTEARRAY where deployment_id_ = 23582567;

--- find proc def id for deployment
select * from ACT_RE_PROCDEF where deployment_id_ = 15057540;

-- set proc def to next version
 insert into ACT_GE_BYTEARRAY (id_,rev_,name_,deployment_id_,bytes_)
select UUID(),1,'SimpleToDo:4:15057542',15057540,bytes_ from ACT_GE_BYTEARRAY where deployment_id_ = 23582567;
