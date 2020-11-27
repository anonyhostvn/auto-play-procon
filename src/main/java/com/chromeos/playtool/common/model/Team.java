package com.chromeos.playtool.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Team implements Serializable {

    private Long teamID;

    private List<Agent> agents;

    private Integer tilePoint;

    private Integer areaPoint;
}
