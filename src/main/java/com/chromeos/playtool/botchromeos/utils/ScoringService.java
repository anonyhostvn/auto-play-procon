package com.chromeos.playtool.botchromeos.utils;


import com.chromeos.playtool.common.model.MapState;
import com.chromeos.playtool.common.model.Treasure;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    private final int[] dX = {0, 0, -1, 1};
    private final int[] dY = {-1, 1, 0, 0};

    private MapState map;

    private boolean[][] isMark;

    private boolean isReachBoundary;

    public void setMap(MapState map) {
        this.map = map;
    }

    private void dfs(int x, int y, int teamId) {
        if (x == 0 || x == map.getWidth() - 1 || y == 0 || y == map.getHeight() - 1)
            isReachBoundary = true;
        isMark[x][y] = true;
        for (int direction = 0; direction < 4; direction++) {
            int newX = x + dX[direction], newY = y + dY[direction];
            if (newX < 0 || newX >= map.getWidth() || newY < 0 || newY >= map.getHeight() || isMark[newX][newY] || map.getTile(newX + 1, newY + 1) == teamId)
                continue;
            dfs(newX, newY, teamId);
        }
    }

    public int scoringTilePoint(int teamId) {
        isMark = new boolean[map.getWidth()][map.getHeight()];
        int point = 0;
        for (int i = 0; i < map.getWidth(); i++)
            for (int j = 0; j < map.getHeight(); j++)
                if (map.getTile(i + 1, j + 1) == teamId)
                    point += map.getPoint(i + 1, j + 1);
        for (Treasure treasure : map.getTreasure())
            if (treasure.getStatus().equals(teamId))
                point += treasure.getPoint();
        return point;
    }

    public int scoringAreaPoint(int teamId) {
        isMark = new boolean[map.getWidth()][map.getHeight()];
        int point = 0;
        for (int i = 0; i < map.getWidth(); i++)
            for (int j = 0; j < map.getHeight(); j++)
                if (map.getTile(i + 1, j + 1) != teamId) {
                    for (int ti = 0; ti < map.getWidth(); ti++)
                        for (int tj = 0; tj < map.getHeight(); tj++)
                            isMark[ti][tj] = false;
                    isReachBoundary = false;
                    dfs(i, j, teamId);
                    if (!isReachBoundary)
                        point += Math.abs(map.getPoint(i + 1, j + 1));
                }
        return point;
    }
}

