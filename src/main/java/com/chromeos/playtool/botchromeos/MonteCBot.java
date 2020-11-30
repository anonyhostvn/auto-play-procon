package com.chromeos.playtool.botchromeos;

import com.chromeos.playtool.botchromeos.utils.UtilsCustom;
import com.chromeos.playtool.common.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MonteCBot implements IBotChromeOS {

    @Override
    public RequestActionList botMakeDecision(MapState mapState, GameInfo gameInfo) {
        return botMakeDecision(mapState, gameInfo, 10000L);
    }

    @Override
    public RequestActionList botMakeDecision(MapState mapModel, GameInfo gameInfo, Long millisecondForRun) {

        for (int i = 0; i < mapModel.getWidth(); i ++)
            for (int j = 0; j < mapModel.getHeight(); j ++) {
                int newTitle = mapModel.getTiled().get(j).get(i) == 0 ? 0 : gameInfo.getTeamID().equals(mapModel.getTiled().get(j).get(i)) ? 1 : 2;
//                mapModel.setTile(i,j, newTitle);
                mapModel.getTiled().get(j).set(i, newTitle);
            }

        for (Obstacle obstacle : mapModel.getObstacles()) {
//            mapModel.setPoint(obstacle.getX(), obstacle.getY(), -999);
            mapModel.getTiled().get(obstacle.getY()-1).set(obstacle.getX() - 1, -999);
        }

        for (Treasure treasure: mapModel.getTreasure()) {
            int newStatus = treasure.getStatus() == 0 ? 0 : treasure.getStatus().equals(gameInfo.getTeamID()) ? 1 : 2;
            treasure.setStatus(newStatus);
        }

        try {
//            Process process = Runtime.getRuntime().exec(resourceService.getExecutableFilePath() + " " + BotUtils.buildArguments(mapModel, gameInfo));
            Process process = Runtime.getRuntime().exec("/Users/LongNH/Workspace/procon_one/cmake-build-release/procon" + " " + UtilsCustom.buildArguments(mapModel, gameInfo, millisecondForRun));
            List<RequestAction> requestActionModelList = UtilsCustom.extractActionsFromOrBot(new String(process.getInputStream().readAllBytes()).split(" "), mapModel, gameInfo);
            return new RequestActionList(requestActionModelList);
        } catch (Exception e) {
            log.error("Execute bot catch exception !");
            log.error(e.getMessage(), e.getCause());
        }
        return null;
    }
}
