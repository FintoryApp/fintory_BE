package com.fintory.domain.stock.service.token;

import com.fintory.domain.stock.dto.token.DBTokenResponse;
import reactor.core.publisher.Mono;

public interface DBTokenIssueService {


    /**
     *
     * DB API로부터 접근 토큰을 발급하는 메서드
     * @return String 접근 토큰, 만료까지 남은 시간
     */
     DBTokenResponse getNewDBToken();


}
