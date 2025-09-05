package com.fintory.domain.stock.service.overseas;

import com.fintory.domain.stock.dto.overseas.response.OverseasLiveStockPriceResponse;
import com.fintory.domain.stock.model.Stock;
import reactor.core.publisher.Mono;

public interface OverseasLiveStockPriceService {


/**
 * 미국 주식의 현재가 데이터를 조회하고 데이터베이스에 저장함
 *
 * <p>다음 시점에서 자동으로 호출됩니다:</p>
 * <ul>
 *     <li>애플리케이션 시작 시</li>
 *     <li>미국 장마감 후</li>
 *     <ul>
 *         <li>정규시간: 한국시간 05:00 (동부표준시 16:00)</li>
 *         <li>서머타임: 한국시간 04:00 (동부서머시간 16:00)</li>
 *     </ul>
 * </ul>
 */
  void initLiveStockPrice();

    /**
     *
     * KIS Developer 서버로부터 해당 주식 종목의 현재가 시세를 조회
     * @param code 주식 종목 코드
     * @param token Redis에 저장된 KIS Developer 접근 토큰
     *
     */
 void getLiveStockPriceViaRestAPI(String code, String token);


    /**
     * DB에 저장된 현재가 조회
     * @param stock 주식 종목 코드
     * @return OverseasLiveStockPriceResponse ->  주식 현재가 시세 데이터
     */
 OverseasLiveStockPriceResponse getLiveStockPriceViaQuery(Stock stock);
}
