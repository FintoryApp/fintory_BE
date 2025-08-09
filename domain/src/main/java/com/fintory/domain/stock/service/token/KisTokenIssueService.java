package com.fintory.domain.stock.service.token;

import com.fintory.domain.stock.dto.token.KisTokenResponse;
import com.fintory.domain.stock.dto.token.KisWebSocketTokenResponse;
import reactor.core.publisher.Mono;

public interface KisTokenIssueService {

    /**
     *
     * KIS Developer로부터 일반 접근 토큰 발급 메서드
     * @return 토큰 String, 만료까지 남은 시간, 만료 시기
     */
    public Mono<KisTokenResponse> getNewKisToken();

    /**
     *
     * KIS Developer로부터 웹소켓 접근 토큰 발급 메서드
     * @return 토큰 String, 만료까지 남은 시간, 만료 시기
     */
    public Mono<KisWebSocketTokenResponse> getNewWebSocketKisToken();


}
