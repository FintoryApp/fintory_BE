package com.fintory.auth.util;

public class OpenApiList {

    public static final String[] PUBLIC_URLS = {

            "/health",
            "/actuator/**",

            // h2
            "/h2-console/**",

            // user
            "/api/child/auth/login",
            "/api/parent/auth/login",

            "/api/child/auth/signup",
            "/api/parent/auth/signup",

            "/api/child/auth/check-email",
            "/api/parent/auth/check-email",

            "/api/child/auth/social-login/google",
            "/api/parent/auth/social-login/google",

            "/api/child/auth/social-login/kakao",
            "/api/parent/auth/social-login/kakao",

            // refresh
            "/api/child/auth/reissue",
            "/api/parent/auth/reissue",


            // swagger
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/v3/api-docs",
            "/v3/api-docs/**",

            // news
            "/api/news/crawl-test",

            //report
            "/api/child/consulting/test/consulting-report",

            //stock //REVIEW stock과 관련한 데이터는 로그인 없이도 조회 가능하도록
            "/api/child/stock/**",
            "/stock/**",
            "/ws/**",
            "/ws-sockjs/**"

    };
}
