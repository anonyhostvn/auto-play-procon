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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
            log.info("Recent turn get from server: {}", mapState.getTurn());
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
//
//        CompletableFuture.runAsync(() -> {
//            RequestActionList flowMatchingBotDecision = iBotChromeOS.botMakeDecision(mapState, gameInfo);
//            try {
//                iHostServerClient.sendActionToServer(
//                        token,
//                        gameInfo.getId().toString(),
//                        flowMatchingBotDecision
//                );
//                log.info("Send action for turn: {}", mapState.getTurn());
//            } catch (Exception e) {
//                log.info("[flowMatchingBotDecision] Send action failed");
//            }
//        });

        Long timePerTurn = gameInfo.getIntervalMillis() + gameInfo.getTurnMillis();
        Long delayedTime = timePerTurn - (Instant.now().toEpochMilli() - mapState.getStartedAtUnixTime()) % timePerTurn + 100;
        Long remainTime = gameInfo.getTurnMillis() - (Instant.now().toEpochMilli() - mapState.getStartedAtUnixTime()) % timePerTurn - 200;
        log.info("Delayed time between next action: {}", delayedTime);
        log.info("Remain time in turn: {}", remainTime);
        log.info("Set async action");
        log.info(" ================ ");

        if (remainTime > 0) {
//            CompletableFuture<RequestActionList> calculateStep =  CompletableFuture.supplyAsync(
//                    () -> flowMatchingBot.botMakeDecision(mapState, gameInfo)
//            );

            CompletableFuture.supplyAsync(
                    () -> monteCBot.botMakeDecision(mapState, gameInfo, remainTime)
            ).thenApply(actionStep -> {
                log.info("[monteCBot] Bot lat dot is done!");
                try {
                    log.info("Start send action {}", actionStep);
                    Long t1 = System.currentTimeMillis();
                    iHostServerClient.sendActionToServer(
                            token,
                            gameInfo.getId().toString(),
                            actionStep
                    );
                    log.info("[monteCBot] Send action success for turn: {} in {} ms", mapState.getTurn(), System.currentTimeMillis() - t1);
                } catch (Exception e) {
                    log.info("[monteCBot] Send action failed");
                }
                return null;
            });

//            CompletableFuture.runAsync(
//                    () -> {
//                        try {
//                            RequestActionList requestActionList = calculateStep.get();
//                            log.info("Start send action {}", requestActionList);
//                            iHostServerClient.sendActionToServer(
//                                    token,
//                                    gameInfo.getId().toString(),
//                                    requestActionList
//                            );
//                            log.info("[flowMatchingBotDecision] Send action success for turn: {}", mapState.getTurn());
//                        } catch (Exception e) {
//                            log.info("[flowMatchingBotDecision] Send action failed");
//                        }
//                    },
//                    CompletableFuture.delayedExecutor(
//                            remainTime-2000,
//                            TimeUnit.MILLISECONDS
//                    )
//            );
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
