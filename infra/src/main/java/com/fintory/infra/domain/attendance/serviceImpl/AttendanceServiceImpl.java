package com.fintory.infra.domain.attendance.serviceImpl;

import com.fintory.domain.attendence.dto.AttendanceLogResponse;
import com.fintory.domain.attendence.dto.CheckInResponse;
import com.fintory.domain.attendence.model.AttendanceLog;
import com.fintory.domain.attendence.service.AttendanceService;
import com.fintory.domain.child.model.Child;
import com.fintory.infra.domain.attendance.repository.AttendanceRepository;
import com.fintory.infra.domain.point.serviceimpl.PointServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final PointServiceImpl pointService;

    @Override
    @Transactional
    public CheckInResponse check(Child child) {

        LocalDate today = LocalDate.now();
        log.info(today.toString());

        int continuousDays = calculateContinuousDays(child, today);
        boolean isCheckedIn = false;
        // 출석 중복 여부
        boolean isAlreadyChecked = attendanceRepository.existsByChildAndAttendanceDate(child, today);
        if (isAlreadyChecked) {
            isCheckedIn = true;
            return CheckInResponse.from(isCheckedIn, continuousDays);
        }
        AttendanceLog attendanceLog = new AttendanceLog(child, today);
        attendanceRepository.save(attendanceLog);
        pointService.givePointsByContinuousDays(continuousDays, child);

        return CheckInResponse.from(isCheckedIn, continuousDays);
    }

    @Override
    public List<AttendanceLogResponse> getAttendanceLogs(Child child) {

        List<AttendanceLog> logs = attendanceRepository.findAllByChild(child);

        return logs.stream()
                .map(AttendanceLogResponse::from)
                .collect(Collectors.toList());
    }

    public int calculateContinuousDays(Child child, LocalDate today) {
        // 오늘 출석은 이미 처리됨
        int continuousDays = 0;
        LocalDate checkDate = today.minusDays(1); // 어제부터 시작

        // 어제가 이번 달이 아닐 경우(오늘이 1일일 경우) → 어차피 저번달은 DB에 없음
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
