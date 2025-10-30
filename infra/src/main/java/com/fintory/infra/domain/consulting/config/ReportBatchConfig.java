package com.fintory.infra.domain.consulting.config;

import com.fintory.domain.alarm.model.NotificationType;
import com.fintory.domain.alarm.service.AlarmService;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.consulting.service.ConsultingService;
import com.fintory.domain.portfolio.model.StockTransaction;
import com.fintory.infra.domain.child.repository.ChildRepository;
import com.fintory.infra.domain.consulting.repository.ReportRepository;
import com.fintory.infra.domain.portfolio.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ReportBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ConsultingService consultingService;
    private final StockTransactionRepository stockTransactionRepository;
    private final ChildRepository childRepository;
    private final ReportRepository reportRepository;
    private final AlarmService alarmService;

    // Batch Job 설정
    @Bean
    public Job monthlyReportGenerationJob(){
        return new JobBuilder("monthlyReportGenerationJob", jobRepository)
                .start(generateReportsStep())
                .build();
    }

    @Bean
    public Step generateReportsStep(){
        return new StepBuilder("generateReportsStep", jobRepository)
                .<Child, Child>chunk(10, transactionManager)
                .reader(childItemReader())
                .processor(reportProcessor())
                .writer(reportItemWriter())
                .build();
    }


    @Bean
    @StepScope
    public ItemReader<Child> childItemReader(){
        List<Child> children = childRepository.findTop20ByOrderByIdAsc(); //원래는 findAll
        return new ListItemReader<>(children);
    }


    @Bean
    public ItemProcessor<Child, Child> reportProcessor(){
        return child -> {
            try {
                // 이번 달 기준으로 날짜 범위 설정
                YearMonth currentMonth = YearMonth.now();
                LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
                LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

                // 이미 이번 달 리포트가 존재하는지 확인
                boolean reportExists = reportRepository.findByReportMonthAndChild(
                        currentMonth.toString(), child).isPresent();

                if (reportExists) {
                    log.info("리포트가 이미 존재합니다. {}, month: {}", child.getId(), currentMonth);
                    return null;
                }

                // 이번 달 거래 내역 조회
                List<StockTransaction> stockTransactions = stockTransactionRepository
                        .findByExecutedAtBetweenAndAccount_ChildWithFetch(startOfMonth, endOfMonth, child);

                //기존 생성 메서드 호출 
                consultingService.saveReportDetail(stockTransactions, child);

                return child;

            } catch (Exception e) {
                log.error("리포트 생성 시 에러 발생 child: {}", child.getId(), e);
                // 에러 발생시 null 반환하여 해당 child만 스킵
                return null;
            }
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Child> reportItemWriter() {
        return children -> {
            // processor에서 이미 저장이 완료되므로 로그만 기록
            children.forEach(child -> {
                if (child != null) {
                    YearMonth now = YearMonth.now();
                    alarmService.pushMessage(child.getId(),NotificationType.REPORT,now + "자 Report 생성","Report가 생성되었습니다. 와서 확인하세요");
                }
            });
        };
    }
}