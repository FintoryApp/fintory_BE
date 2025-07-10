package com.fintory.child.consulting.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.common.exception.BaseException;
import com.fintory.common.exception.ErrorCode;
import com.fintory.domain.consulting.model.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.util.List;

import static com.fintory.child.consulting.dto.TradingReport.fromReportDetail;

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

    public static Report toReport(ReportDetail reportDetail) {

            String reportJson = fromReportDetail(reportDetail);
            return Report.builder()
                    .riskType(reportDetail.getInvestmentStyle().getInvestmentStyle())
                    .reportMonth(reportDetail.getReportMonth())
                    .advice(reportDetail.getAdvice())
                    .reportJson(reportJson)
                    .build();
    }

    public static ReportDetail fromReport(Report report) {
        try {
            System.out.println("=== JSON 디버깅 시작 ===");
            ObjectMapper objectMapper = new ObjectMapper();

            TradingReport tradingReportDto = objectMapper.readValue(
                    report.getReportJson(),
                    TradingReport.class
            );
            System.out.println(tradingReportDto);
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
        } catch (JsonProcessingException e) {
            System.out.println("JSON 파싱 에러: " + e.getMessage());
            e.printStackTrace(); // 전체 스택 트레이스 출력
            throw new BaseException(ErrorCode.JSON_PROCESSING_FAILED);
        }
    }
}
