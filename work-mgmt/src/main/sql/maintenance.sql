
-- Find and remove process history where the definition has been deleted
select count(1) from ACT_HI_PROCINST where PROC_DEF_ID_ not in (select id_ from ACT_RE_PROCDEF);
select count(1) from ACT_HI_PROCINST;
 delete from ACT_HI_PROCINST where PROC_DEF_ID_ not in (select id_ from ACT_RE_PROCDEF);
 
-- Find and remove variable history where the definition has been deleted
 select count(1) from ACT_HI_VARINST;
 select count(1) from ACT_HI_VARINST where proc_inst_id_ not in (select proc_inst_id_ from ACT_HI_PROCINST);
delete from ACT_HI_VARINST where proc_inst_id_ not in (select proc_inst_id_ from ACT_HI_PROCINST);

-- show process counts by defintion and tenant
select distinct(PROC_DEF_ID_) AS 'def', tenant_id_, count(1) from ACT_HI_PROCINST
group by tenant_id_, def;

-- Report which definitions are actually being used by tenant
select distinct(proc_def_id_)  from ACT_RU_EXECUTION  where tenant_id_ = 'flexspace';

-- Find space used for process images
select sum(length(bytes_))/1024/1024 as Mb from ACT_GE_BYTEARRAY where generated_ = 1;

-- Remove generated process images
DELETE from ACT_GE_BYTEARRAY where generated_ = 1;
