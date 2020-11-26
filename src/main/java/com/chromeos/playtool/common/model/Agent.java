package com.chromeos.playtool.common.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Agent implements Serializable {

    private Integer agentID;

    private Integer x;

    private Integer y;

    public Agent(Integer agentID, Integer x, Integer y) {
        this.agentID = agentID;
        this.x = x;
        this.y = y;
    }
}
