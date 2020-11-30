package com.chromeos.playtool.botchromeos.utils;

import com.chromeos.playtool.common.enums.MoveTypes;
import com.chromeos.playtool.common.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class UtilsCustom {

    public static String buildArguments(MapState mapModel, GameInfo gameInfo, Long milliSecondForRun) {
        StringBuilder res = new StringBuilder();

        // Số turn còn lại
        res.append(gameInfo.getTurns() - mapModel.getTurn()).append(" ");

        // Thời gian được phép chạy
        res.append(milliSecondForRun).append(" ");

        // Kích thước bàn cờ
        res.append(mapModel.getWidth()).append(" ").append(mapModel.getHeight()).append(" ");

        // Điểm của mỗi ô trên bàn đấu
        for (List<Integer> row : mapModel.getPoints())
            for (Integer score : row) res.append(score).append(" ");

        // Màu của mỗi ô trên bàn đấu
        for (List<Integer> row : mapModel.getTiled())
            for (Integer tile : row) res.append(tile).append(" ");

        // Số lượng kho báu chưa được chiếm và toạ độ
        // TODO: CODE HERE

        long myTeamId = gameInfo.getTeamID();
        Team myTeam = null, opponentTeam = null;

        for (Team teamModel : mapModel.getTeams())
            if (teamModel.getTeamID() == myTeamId) myTeam = teamModel;
            else opponentTeam = teamModel;

        if (myTeam != null && opponentTeam != null) {
            res.append(myTeam.getAgents().size()).append(" ");
            // Số lượng agent và toạ độ các agent
            for (Agent agentModel : myTeam.getAgents())
                res.append(agentModel.getX()).append(" ").append(agentModel.getY()).append(" ");

            for (Agent agentModel : opponentTeam.getAgents())
                res.append(agentModel.getX()).append(" ").append(agentModel.getY()).append(" ");
        }

        res.append(mapModel.getTreasure().size()).append(" ");
        for (Treasure treasure : mapModel.getTreasure()) {
            res.append(treasure.getStatus()).append(" ");
            res.append(treasure.getPoint()).append(" ");
            res.append(treasure.getX()).append(" ");
            res.append(treasure.getY()).append(" ");
        }

        log.info("Params to run or bot {} ", res.toString());

        return res.toString();
    }

    public static List<RequestAction> extractActionsFromOrBot(String[] output, MapState mapState, GameInfo gameInfo) {

        Team myTeam = null;
        for (Team teamModel : mapState.getTeams())
            if (teamModel.getTeamID().equals(gameInfo.getTeamID().longValue())) myTeam = teamModel;

        List<RequestAction> botOrActionResponseList = new ArrayList<>();
        if (myTeam != null) {
            List<Agent> agentList = myTeam.getAgents();
            for (int i = 0; i < agentList.size(); i++) {
                Agent recentAgent = agentList.get(i);
                int actionType = Integer.parseInt(output[i * 3]);
                int dx = Integer.parseInt(output[i * 3 + 1]);
                int dy = Integer.parseInt(output[i * 3 + 2]);
                RequestAction requestAction = new RequestAction();
                String moveTypes =
                        actionType == 0 || actionType == 1
                                ? (
                                (dx == 0 && dy == 0) ? MoveTypes.STAY : MoveTypes.MOVE
                        ) : MoveTypes.REMOVE;
//                int newX = recentAgent.getX() + dx ;
//                int newY = recentAgent.getY() + dy ;
//                String moveTypes = null;
//                if (dx == 0 && dy == 0) moveTypes = MoveTypes.STAY;
//                else if (MapState.checkIsMove(newX-1, newY-1, mapState, gameInfo)) moveTypes = MoveTypes.MOVE;
//                else moveTypes = MoveTypes.REMOVE;
                requestAction.setAgentID(recentAgent.getAgentID());
                requestAction.setDx(dx);
                requestAction.setDy(dy);
                requestAction.setType(moveTypes);
                botOrActionResponseList.add(requestAction);
            }
        }

        return botOrActionResponseList;
    }

}
