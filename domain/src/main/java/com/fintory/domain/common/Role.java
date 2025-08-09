package com.fintory.domain.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    CHILD("ROLE_CHILD", "어린이"),
    PARENT("ROLE_PARENT", "부모"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String title;
}
