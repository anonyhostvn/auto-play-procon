package com.chromeos.playtool.botchromeos;

import com.chromeos.playtool.common.model.GameInfo;
import com.chromeos.playtool.common.model.MapState;
import com.chromeos.playtool.common.model.RequestActionList;

public interface IBotChromeOS {
    RequestActionList botMakeDecision(MapState mapState, GameInfo gameInfo);
}
