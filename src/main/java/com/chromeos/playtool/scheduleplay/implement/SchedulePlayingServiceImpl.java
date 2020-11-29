package com.chromeos.playtool.scheduleplay.implement;

import com.chromeos.playtool.botchromeos.FlowMatchingBot;
import com.chromeos.playtool.botchromeos.MonteCBot;
import com.chromeos.playtool.botchromeos.IBotChromeOS;
import com.chromeos.playtool.common.model.GameInfo;
import com.chromeos.playtool.common.model.MapState;
import com.chromeos.playtool.common.model.RequestActionList;
import com.chromeos.playtool.constant.ResponseStatusMsg;
import com.chromeos.playtool.domain.EmptyResponseData;
import com.chromeos.playtool.exception.BusinessLogicException;
import com.chromeos.playtool.hostserver.IHostServerClient;
import com.chromeos.playtool.repositories.PlayerStagingRepository;
import com.chromeos.playtool.scheduleplay.ISchedulePlayingService;
import com.chromeos.playtool.service.IPlayToolService;
import com.chromeos.playtool.service.implement.PlayToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SchedulePlayingServiceImpl implements ISchedulePlayingService {

    private final PlayerStagingRepository playerStagingRepository;

    private final IBotChromeOS flowMatchingBot;

    private final IBotChromeOS monteCBot;

    private final IHostServerClient iHostServerClient;

    private final List<CompletableFuture<EmptyResponseData>> listWaiting;

    public SchedulePlayingServiceImpl(
            PlayerStagingRepository playerStagingRepository,
            FlowMatchingBot flowMatchingBot,
            MonteCBot monteCBot,
            IHostServerClient iHostServerClient
    ) {
        this.playerStagingRepository = playerStagingRepository;
        this.flowMatchingBot = flowMatchingBot;
        this.monteCBot = monteCBot;
        this.iHostServerClient = iHostServerClient;
        this.listWaiting = new ArrayList<>();
    }

    private MapState fetchMapState(String token, String matchId) {
        MapState mapState = null;
        log.info("Start fetch mapState");
        try {
            mapState = iHostServerClient.getRecentMapState(token, matchId);
        } catch (Exception e) {
            log.info(e.getMessage(), e.getCause());
        }

        if (mapState == null) {
            log.info("Failed to get mapState");
            throw new BusinessLogicException(
                    ResponseStatusMsg.GET_MAP_STATE_FAILED.getCode(),
                    ResponseStatusMsg.GET_MAP_STATE_FAILED.getMsg()
            );
        }

        return mapState;
    }

    private void playBot(IBotChromeOS iBotChromeOS, String token, GameInfo gameInfo) {

        MapState mapState = fetchMapState(token, gameInfo.getId().toString());

        RequestActionList flowMatchingBotDecision = iBotChromeOS.botMakeDecision(mapState, gameInfo);

        try {
            iHostServerClient.sendActionToServer(
                    token,
                    gameInfo.getId().toString(),
                    flowMatchingBotDecision
            );
        } catch (Exception e) {
            log.info("[flowMatchingBotDecision] Send action failed");
        }
    }

    @Override
    public void startASchedulePlaying() {
        GameInfo gameInfo = playerStagingRepository.getCurrentGameInfo();
        String token = playerStagingRepository.getToken();

        if (gameInfo == null)
            throw new BusinessLogicException(
                    ResponseStatusMsg.GAME_INFO_NOT_FOUND.getCode(),
                    ResponseStatusMsg.GAME_INFO_NOT_FOUND.getMsg()
            );

        MapState mapState = fetchMapState(token, gameInfo.getId().toString());
        Long currentUnixTime = Instant.now().toEpochMilli();
        Long startUnixTime = mapState.getStartedAtUnixTime();
        Long totalTimePerTurn = gameInfo.getTurnMillis() + gameInfo.getIntervalMillis();
//        Long remainTimeInRecentTurn = (currentUnixTime - startUnixTime) % (gameInfo.getTurnMillis() + gameInfo.getIntervalMillis());
        Long recentTurn = (currentUnixTime - startUnixTime) / totalTimePerTurn ;
        log.info("recentTurn: {}", recentTurn);
        log.info("recentTurn double: {}", ((double) currentUnixTime - startUnixTime) / totalTimePerTurn);


        playBot(flowMatchingBot, token, gameInfo);
        long epsilon = 200L;

        for (long i = recentTurn + 1; i < gameInfo.getTurns(); i++) {
            Long currentTurn = i;
            Long startTurnTime = startUnixTime + i * totalTimePerTurn;
            CompletableFuture<EmptyResponseData> future = new CompletableFuture<>();
            epsilon += 80;
            future.completeAsync(
                    () -> {
                        playBot(flowMatchingBot, token, gameInfo);
                        log.info("[Schedule] Send action for turn: {}", currentTurn);
                        return new EmptyResponseData();
                    },
                    CompletableFuture.delayedExecutor(
                            startTurnTime - currentUnixTime + epsilon,
                            TimeUnit.MILLISECONDS
                    )
            );
            listWaiting.add(future);
        }
    }

    @Override
    public void stopAllSchedulePlaying() {
        log.info("Stop all schedule playing");
        for (CompletableFuture<?> temp : listWaiting) temp.cancel(true);
        listWaiting.clear();
    }
}
