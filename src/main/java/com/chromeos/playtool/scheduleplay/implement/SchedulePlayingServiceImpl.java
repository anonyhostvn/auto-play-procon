package com.chromeos.playtool.scheduleplay.implement;

import com.chromeos.playtool.botchromeos.FlowMatchingBot;
import com.chromeos.playtool.botchromeos.MonteCBot;
import com.chromeos.playtool.botchromeos.IBotChromeOS;
import com.chromeos.playtool.common.model.GameInfo;
import com.chromeos.playtool.common.model.MapState;
import com.chromeos.playtool.constant.ResponseStatusMsg;
import com.chromeos.playtool.domain.EmptyResponseData;
import com.chromeos.playtool.exception.BusinessLogicException;
import com.chromeos.playtool.hostserver.IHostServerClient;
import com.chromeos.playtool.repositories.PlayerStagingRepository;
import com.chromeos.playtool.scheduleplay.ISchedulePlayingService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
        log.info("---> Start fetch mapState <---");
        Long t1 = System.currentTimeMillis();
        try {
            mapState = iHostServerClient.getRecentMapState(token, matchId);
            log.info("Recent turn get from server: {}. GET map successfully in {} ms", mapState.getTurn(), System.currentTimeMillis() - t1);
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

    private void playBot(String token) {
        GameInfo gameInfo = playerStagingRepository.getCurrentGameInfo();
        MapState mapState = fetchMapState(token, gameInfo.getId().toString());

//        Long timePerTurn = gameInfo.getIntervalMillis() + gameInfo.getTurnMillis();
        Long timePerTurn = 2000 + gameInfo.getTurnMillis();
        Long delayedTime = timePerTurn - (Instant.now().toEpochMilli() - mapState.getStartedAtUnixTime()) % timePerTurn + 100;
        Long remainTime = gameInfo.getTurnMillis() - (Instant.now().toEpochMilli() - mapState.getStartedAtUnixTime()) % timePerTurn - 200;
        log.info("Delayed time between next action: {}", delayedTime);
        log.info("Remain time in turn: {}", remainTime);
        log.info("Set async action");
        Long startTime = System.currentTimeMillis();

        if (remainTime > 0) {
//            CompletableFuture.supplyAsync(
//                    () -> flowMatchingBot.botMakeDecision(mapState, gameInfo)
//            ).thenApply(actionStep -> {
//                log.info("[flowMatchingBot] Bot Hung is done!");
//                try {
//                    log.info("[flowMatchingBot] Start send action {}", actionStep);
//                    Long t1 = System.currentTimeMillis();
//                    iHostServerClient.sendActionToServer(
//                            token,
//                            gameInfo.getId().toString(),
//                            actionStep
//                    );
//                    log.info("[flowMatchingBot] Send action success for turn: {} in {} ms", mapState.getTurn(), System.currentTimeMillis() - t1);
//                } catch (Exception e) {
//                    log.info("[flowMatchingBot] Send action failed");
//                }
//                return null;
//            });

            String cloneMapState = new Gson().toJson(mapState);
            MapState cloneMapStateObj = new Gson().fromJson(cloneMapState,MapState.class);
            log.info("Time for clone new mapState: {} ms", System.currentTimeMillis() - startTime);

            CompletableFuture.supplyAsync(
                    () -> monteCBot.botMakeDecision(cloneMapStateObj, gameInfo, 5000L)
            ).thenApply(actionStep -> {
                log.info("[monteCBotLow] Bot lat dot is done in {} ms !", System.currentTimeMillis() - startTime);
                try {
                    log.info("[monteCBotLow] Start send action {}", actionStep);
                    Long t1 = System.currentTimeMillis();
                    iHostServerClient.sendActionToServer(
                            token,
                            gameInfo.getId().toString(),
                            actionStep
                    );
                    log.info("[monteCBotLow] Send action success for turn: {} in {} ms", mapState.getTurn(), System.currentTimeMillis() - t1);
                } catch (Exception e) {
                    log.info("[monteCBotLow] Send action failed");
                }
                log.info(" ================");
                return null;
            });

            CompletableFuture.supplyAsync(
                    () -> monteCBot.botMakeDecision(mapState, gameInfo, remainTime)
            ).thenApply(actionStep -> {
                log.info("[monteCBot] Bot lat dot is done in {} ms !", System.currentTimeMillis() - startTime);
                try {
                    log.info("[monteCBot] Start send action {}", actionStep);
                    long t1 = System.currentTimeMillis();
                    iHostServerClient.sendActionToServer(
                            token,
                            gameInfo.getId().toString(),
                            actionStep
                    );
                    log.info("[monteCBot] Send action success for turn: {} in {} ms", mapState.getTurn(), System.currentTimeMillis() - t1);
                } catch (Exception e) {
                    log.info("[monteCBot] Send action failed");
                }
                log.info(" ================ ");
                return null;
            });
        }

        CompletableFuture<EmptyResponseData> future = new CompletableFuture<>();
        future.completeAsync(
                () -> {
                    playBot(token);
                    return new EmptyResponseData();
                },
                CompletableFuture.delayedExecutor(
                        delayedTime,
                        TimeUnit.MILLISECONDS
                )
        );
        listWaiting.add(future);
    }

    @Override
    public void startASchedulePlaying() {
        String token = playerStagingRepository.getToken();
        playBot(token);
    }

    @Override
    public void stopAllSchedulePlaying() {
        log.info("Stop all schedule playing");
        for (CompletableFuture<?> temp : listWaiting) temp.cancel(true);
        listWaiting.clear();
    }
}
