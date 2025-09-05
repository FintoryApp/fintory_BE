package com.fintory.domain.stock.service.overseas;

public interface OverseasStockRankService {

    /**
     *
     * 모든 해외 주식의 시가총액, 거래량, 등락률을 조회하여
     * 순위를 데이터베이스에 저장
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
     *
     */
    public void saveOverseasStockRank();
}
