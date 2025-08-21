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
            "/api/auth/social-login/google",
            "/api/auth/social-login/kakao",

            // refresh
            "/api/auth/reissue",

            // swagger
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/v3/api-docs",
            "/v3/api-docs/**",

            // news
            "/api/news/crawl-test"

    };
}
