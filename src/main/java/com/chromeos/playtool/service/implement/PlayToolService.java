package com.chromeos.playtool.service.implement;

import com.chromeos.playtool.common.model.GameInfo;
import com.chromeos.playtool.common.model.MapState;
import com.chromeos.playtool.constant.ResponseStatusMsg;
import com.chromeos.playtool.exception.BusinessLogicException;
import com.chromeos.playtool.hostserver.IHostServerClient;
import com.chromeos.playtool.models.request.SetTokenPlayToolRequest;
import com.chromeos.playtool.models.response.*;
import com.chromeos.playtool.repositories.PlayerStagingRepository;
import com.chromeos.playtool.service.IPlayToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Override
    public SetCurrentGameResponse setCurrentGame(Long matchId) {

        log.info("Start set current game");

        if (playerStagingRepository.getGameInfoList() == null || playerStagingRepository.getGameInfoList().isEmpty()) {
            log.info("Game info list is empty");
            throw new BusinessLogicException(
                    ResponseStatusMsg.GAME_INFO_NOT_FOUND.getCode(),
                    ResponseStatusMsg.GAME_INFO_NOT_FOUND.getMsg()
            );
        }

        GameInfo gameInfo = playerStagingRepository.findFirstGameInfoByMatchId(matchId);

        if (gameInfo == null) {
            log.info("Not have game info with that matchId");
            throw new BusinessLogicException(
                    ResponseStatusMsg.GAME_INFO_NOT_FOUND.getCode(),
                    ResponseStatusMsg.GAME_INFO_NOT_FOUND.getMsg()
            );
        }

        log.info("Set current game info successfully");
        playerStagingRepository.setCurrentGameInfo(gameInfo);

        MapState currentMapState = null;
        try {
            currentMapState = iHostServerClient.getRecentMapState(
                    playerStagingRepository.getToken(),
                    gameInfo.getId().toString()
            );
            log.info("Get current map state from server successfully");
        } catch (Exception e) {
            log.info("Get current map state from server failed");
        }
        if (currentMapState == null) throw new BusinessLogicException(
                ResponseStatusMsg.GET_MAP_STATE_FAILED.getCode(),
                ResponseStatusMsg.GET_MAP_STATE_FAILED.getMsg()
        );

        playerStagingRepository.setRecentMapState(currentMapState);

        SetCurrentGameResponse setCurrentGameResponse = new SetCurrentGameResponse();
        setCurrentGameResponse.setCurrentMapState(currentMapState);

        return setCurrentGameResponse;
    }

    @Override
    public GetRecentTokenRequest getRecentToken() {
        String token = playerStagingRepository.getToken();
        if (token == null || StringUtils.isEmpty(token)) {
            log.info("Token is not exist");
            throw new BusinessLogicException(
                    ResponseStatusMsg.NOT_FOUND_TOKEN.getCode(),
                    ResponseStatusMsg.NOT_FOUND_TOKEN.getMsg()
            );
        }
        log.info("Get token is exist");

        GetRecentTokenRequest getRecentTokenRequest = new GetRecentTokenRequest();
        getRecentTokenRequest.setRecentToken(token);
        return getRecentTokenRequest;
    }

    @Override
    public GetAllGameResponse getAllGame() {
        List<GameInfo> gameInfoList = playerStagingRepository.getGameInfoList();

        if (gameInfoList == null || gameInfoList.isEmpty()) {
            log.info("Game info is not found, {}", gameInfoList);
            throw new BusinessLogicException(
                    ResponseStatusMsg.GAME_INFO_NOT_FOUND.getCode(),
                    ResponseStatusMsg.GAME_INFO_NOT_FOUND.getMsg()
            );
        }

        log.info("Find all game info successfully {}", gameInfoList);

        GetAllGameResponse getAllGameResponse = new GetAllGameResponse();
        getAllGameResponse.setListGame(gameInfoList);
        return getAllGameResponse;
    }

    @Override
    public UpdateGameStateResponse updateGameState() {

        log.info("Start update game state");

        String token = playerStagingRepository.getToken();

        if (token == null || StringUtils.isEmpty(token)) {
            log.info("Token is null");
            throw new BusinessLogicException(
                    ResponseStatusMsg.NOT_FOUND_TOKEN.getCode(),
                    ResponseStatusMsg.NOT_FOUND_TOKEN.getMsg()
            );
        }

        GameInfo gameInfo = playerStagingRepository.getCurrentGameInfo();

        if (gameInfo == null) {
            log.info("Recent game info is null");
            throw new BusinessLogicException(
                    ResponseStatusMsg.GAME_INFO_NOT_FOUND.getCode(),
                    ResponseStatusMsg.GAME_INFO_NOT_FOUND.getMsg()
            );
        }

        MapState mapState = iHostServerClient.getRecentMapState(token, gameInfo.getId().toString());

        if (mapState == null) {
            log.info("Get mapState failed");
            throw new BusinessLogicException(
                    ResponseStatusMsg.GET_MAP_STATE_FAILED.getCode(),
                    ResponseStatusMsg.GET_MAP_STATE_FAILED.getMsg()
            );
        }

        playerStagingRepository.setRecentMapState(mapState);

        UpdateGameStateResponse updateGameStateResponse = new UpdateGameStateResponse();
        updateGameStateResponse.setCurrentMapState(mapState);

        return updateGameStateResponse;
    }

}
