package com.jiyun.baro.intern.dto.response;

import com.jiyun.baro.intern.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class ErrorResponse {

    private final ErrorDetails error;

    private ErrorResponse(ErrorCode errorCode) {
        this.error = new ErrorDetails(errorCode.getCode(), errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }

    @Getter
    @RequiredArgsConstructor
    private static class ErrorDetails() {
        private final String code;
        private final String message;
    }

}
