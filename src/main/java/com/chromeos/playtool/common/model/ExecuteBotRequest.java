package com.chromeos.playtool.common.model;

import lombok.Data;

@Data
public class ExecuteBotRequest {
    private MapState mapState;
    private Integer teamId;
}
