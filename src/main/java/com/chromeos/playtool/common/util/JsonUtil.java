package com.chromeos.playtool.common.util;

import com.chromeos.playtool.common.model.GameInfo;
import com.chromeos.playtool.common.model.MapState;
import com.google.gson.Gson;

public class JsonUtil {

    public static GameInfo[] readGameInfosFromFile(String data) throws Exception {
        return new Gson().fromJson(data, GameInfo[].class);
    }

    public static MapState readMapFromFile(String data) throws Exception {
        return new Gson().fromJson(data, MapState.class);
    }
}