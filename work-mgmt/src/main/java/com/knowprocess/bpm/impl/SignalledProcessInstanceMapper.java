/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.bpm.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Select;

public interface SignalledProcessInstanceMapper {

    @Select("SELECT es.GROUP_ID_ as groupId, il.USER_ID_ as userId, il.TYPE_ as type "
            + "FROM ACT_RU_EVENT_SUBSCR es WHERE il.TASK_ID_ = #{taskId}")
    List<Map<String, Object>> selectTaskAllocation(String taskId);

}
