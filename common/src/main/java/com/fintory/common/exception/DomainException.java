package com.fintory.common.exception;



public class DomainException extends RuntimeException {

    private final DomainErrorCode errorCode;

    public DomainException(DomainErrorCode errorCode) {
      super(errorCode.getMessage());
      this.errorCode = errorCode;
    }

    public DomainErrorCode getErrorCode() {
      return errorCode;
    }
}
