package com.chromeos.playtool.models.response;

import com.chromeos.playtool.common.model.GameInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
public class SetTokenPlayToolResponse {

    @JsonProperty("listGame")
    public List<GameInfo> listGame;

}
