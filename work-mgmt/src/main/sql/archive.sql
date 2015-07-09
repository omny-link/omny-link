select count(1) from ACT_HI_PROCINST where PROC_DEF_ID_ not in (select id_ from ACT_RE_PROCDEF);
select count(1) from ACT_HI_PROCINST;
 delete from ACT_HI_PROCINST where PROC_DEF_ID_ not in (select id_ from ACT_RE_PROCDEF);
 
 
 select count(1) from ACT_HI_VARINST;
 select count(1) from ACT_HI_VARINST where proc_inst_id_ not in (select proc_inst_id_ from ACT_HI_PROCINST);
delete from ACT_HI_VARINST where proc_inst_id_ not in (select proc_inst_id_ from ACT_HI_PROCINST);

