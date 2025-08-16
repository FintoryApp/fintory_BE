package com.fintory.auth.util;

public class OpenApiList {

    public static final String[] PUBLIC_URLS = {

            "/health",
            "/actuator/**",

            // h2
            "/h2-console/**",

            // user
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/check-email",

            // refresh
            "/api/auth/reissue",

            // swagger
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/v3/api-docs",
            "/v3/api-docs/**",

            // news
            "/api/news/crawl-test",

            //stock
            //NOTE 시중에 나와있는 앱들은 로그인 없이도 주식 데이터 확인이 가능해서 일단 열어둠. 추후 변경 가능
            "/api/child/stock/**"

    };
}
