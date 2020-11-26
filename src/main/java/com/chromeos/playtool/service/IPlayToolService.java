package com.chromeos.playtool.service;

import com.chromeos.playtool.models.request.SetTokenPlayToolRequest;
import com.chromeos.playtool.models.response.GetAllGameResponse;
import com.chromeos.playtool.models.response.GetRecentTokenRequest;
import com.chromeos.playtool.models.response.SetCurrentGameResponse;
import com.chromeos.playtool.models.response.SetTokenPlayToolResponse;

public interface IPlayToolService {

    SetTokenPlayToolResponse setNewToken(SetTokenPlayToolRequest setTokenPlayToolRequest);

    SetCurrentGameResponse setCurrentGame(Long matchId);

    GetRecentTokenRequest getRecentToken();

    GetAllGameResponse getAllGame();
}
