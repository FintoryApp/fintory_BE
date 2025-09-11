package com.fintory.domain.common;

import com.fintory.domain.child.model.LoginType;

public interface User {
    String getEmail();
    String getPassword();
    String getNickname();
    Role getRole();
    LoginType getLoginType();
}
