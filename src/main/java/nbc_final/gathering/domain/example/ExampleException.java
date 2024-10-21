package nbc_final.gathering.domain.example;

import nbc_final.gathering.common.exception.ErrorCode;

public class ExampleException extends RuntimeException{

    public ExampleException(ErrorCode errorCode) {
        super(errorCode.getMessage());
    }

    // 스택 트레이스 기록 안 해서 성능 향상
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
