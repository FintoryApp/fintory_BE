package com.fintory.domain.stock.service.korean;

import com.fintory.domain.stock.dto.korean.response.KoreanStockPriceHistoryResponse;
import com.fintory.domain.stock.dto.korean.core.KoreanStockPriceHistory;

import java.time.LocalDate;
import java.util.List;

public interface KoreanStockPriceHistoryService {

    /**
     *
     * 모든 국내 주식의 기간별 시세를 조회하여 데이터베이스에 저장
     *
     * <p>다음 시점에 자동으로 호출됩니다:</p>
     * <ul>
     *   <li>애플리케이션 시작 시</li>
     *   <li>장 마감 후 (15:30 이후)</li>
     * </ul>
     */
     void initiateStockPriceHistory();


    /**
     *
     *  KIS Developer에서 지정된 종목의 기간별 시세(일/주/월/년)를 조회하는 공통 메서드 (외부 API 의존)
     *
     * @param unit 조회 단위 ("D": 일봉, "W": 주봉, "M": 월봉, "Y": 년봉)
     * @param code 주식 종목 코드
     * @param localDate1 조회 시작일
     * @param localDate2 조회 종료일
     * @param orgAdjPrc 수정주가 반영 여부
     * @return 기간별 시세 이력 데이터 리스트
     */
     List<KoreanStockPriceHistory> getKoreanStockItemChatPrice(String unit, String code, LocalDate localDate1, LocalDate localDate2, String orgAdjPrc);


    /**
     *
     * 지정된 종목의 기간별 시세(일/주/월/년) 조회
     * @param code 주식 종목 코드
     * @return 해당 주식의 기간별 시세(일/주/월/년)
     */
     KoreanStockPriceHistoryResponse getKoreanStockPriceHistory(String code);

}
