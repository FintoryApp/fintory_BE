package com.fintory.domain.stock.service.korean;

import com.fintory.domain.stock.dto.korean.response.*;

import java.util.List;


public interface KoreanStockService {



        /**
         * 국내 주식 시가총액 Top20 조회
         * @return 시가총액 순위 리스트
         */
         List<KoreanMarketCapResponse> getKoreanMarketCapTop20();

        /**
         * 국내 주식 등락률 Top20 조회
         * @return 등락률 순위 리스트
         */
        public List<KoreanROCResponse> getKoreanROCTop20();

        /**
         * 국내 주식 거래량 Top20 조회
         * @return 거래량 순위 리스트
         */
        List<KoreanRankResponse> getKoreanTradingVolumeTop20();


        /**
         * 국내 주식 가격 이력 조회
         * @param code 주식 코드
         * @return 가격 이력 정보
         */
        KoreanStockPriceHistoryResponse getKoreanStockPriceHistory(String code);


        /**
         *  국내 주식 현재가 조회
         * @param code 주식 코드
         * @return 현재가 시세 데이터
         */
        KoreanLiveStockPriceResponse getLiveStockPrice(String code);
}

