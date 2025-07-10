package com.fintory.child.consulting.controller;

import com.fintory.child.consulting.dto.ReportDetail;
import com.fintory.child.consulting.service.ConsultingSchedulerService;
import com.fintory.child.consulting.service.ConsultingService;
import com.fintory.common.response.ApiResponse;
import com.fintory.domain.portfolio.model.StockTransaction;
import com.fintory.domain.portfolio.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/consulting")
public class ConsultingController {

    private final ConsultingService consultingService;
    private final ConsultingSchedulerService schedulerService;
    private final StockTransactionRepository stockTransactionRepository;
    //테스트용
    @PostMapping("/test/consulting-report")
    public void triggerConsultingReport() {
         schedulerService.scheduledGenerateConsultingReport();
    }

    // 날짜별 리포트 조회
    @GetMapping("/{reportMonth}")
    public ApiResponse<ReportDetail> getConsultingByDate(@PathVariable String reportMonth){
        ReportDetail reportDetail =  consultingService.getConsultingByDate(reportMonth);
        return ApiResponse.ok(reportDetail);
    }

}
