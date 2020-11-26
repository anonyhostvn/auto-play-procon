package com.chromeos.playtool.service.implement;

import com.chromeos.playtool.common.model.GameInfo;
import com.chromeos.playtool.constant.ResponseStatusMsg;
import com.chromeos.playtool.exception.BusinessLogicException;
import com.chromeos.playtool.hostserver.IHostServerClient;
import com.chromeos.playtool.models.request.SetTokenPlayToolRequest;
import com.chromeos.playtool.models.response.SetTokenPlayToolResponse;
import com.chromeos.playtool.repositories.PlayerStagingRepository;
import com.chromeos.playtool.service.IPlayToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PlayToolService implements IPlayToolService {


    private final PlayerStagingRepository playerStagingRepository;

    private final IHostServerClient iHostServerClient;

    @Autowired
    public PlayToolService(PlayerStagingRepository playerStagingRepository, IHostServerClient iHostServerClient) {
        this.playerStagingRepository = playerStagingRepository;
        this.iHostServerClient = iHostServerClient;
    }

    @Override
    public List<GameInfo> getAllRecentGame() {
        return null;
    }

    @Override
    public SetTokenPlayToolResponse setNewToken(SetTokenPlayToolRequest setTokenPlayToolRequest) {
        log.info("set Token to playerStagingRepository");
        playerStagingRepository.setToken(setTokenPlayToolRequest.getToken());

        log.info("Start call host to get all matches");
        List<GameInfo> gameInfoList = iHostServerClient.getAllMatches(setTokenPlayToolRequest.getToken());

        if (gameInfoList == null) {
            log.info("Error call host to get all matches");
            throw new BusinessLogicException(
                    ResponseStatusMsg.GET_GAME_INFO_FAILED.getCode(),
                    ResponseStatusMsg.GET_GAME_INFO_FAILED.getMsg()
            );
        }

        log.info("Success call host to get all matches");

        playerStagingRepository.setGameInfoList(gameInfoList);

        SetTokenPlayToolResponse setTokenPlayToolResponse = new SetTokenPlayToolResponse();
        setTokenPlayToolResponse.setListGame(gameInfoList);
        return setTokenPlayToolResponse;
    }

}
