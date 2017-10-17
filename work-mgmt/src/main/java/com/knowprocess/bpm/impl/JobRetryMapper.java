package com.knowprocess.bpm.impl;

import org.apache.ibatis.annotations.Update;

public interface JobRetryMapper {

    @Update("UPDATE ACT_RU_JOB j "
            + "SET j.RETRIES_ = 1, j.DUEDATE_ = #{1}, j.LOCK_EXP_TIME_ = null "
            + "WHERE j.ID_ = #{0}")
    void retryJob(String jobId, String dueDate);

}
