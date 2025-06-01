package com.campuspick.fintory.modules.news.domain.exception;

import com.campuspick.fintory.global.exception.BaseException;
import com.campuspick.fintory.global.exception.ErrorCode;

public class NewsNotFoundException extends BaseException {

    public NewsNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
