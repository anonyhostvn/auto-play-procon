package com.chromeos.playtool.service;

import com.chromeos.playtool.models.request.SetTokenPlayToolRequest;
import com.chromeos.playtool.models.response.*;

public interface IPlayToolService {

    SetTokenPlayToolResponse setNewToken(SetTokenPlayToolRequest setTokenPlayToolRequest);

    SetCurrentGameResponse setCurrentGame(Long matchId);

    GetRecentTokenRequest getRecentToken();

    GetAllGameResponse getAllGame();

    UpdateGameStateResponse updateGameState();
}
