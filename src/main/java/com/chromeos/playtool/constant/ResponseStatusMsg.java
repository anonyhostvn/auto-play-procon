package com.chromeos.playtool.constant;

import lombok.Getter;

@Getter
public enum ResponseStatusMsg {
    SUCCESS("200", "Success"),
    GENERAL_ERROR("500", "General Error"),
    GET_GAME_INFO_FAILED("H00001", "Get game info from host failed"),
    GAME_INFO_NOT_FOUND("H00002", "Not have game info, must set token first");

    private final String code;
    private final String msg;

    private ResponseStatusMsg(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
