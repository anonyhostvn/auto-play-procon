package com.chromeos.playtool.common.model;

import lombok.Data;

@Data
public class GameInfo {

    private Long id;

    private Long intervalMillis;

    private String matchTo;

    private Integer teamID;

    private Long turnMillis;

    private Integer turns;
}
