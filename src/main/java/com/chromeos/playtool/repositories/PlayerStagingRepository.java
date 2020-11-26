package com.chromeos.playtool.repositories;

import com.chromeos.playtool.common.model.GameInfo;
import com.chromeos.playtool.common.model.MapState;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Getter
@Setter
public class PlayerStagingRepository {

    private String token ;

    private List<GameInfo> gameInfoList;

    private GameInfo currentGameInfo;

    private MapState recentMapState;

    public PlayerStagingRepository() {
        this.token = null;
        gameInfoList = null;
    }

    public GameInfo findFirstGameInfoByMatchId(Long matchId) {
        for (GameInfo gameInfo : gameInfoList)
            if (Objects.equals(matchId, gameInfo.getId())) return gameInfo;
        return null;
    }

}
