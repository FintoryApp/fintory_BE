package com.fintory.infra.domain.consulting.serviceImpl;

import com.fintory.infra.domain.portfolio.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultingSchedulerService {

    private final JobLauncher jobLauncher;
    private final Job monthlyReportGenerationJob;

    //NOTE 스케쥴러는 구현체가 하나이므로 인터페이스 생성X
    @Scheduled(cron= "0 00 10 L * ?")
    public void scheduledGenerateConsultingReport(){
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(monthlyReportGenerationJob, jobParameters);

            log.info("배치 작업 시작: {}", jobExecution.getStatus());
        } catch (Exception e) {
            log.error("배치 실행 중 오류 발생", e);
        }

    }

}

