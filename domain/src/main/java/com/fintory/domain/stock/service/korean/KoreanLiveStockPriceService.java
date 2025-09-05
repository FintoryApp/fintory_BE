package com.fintory.domain.stock.service.korean;

import com.fintory.domain.stock.dto.korean.response.KoreanLiveStockPriceResponse;
import com.fintory.domain.stock.model.Stock;
import reactor.core.publisher.Mono;

public interface KoreanLiveStockPriceService {


    /**
     *
     * 모든 국내 주식의 현재가 데이터를 조회하여 데이터베이스에 저장함
     *
     * <p> 다음 시점에서 자동으로 호출됩니다</p>
     * <ul>
     *     <li>어플리케이션 시작 시 </li>
     *     <li>장 마감 후(15:30 이후)</li>
     * </ul>
     *
     */
     void initLiveStockPrice();

    /**
     *
     * 지정된 종목의 호가 데이터를 조회
     *
     * @param code 주식 종목 코드
     * @param token KIS Developer 서버 접근 토큰
     *
     */
     void getLiveStockPriceViaRestAPI(String code, String token);


    /**
     *
     * DB에 저장된 시세 데이터 조회
     * 장 마감 이후 프론트에서 현재가를 보여줄 때 사용(물론 리액트의 상태값에 값을 저장할 순 있겠지만 그렇지 못할 경우를 대비 -> 예를 들어 어플리케이션을 첫 시작했을 때 장마감 시간이라서 웹소켓으로 값을 못가지고 올 때"
     *
     * @param stock 주식 종목
     * @return 현재가, 변동된 가격, 변화률
     */
    KoreanLiveStockPriceResponse getLiveStockPriceViaQuery(Stock stock);
}
