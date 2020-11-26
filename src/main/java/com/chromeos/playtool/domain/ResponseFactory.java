package com.chromeos.playtool.domain;

import com.chromeos.playtool.constant.ResponseStatusMsg;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseFactory {
    private ResponseFactory() {}


    public static <T> ResponseEntity<GeneralResponse<T>> success(T data) {
        GeneralResponse<T> generalResponse = new GeneralResponse<>();
        ResponseStatusCustom responseStatus = new ResponseStatusCustom();
        responseStatus.setCode(ResponseStatusMsg.SUCCESS.getCode());
        responseStatus.setMessage(ResponseStatusMsg.SUCCESS.getMsg());
        generalResponse.setData(data);
        return ResponseEntity.ok(generalResponse);
    }

    public static ResponseEntity<GeneralResponse<EmptyResponseData>> error(HttpStatus httpStatus, String code, String msg) {
        GeneralResponse<EmptyResponseData> generalResponse = new GeneralResponse<>();
        ResponseStatusCustom responseStatus = new ResponseStatusCustom();
        responseStatus.setCode(code);
        responseStatus.setMessage(msg);
        generalResponse.setStatus(responseStatus);
        return new ResponseEntity<>(generalResponse, httpStatus);
    }
}
