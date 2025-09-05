package com.fintory.domain.stock.service.korean;

public interface KoreanStockRankService {

    /**
     *
     * 모든 국내 주식의 시가총액, 거래량, 등락률을 조회하여
     * 순위를 데이터베이스에 저장
     *
     * <p>다음 시점에 자동으로 호출됩니다:</p>
     * <ul>
     *   <li>애플리케이션 시작 시</li>
     *   <li>밤 12시 </li>
     * </ul>
     *
     */
    public void initiateKoreanStockRank();
}
