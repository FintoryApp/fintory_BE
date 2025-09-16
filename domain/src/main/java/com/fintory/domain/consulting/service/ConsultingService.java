package com.fintory.domain.consulting.service;


import com.fintory.domain.child.model.Child;
import com.fintory.domain.consulting.dto.AiResponse;
import com.fintory.domain.consulting.dto.ReportDetail;
import com.fintory.domain.portfolio.model.StockTransaction;

import java.util.List;

public interface ConsultingService {


    /**
     *  GPT API로 응답 요청
     * @param stockTransactions 거래 내역
     * @return 원본 응답을 parse한 AiResponse(투자 스타일, 조언)
     */
    AiResponse getConsulting(List<StockTransaction> stockTransactions);

    /**
     * 월간 리포트 생성 및 저장하는 통합 메소드
     * @param stockTransactions
     * @param child
     */
    void saveReportDetail(List<StockTransaction> stockTransactions, Child child);

    /**
     * 특정 날짜의 리포트 조회
     * @param date
     * @param child
     * @return 전체 리포트에 들어갈 내용
     */
    ReportDetail getConsultingByDate(String date, Child child);

}
