package com.fintory.infra.domain.attendance.serviceImpl;

import com.fintory.infra.domain.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceCleaner {

    private final AttendanceRepository attendanceRepository;

    @Scheduled(cron = "0 0 0 1 * ?")
    public void deleteLastMonthLogs() {
        LocalDate cutoffDate = LocalDate.now().withDayOfMonth(1);
        attendanceRepository.deleteByAttendanceDateBefore(cutoffDate);
        log.info("{} 매월 1일 자정에 이전 달 로그 자동 삭제", cutoffDate);
    }
}
