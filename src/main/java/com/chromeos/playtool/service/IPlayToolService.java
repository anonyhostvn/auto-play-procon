package com.chromeos.playtool.service;

import com.chromeos.playtool.common.model.GameInfo;
import com.chromeos.playtool.models.request.SetTokenPlayToolRequest;
import com.chromeos.playtool.models.response.SetCurrentGameResponse;
import com.chromeos.playtool.models.response.SetTokenPlayToolResponse;

import java.util.List;

public interface IPlayToolService {
    List<GameInfo> getAllRecentGame();

    SetTokenPlayToolResponse setNewToken(SetTokenPlayToolRequest setTokenPlayToolRequest);

    SetCurrentGameResponse setCurrentGame(Long matchId);
}
