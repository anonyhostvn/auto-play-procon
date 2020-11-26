package com.chromeos.playtool.models.response;

import com.chromeos.playtool.common.model.GameInfo;
import lombok.Data;

import java.util.List;

@Data
public class GetAllGameResponse {
    private List<GameInfo> listGame;
}
