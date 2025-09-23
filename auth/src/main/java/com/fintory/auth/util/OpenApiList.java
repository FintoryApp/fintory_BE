package com.fintory.auth.util;

public class OpenApiList {

    public static final String[] PUBLIC_URLS = {

            "/health",
            "/actuator/**",

            // h2
            "/h2-console/**",

            // user
            "/api/child/auth/login",

            "/api/child/auth/signup",

            "/api/child/auth/check-email",

            "/api/child/auth/social-login/google",

            "/api/child/auth/social-login/kakao",

            // refresh
            "/api/child/auth/reissue",


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
            "/api/child/trading",
            "/stock/**",
            "/ws/**",
            "/ws",
            "/ws-sockjs/**"

    };
}
