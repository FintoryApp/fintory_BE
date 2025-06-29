package com.fintory.common.exception;



public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode) {
      super(errorCode.getMessage());
      this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
      return errorCode;
    }
}
