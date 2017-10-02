package com.knowprocess.bpm.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Allocation {
    public Object type;
    public Object groupId;
    public Object userId;

    public Allocation(Object type, Object groupId, Object userId) {
        this.type = type;
        this.groupId = groupId;
        this.userId = userId;
    }
}