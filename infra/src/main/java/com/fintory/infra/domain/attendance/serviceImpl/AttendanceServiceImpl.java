package com.fintory.infra.domain.attendance.serviceImpl;

import com.fintory.domain.attendence.model.AttendanceLog;
import com.fintory.domain.attendence.service.AttendanceService;
import com.fintory.domain.child.model.Child;
import com.fintory.infra.domain.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;

    @Override
    public int check(Child child) {

        LocalDate today = LocalDate.now();
        // 출석 중복 여부
        boolean isAlreadyChecked = attendanceRepository.existsByChildAndAttendanceDate(child, today);
        if (!isAlreadyChecked) {
            AttendanceLog attendanceLog = new AttendanceLog(child, today);
            attendanceRepository.save(attendanceLog);
        }
        return calculateContinuousDays(child, today);
    }

    public int calculateContinuousDays(Child child, LocalDate today) {
        // 오늘 출석은 이미 처리됨
        int continuousDays = 0;
        LocalDate checkDate = today.minusDays(1); // 어제부터 시작

        // 어제가 이번 달이 아닐 경우 → 어차피 DB에 없음
        if (checkDate.getMonth() != today.getMonth()) {
            return 1; // 오늘만 출석한 것으로 간주
        }

        // 어제부터 거꾸로 연속 출석 체크
        while (attendanceRepository.existsByChildAndAttendanceDate(child, checkDate)) {
            continuousDays++;
            checkDate = checkDate.minusDays(1);

            // 이번 달 범위 안에서만 검사
            if (checkDate.getMonth() != today.getMonth()) break;
        }

        return continuousDays + 1; // 오늘 출석 포함
    }


}
