package com.fintory.domain.stock.service.overseas;

import com.fintory.domain.stock.dto.overseas.core.OverseasStockPriceHistory;
import com.fintory.domain.stock.dto.overseas.response.OverseasStockPriceHistoryResponse;

import java.util.List;

public interface OverseasStockPriceHistoryService {
    /**
     *
     * 모든 해외 주식의 기간별 시세를 조회하여 데이터베이스에 저장
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
     void saveStockPriceHistory();


    /**
     *
     *  Yahoo Finance API에서 지정된 종목의 기간별 시세(일/주/월/년) 데이터 조회 공통 메서드
     *
     * @param code 주식 종목 코드
     * @param interval 데이터 간격
     * @param range 조회 기간
     * @return 기간별 시세 이력 데이터 리스트
     */
     List<OverseasStockPriceHistory> getOverseasStockItemChatPrice(String code, String interval, String range);


    /**
     *
     * 지정된 종목의 기간별 시세(일/주/월/년) 데이터 조회
     * @param code 주식 종목 코드
     * @return 해당 주식의 기간별 시세(일/주/월/년)
     */
    OverseasStockPriceHistoryResponse getOverseasStockPriceHistory(String code);
}
