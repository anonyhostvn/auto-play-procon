package com.chromeos.playtool.interceptor;


import com.chromeos.playtool.constant.ResponseStatusMsg;
import com.chromeos.playtool.domain.EmptyResponseData;
import com.chromeos.playtool.domain.GeneralResponse;
import com.chromeos.playtool.domain.ResponseFactory;
import com.chromeos.playtool.exception.BusinessLogicException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalDefaultExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity<GeneralResponse<EmptyResponseData>> defaultExceptionHandler(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseFactory.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ResponseStatusMsg.GENERAL_ERROR.getCode(),
                ResponseStatusMsg.GENERAL_ERROR.getMsg()
        );
    }

    @ExceptionHandler(value = BusinessLogicException.class)
    @ResponseBody
    public ResponseEntity<GeneralResponse<EmptyResponseData>> defaultBusinessLogicExceptionHandler(
            BusinessLogicException e
    ) {
        log.error(e.getMessage(), e);
        return ResponseFactory.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getCode(),
                e.getMessage()
        );
    }
}
