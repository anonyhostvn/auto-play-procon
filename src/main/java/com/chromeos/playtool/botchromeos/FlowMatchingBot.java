package com.chromeos.playtool.botchromeos;

import com.chromeos.playtool.botchromeos.algozoo.MinCost;
import com.chromeos.playtool.botchromeos.utils.ScoringService;
import com.chromeos.playtool.common.enums.MoveTypes;
import com.chromeos.playtool.common.model.*;
import com.chromeos.playtool.common.util.JsonUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

@Slf4j
@Service
public class FlowMatchingBot implements IBotChromeOS {
    private static final int INF = 1000000;
    private static final int STEP_PENALTY = 1;
    private static final int[] dX = {-1, -1, -1, 0, 0, 1, 1, 1, 0};
    private static final int[] dY = {-1, 0, 1, -1, 1, -1, 0, 1, 0};

    private MapState map;
    private int teamId;

    private MinCost minCost;
    private int[][] dist = new int[25][25];
    private boolean[][] mark = new boolean[25][25];

    private int[][] gapPointMap = new int[25][25];

    private int getId(int x, int y) {
        return (x - 1) * map.getHeight() + y;
    }

    private Pair getCoordinate(int id) {
        int x = id / map.getHeight() + 1;
        int y = id % map.getHeight();
        if (y == 0) {
            x--;
            y += map.getHeight();
        }
        return new Pair(x, y);
    }

    private void bfs(int u, int v) {
        int[][] tempDist = new int[map.getWidth() + 5][map.getHeight() + 5];
        for (int i = 1; i <= map.getWidth(); i++)
            for (int j = 1; j <= map.getHeight(); j++) {
                tempDist[i][j] = 0;
                mark[i][j] = false;
            }
        Queue<Pair> queue = new LinkedList<>();
        queue.add(new Pair(u, v));
        tempDist[u][v] = -calculateGapPoint(u, v);
        for (Treasure treasure : map.getTreasure())
            if (treasure.getStatus().equals(0) && u == treasure.getX() && v == treasure.getY()) {
                tempDist[u][v] -= treasure.getPoint() * treasure.getPoint();
                break;
            }
        if (tempDist[u][v] > 0)
            return;
        while (queue.size() > 0) {
            Pair pos = queue.peek();
            queue.remove();
            for (int dir = 0; dir < 8; dir++) {
                int newX = pos.first + dX[dir];
                int newY = pos.second + dY[dir];
                if (newX <= 0 || newX > map.getWidth() || newY <= 0 || newY > map.getHeight())
                    continue;
                if (tempDist[newX][newY] > tempDist[pos.first][pos.second] + STEP_PENALTY) {
                    tempDist[newX][newY] = tempDist[pos.first][pos.second] + STEP_PENALTY;
                    queue.add(new Pair(newX, newY));
                }
            }
        }
        for (int i = 1; i <= map.getWidth(); i++)
            for (int j = 1; j <= map.getHeight(); j++)
                dist[i][j] += tempDist[i][j];
    }

    private boolean hasObstacle(int x, int y) {
        for (Obstacle obstacle : map.getObstacles())
            if (x == obstacle.getX() && y == obstacle.getY())
                return true;
        return false;
    }

    int calculateGapPoint(int x, int y) {
        try {
            if (gapPointMap[x][y] != -INF)
                return gapPointMap[x][y];
            int gapBefore = 0, opponentTeamId = 0;
            for (Team team : map.getTeams())
                if (team.getTeamID().equals(teamId))
                    gapBefore += 10 * team.getTilePoint() + team.getAreaPoint();
                else {
                    opponentTeamId = team.getTeamID().intValue();
                    gapBefore -= 10 * team.getTilePoint() + team.getAreaPoint();
                }
            // Calculate gap point
            String json = new Gson().toJson(map);
            MapState newMap = JsonUtil.readMapFromFile(json);
            if (newMap.getTile(x, y) == 0)
                newMap.setTile(x, y, teamId);
            else if (newMap.getTile(x, y) != teamId)
                newMap.setTile(x, y, 0);
            else
                return 0;
            ScoringService scoringService = new ScoringService();
            scoringService.setMap(newMap);
            int gapAfter = 10 * scoringService.scoringTilePoint(teamId) + scoringService.scoringAreaPoint(teamId)
                    - 10 * scoringService.scoringTilePoint(opponentTeamId) - scoringService.scoringAreaPoint(opponentTeamId);
            return gapPointMap[x][y] = gapAfter - gapBefore;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return 0;
    }

    private void addEdge(Agent agent) {
        try {
            // Adjacent tiles
            for (int dir = 0; dir < 8; dir++) {
                int newX = agent.getX() + dX[dir];
                int newY = agent.getY() + dY[dir];
                if (newX <= 0 || newX > map.getWidth() || newY <= 0 || newY > map.getHeight() || map.getTile(newX, newY) == teamId)
                    continue;
                int bonusPoint = calculateGapPoint(newX, newY);
                if (bonusPoint > 0 && !hasObstacle(newX, newY))
                    minCost.addEdge(agent.getAgentID(), 100 + getId(newX, newY), -bonusPoint, 1);
            }
            // Far tiles
            for (int i = 1; i <= map.getWidth(); i++)
                for (int j = 1; j <= map.getHeight(); j++)
                    if (calculateGapPoint(i, j) < 0)
                        dist[i][j] = calculateGapPoint(i, j) * calculateGapPoint(i, j);
                    else
                        dist[i][j] = 0;
            for (int i = 1; i <= map.getWidth(); i++)
                for (int j = 1; j <= map.getHeight(); j++)
                    bfs(i, j);
            for (int dir = 0; dir < 9; dir++) {
                int newX = agent.getX() + dX[dir];
                int newY = agent.getY() + dY[dir];
                if (newX <= 0 || newX > map.getWidth() || newY <= 0 || newY > map.getHeight())
                    continue;
                if (!hasObstacle(newX, newY))
                    minCost.addEdge(agent.getAgentID(), 100 + getId(newX, newY), INF + dist[newX][newY], 1);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    // source=900
    // sink=901
    // agentId=1-100
    // tileId=101-500
    @Override
    public RequestActionList botMakeDecision(MapState mapState, GameInfo gameInfo) {
        map = mapState;
        teamId = gameInfo.getTeamID();
        for (int i = 0; i < 24; i++)
            for (int j = 0; j < 24; j++)
                gapPointMap[i][j] = -INF;
        log.info("PROCON:TURN:" + map.getTurn());
        int source = 900, sink = 901, send = map.getTeams().get(0).getAgents().size();
        minCost = new MinCost(1000, source, sink, send);
        for (Team team : map.getTeams())
            if (team.getTeamID() == teamId) {
                // Add source edges
                for (Agent agent : team.getAgents())
                    minCost.addEdge(source, agent.getAgentID(), 0, 1);
                // Add sink edges
                for (int i = 1; i <= map.getWidth(); i++)
                    for (int j = 1; j <= map.getHeight(); j++)
                        minCost.addEdge(100 + getId(i, j), sink, 0, 1);
                for (Agent agent : team.getAgents())
                    addEdge(agent);
                minCost.minCost();
                RequestActionList requests = new RequestActionList();
                requests.setActions(new ArrayList<>());
                for (Agent agent : team.getAgents()) {
                    int id = minCost.find(agent.getAgentID()) - 100;
                    Pair pos = getCoordinate(id);
                    RequestAction request = new RequestAction();
                    request.setAgentID(agent.getAgentID());
                    request.setDx(pos.first - agent.getX());
                    request.setDy(pos.second - agent.getY());
                    if (map.getTile(pos.first, pos.second) == 0 || map.getTile(pos.first, pos.second) == teamId)
                        request.setType(MoveTypes.MOVE);
                    else
                        request.setType(MoveTypes.REMOVE);
                    requests.getActions().add(request);
                }
                return requests;
            }
        return null;
    }

    @Override
    public RequestActionList botMakeDecision(MapState mapState, GameInfo gameInfo, Long timeForRun) {
        return botMakeDecision(mapState, gameInfo);
    }
}
