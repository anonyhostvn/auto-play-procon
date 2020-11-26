package com.chromeos.playtool.common.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Action implements Serializable {

    private Integer agentID;

    private String type;

    private Integer dx;

    private Integer dy;

    private Integer turn;

    private Integer apply;

    public Action() {
    }

    public Action(RequestAction requestAction) {
        this.agentID=requestAction.getAgentID();
        this.type=requestAction.getType();
        this.dx=requestAction.getDx();
        this.dy=requestAction.getDy();
    }
}
