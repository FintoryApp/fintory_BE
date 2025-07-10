package com.fintory.child.consulting.service;

import com.fintory.domain.portfolio.model.StockTransaction;
import com.fintory.domain.portfolio.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultingSchedulerService {

    private final ConsultingService consultingService;
    private final StockTransactionRepository stockTransactionRepository;

    @Scheduled(cron= "0 00 12 L * ?")
    public void scheduledGenerateConsultingReport(){
        List<StockTransaction> stockTransactions = stockTransactionRepository.findByExecutedAtAfter(LocalDateTime.now().minusDays(30));

        consultingService.saveReportDetail(stockTransactions);
    }

}

