package com.fintory.domain.consulting.dto;


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

    public static TradingReport fromReportDetail(ReportDetail reportDetail)  {
            return TradingReport.builder()
                    .reportMonth(reportDetail.getReportMonth())
                    .investmentStyle(reportDetail.getInvestmentStyle())
                    .investmentArea(reportDetail.getInvestmentArea())
                    .investmentSummary(reportDetail.getInvestmentSummary())
                    .topStock(reportDetail.getTopStock())
                    .bottomStock(reportDetail.getBottomStock())
                    .build();

    }
}
