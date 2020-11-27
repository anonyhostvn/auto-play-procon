package com.chromeos.playtool.utils;

import com.chromeos.playtool.common.model.MapState;
import com.chromeos.playtool.common.model.Team;

import java.util.Objects;

public class GameUtils {
    public static Long opponentTeamId(MapState mapState, Long teamId) {
        for (Team team : mapState.getTeams())
            if (!Objects.equals(team.getTeamID(), teamId))
                return team.getTeamID();
        return null;
    }
}
