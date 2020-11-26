package com.chromeos.playtool.common.model;

import com.chromeos.playtool.common.util.ListUtil;
import lombok.Data;

import java.util.List;

@Data
public class MapState {

    private Integer width;

    private Integer height;

    private List<List<Integer>> points;

    private Long startedAtUnixTime;

    private Integer turn;

    private List<List<Integer>> tiled;

    private List<Team> teams;

    private List<Action> actions;

    private List<Obstacle> obstacles;

    private List<Treasure> treasure;

    // More methods
    public void initPoints() {
        points = ListUtil.init(points, width, height);
    }

    public int getPoint(int x, int y) {
        return ListUtil.get(points, x, y);
    }

    public void setPoint(int x, int y, int value) {
        points = ListUtil.set(points, x, y, value);
    }

    public void initTiled() {
        tiled = ListUtil.init(tiled, width, height);
    }

    public int getTile(int x, int y) {
        return ListUtil.get(tiled, x, y);
    }

    public void setTile(int x, int y, int value) {
        tiled = ListUtil.set(tiled, x, y, value);
    }

    public static Boolean checkIsMove(int x, int y, MapState mapState, GameInfo gameInfo) {
        return mapState.getTiled().get(y).get(x) == 0 || mapState.getTiled().get(y).get(x).equals(gameInfo.getTeamID());
    }
}
