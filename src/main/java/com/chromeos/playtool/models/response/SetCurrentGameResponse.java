package com.chromeos.playtool.models.response;

import com.chromeos.playtool.common.model.MapState;
import lombok.Data;

@Data
public class SetCurrentGameResponse {
    private MapState currentMapState;
}
