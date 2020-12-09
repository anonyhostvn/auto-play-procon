package com.chromeos.playtool.botchromeos;

import com.chromeos.playtool.botchromeos.algozoo.MinCost;
import com.chromeos.playtool.common.enums.MoveTypes;
import com.chromeos.playtool.common.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

@Slf4j
@Service
public class OlderFlowMatchingBot implements IBotChromeOS {

    private static final int INF = 1000000;
    private static final int[] dX = {-1, -1, -1, 0, 0, 1, 1, 1};
    private static final int[] dY = {-1, 0, 1, -1, 1, -1, 0, 1};

    private MapState map;
    private int teamId;

    private MinCost minCost;
    private int[][] dist = new int[25][25];
    private boolean[][] mark = new boolean[25][25];

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

    private int dfs(int u, int v) {
        mark[u][v] = true;
        int sumPoint = map.getPoint(u, v);
        if (map.getPoint(u, v) != 0 && map.getPoint(u, v) != teamId)
            sumPoint = 2 * map.getPoint(u, v);
        for (int dir = 0; dir < 8; dir++) {
            int newX = u + dX[dir];
            int newY = v + dY[dir];
            if (newX <= 0 || newX > map.getWidth() || newY <= 0 || newY > map.getHeight())
                continue;
            if (!mark[newX][newY] && map.getTile(newX, newY) != teamId && map.getPoint(newX, newY) >= 0)
                sumPoint += dfs(newX, newY);
        }
        return sumPoint;
    }

    private void bfs(int u, int v) {
        int[][] tempDist = new int[map.getWidth() + 5][map.getHeight() + 5];
        for (int i = 1; i <= map.getWidth(); i++)
            for (int j = 1; j <= map.getHeight(); j++) {
                tempDist[i][j] = INF;
                mark[i][j] = false;
            }
        Queue<Pair> queue = new LinkedList<>();
        queue.add(new Pair(u, v));
        tempDist[u][v] = -dfs(u, v);
        for (Treasure treasure : map.getTreasure())
            if (u == treasure.getX() && v == treasure.getY()) {
                tempDist[u][v] -= treasure.getPoint() * treasure.getPoint();
                break;
            }
        while (queue.size() > 0) {
            Pair pos = queue.peek();
            queue.remove();
            for (int dir = 0; dir < 8; dir++) {
                int newX = pos.first + dX[dir];
                int newY = pos.second + dY[dir];
                if (newX <= 0 || newX > map.getWidth() || newY <= 0 || newY > map.getHeight())
                    continue;
                if (map.getTile(newX, newY) == teamId && tempDist[newX][newY] > tempDist[pos.first][pos.second] + 1) {
                    tempDist[newX][newY] = tempDist[pos.first][pos.second] + 1;
                    queue.add(new Pair(newX, newY));
                }
            }
        }
        for (int i = 1; i <= map.getWidth(); i++)
            for (int j = 1; j <= map.getHeight(); j++)
                dist[i][j] = Math.min(dist[i][j], tempDist[i][j]);
    }

    private boolean hasObstacle(int x, int y) {
        for (Obstacle obstacle : map.getObstacles())
            if (x == obstacle.getX() && y == obstacle.getY())
                return true;
        return false;
    }

    private void addEdge(Agent agent) {
        // Positive tile
        for (int dir = 0; dir < 8; dir++) {
            int newX = agent.getX() + dX[dir];
            int newY = agent.getY() + dY[dir];
            if (newX <= 0 || newX > map.getWidth() || newY <= 0 || newY > map.getHeight())
                continue;
            if (map.getTile(newX, newY) != teamId && map.getPoint(newX, newY) >= 0 && !hasObstacle(newX, newY))
                minCost.addEdge(agent.getAgentID(), 100 + getId(newX, newY), -map.getPoint(newX, newY), 1);
        }
        // Negative
        for (int i = 1; i <= map.getWidth(); i++)
            for (int j = 1; j <= map.getHeight(); j++)
                dist[i][j] = INF;
        for (int i = 1; i <= map.getWidth(); i++)
            for (int j = 1; j <= map.getHeight(); j++)
                if (map.getTile(i, j) != teamId)
                    bfs(i, j);
        for (int dir = 0; dir < 8; dir++) {
            int newX = agent.getX() + dX[dir];
            int newY = agent.getY() + dY[dir];
            if (newX <= 0 || newX > map.getWidth() || newY <= 0 || newY > map.getHeight())
                continue;
            if (dist[newX][newY] < INF && !hasObstacle(newX, newY))
                minCost.addEdge(agent.getAgentID(), 100 + getId(newX, newY), 5000 + dist[newX][newY], 1);
        }
        // Stay command
        minCost.addEdge(agent.getAgentID(), 100 + getId(agent.getX(), agent.getY()), 10000, 1);
    }

    // source=900
    // sink=901
    // agentId=1-100
    // tileId=101-500
    @Override
    public RequestActionList botMakeDecision(MapState mapState, GameInfo gameInfo) {
        map = mapState;
        teamId = gameInfo.getTeamID();
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
        return this.botMakeDecision(mapState, gameInfo);
    }
}