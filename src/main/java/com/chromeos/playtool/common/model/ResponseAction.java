package com.chromeos.playtool.common.model;

import lombok.Data;

@Data
public class ResponseAction {

    private Integer agentID;

    private String type;

    private Integer dx;

    private Integer dy;

    private Integer turn;
}
