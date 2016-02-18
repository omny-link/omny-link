package com.knowprocess.bpm.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Select;

public interface TaskAllocationMapper {

    @Select("SELECT il.GROUP_ID_ as groupId, il.USER_ID_ as userId, il.TYPE_ as type FROM ACT_RU_IDENTITYLINK il WHERE il.TASK_ID_ = #{taskId}")
    List<Map<String, Object>> selectTaskAllocation(String taskId);

}
