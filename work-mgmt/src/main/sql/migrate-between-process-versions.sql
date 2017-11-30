-- CAVEAT: processes need to be pretty similar including in internal details 
-- like ids. Best is when change is only downstream of where process has got to.

-- Update ACT_RU_EXECUTION
update  ACT_RU_EXECUTION set proc_def_id_ = 'SimpleToDo:5:26790057' where proc_def_id_ = 'SimpleToDo:2:15057708';
update  ACT_RU_EXECUTION set proc_def_id_ = 'SimpleToDo:5:26790057' where proc_def_id_ = 'SimpleToDo:3:23590258';
update  ACT_RU_EXECUTION set proc_def_id_ = 'SimpleToDo:5:26790057' where proc_def_id_ = 'SimpleToDo:4:25465428';

-- Update ACT_ID_TASK 
update  ACT_RU_TASK set proc_def_id_ = 'SimpleToDo:5:26790057' where proc_def_id_ = 'SimpleToDo:2:15057708';
update  ACT_RU_TASK set proc_def_id_ = 'SimpleToDo:5:26790057' where proc_def_id_ = 'SimpleToDo:3:23590258';
update  ACT_RU_TASK set proc_def_id_ = 'SimpleToDo:5:26790057' where proc_def_id_ = 'SimpleToDo:4:25465428';
