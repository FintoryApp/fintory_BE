package com.fintory.child.consulting.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.common.exception.BaseException;
import com.fintory.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradingReport {
    private String reportMonth;
    private InvestmentStyle investmentStyle;
    private List<InvestmentArea> investmentArea;
    private InvestmentSummary investmentSummary;
    private TopStock topStock;
    private BottomStock bottomStock;
    private String advice;

    public static String fromReportDetail(ReportDetail reportDetail)  {
        try {
            TradingReport tradingReport = TradingReport.builder()
                    .investmentStyle(reportDetail.getInvestmentStyle())
                    .investmentArea(reportDetail.getInvestmentArea())
                    .investmentSummary(reportDetail.getInvestmentSummary())
                    .topStock(reportDetail.getTopStock())
                    .bottomStock(reportDetail.getBottomStock())
                    .build();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(tradingReport);
        }catch(JsonProcessingException e){
            throw new BaseException(ErrorCode.JSON_PROCESSING_FAILED);
        }
    }
}
