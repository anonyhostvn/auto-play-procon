package com.chromeos.playtool.botchromeos;

import com.chromeos.playtool.botchromeos.utils.UtilsCustom;
import com.chromeos.playtool.common.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

@Slf4j
@Service
public class StdIOMonteCBot implements IBotChromeOS {

    private Process process = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private Scanner inputScanner = null;

    @Autowired
    public StdIOMonteCBot() {
        startBotProcess();
    }

    private void startBotProcess() {
        boolean isStart = false;
        if (this.process != null) this.process.destroy();

        while (!isStart) {
            try {
                process = Runtime.getRuntime().exec("/Users/LongNH/Workspace/procon_one/cmake-build-release/procon" + " " + "1");
                inputStream = process.getInputStream();
                outputStream = process.getOutputStream();
                inputScanner = new Scanner(inputStream);
                isStart = true;
                log.info("Start bot oke");
            } catch (Exception e) {
                log.info("Running StdIOMonteCBot failed");
                log.error(e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public RequestActionList botMakeDecision(MapState mapState, GameInfo gameInfo) {
        return botMakeDecision(mapState, gameInfo, 10000L);
    }

    @Override
    public RequestActionList botMakeDecision(MapState mapState, GameInfo gameInfo, Long timeForRun) {

        for (int i = 0; i < mapState.getWidth(); i ++)
            for (int j = 0; j < mapState.getHeight(); j ++) {
                int newTitle = mapState.getTiled().get(j).get(i) == 0 ? 0 : gameInfo.getTeamID().equals(mapState.getTiled().get(j).get(i)) ? 1 : 2;
//                mapModel.setTile(i,j, newTitle);
                mapState.getTiled().get(j).set(i, newTitle);
            }

        for (Obstacle obstacle : mapState.getObstacles()) {
//            mapModel.setPoint(obstacle.getX(), obstacle.getY(), -999);
            mapState.getTiled().get(obstacle.getY()-1).set(obstacle.getX() - 1, -999);
        }

        for (Treasure treasure: mapState.getTreasure()) {
            int newStatus = treasure.getStatus() == 0 ? 0 : treasure.getStatus().equals(gameInfo.getTeamID()) ? 1 : 2;
            treasure.setStatus(newStatus);
        }

        String stateInput = UtilsCustom.buildArgumentsForStdMontCBot(mapState, gameInfo, timeForRun-200);
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.write(stateInput.getBytes());
            dataOutputStream.flush();
            log.info("Write input to bot successfully");
        } catch (Exception e) {
            log.info(e.getMessage(), e.getCause());
            log.info("Input to bot StdIOMonteCBot failed, restart bot!");
            startBotProcess();
        }

        String output = inputScanner.nextLine();
        log.info("Receive output from StdIOMonteCBot: {}", output);

        return new RequestActionList(UtilsCustom.extractActionsFromOrBot(output.split(" "), mapState, gameInfo));

//        List<RequestAction> requestActionLists = null;
//        try {
//            requestActionLists = CompletableFuture.supplyAsync(() -> {
//                try {
//                    String outputFromBot = new String(inputStream.readAllBytes());
//                    log.info("output from StdIOMonteCBot {} ", outputFromBot);
//                    return UtilsCustom.extractActionsFromOrBot(outputFromBot.split(" "), mapState, gameInfo);
//                } catch (Exception e) {
//                    log.info(e.getMessage(), e.getCause());
//                    log.info("Get output from bot failed, restart bot!");
//                }
//                return null;
//            }, CompletableFuture.delayedExecutor(
//                    timeForRun,
//                    TimeUnit.MILLISECONDS
//            )).get();
//        } catch (Exception e) {
//            log.info("Error when running StdIOMonteCBot");
//        }

//        return new RequestActionList(requestActionLists);
    }
}
