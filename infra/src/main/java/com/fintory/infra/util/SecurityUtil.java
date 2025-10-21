package com.fintory.infra.util;


import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.child.model.Child;
import com.fintory.infra.domain.child.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
/*
*  REVIEW
* 현재 로그인한 사용자에 대한 정보를 가져오기 위한 유틸리티.
* 의존성 문제로 인해 infra 모듈에서 생성
* 혹시 다른 방식으로 얻어올 수 있다면 리뷰 달아주세요
* */
public class SecurityUtil {

    private final ChildRepository childRepository;

    public Child getCurrentChild(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal(); //의존성 문제로 인해 CustomUserDetails 대신 UserDetails 사용

        String username = userDetails.getUsername();

        return childRepository.findByEmail(username)
                .orElseThrow(()-> new DomainException(DomainErrorCode.USER_NOT_FOUND));
    }
}
