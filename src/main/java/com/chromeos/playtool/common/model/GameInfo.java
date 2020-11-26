package com.chromeos.playtool.common.model;

import lombok.Data;

@Data
public class GameInfo {

    private Integer id;

    private Integer intervalMillis;

    private String matchTo;

    private Integer teamID;

    private Integer turnMillis;

    private Integer turns;
}
