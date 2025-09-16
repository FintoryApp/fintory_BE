package com.fintory.domain.consulting.dto;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.consulting.model.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;



@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetail {
    private Long id;
    private String reportMonth;
    private InvestmentStyle investmentStyle;
    private List<InvestmentArea> investmentArea;
    private InvestmentSummary investmentSummary;
    private TopStock topStock;
    private BottomStock bottomStock;
    private String advice;

    public static Report toReport(ReportDetail reportDetail, String reportJson, Child child) {

            return Report.builder()
                    .riskType(reportDetail.getInvestmentStyle().getInvestmentStyle())
                    .reportMonth(reportDetail.getReportMonth())
                    .advice(reportDetail.getAdvice())
                    .reportJson(reportJson)
                    .child(child)
                    .build();
    }

    public static ReportDetail from(Report report, TradingReport tradingReportDto) {

        return ReportDetail.builder()
                .id(report.getId())
                .reportMonth(tradingReportDto.getReportMonth())
                .investmentStyle(tradingReportDto.getInvestmentStyle())
                .investmentArea(tradingReportDto.getInvestmentArea())
                .investmentSummary(tradingReportDto.getInvestmentSummary())
                .topStock(tradingReportDto.getTopStock())
                .bottomStock(tradingReportDto.getBottomStock())
                .advice(report.getAdvice())
                .build();
    }

}
