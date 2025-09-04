package com.fintory.infra.domain.attendance.serviceImpl;

import com.fintory.domain.child.model.Child;
import com.fintory.infra.domain.attendance.repository.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // 모키토 객체 활성화
class AttendanceServiceImplTest {

    @InjectMocks // 테스트 대상 실제 클래스
    private AttendanceServiceImpl attendanceService;

    @Mock // 가짜 db 객체
    private AttendanceRepository attendanceRepository;

    // 테스트용 더미 유저
    private Child child;

    @BeforeEach
    void setUp() {
        child = mock(Child.class);
    }

    @Test
    void 연속_출석_2일이면_3리턴() {
        // given
        LocalDate today = LocalDate.of(2025, 9, 6);

        when(attendanceRepository.existsByChildAndAttendanceDate(child, today.minusDays(1))).thenReturn(true);  // 9/5
        when(attendanceRepository.existsByChildAndAttendanceDate(child, today.minusDays(2))).thenReturn(true);  // 9/4
        when(attendanceRepository.existsByChildAndAttendanceDate(child, today.minusDays(3))).thenReturn(false);  // 9/3
        // when
        int result = attendanceService.calculateContinuousDays(child, today);

        // then
        assertEquals(3, result); // 3일 + 오늘
    }

    @Test
    void 어제가_지난달이면_1리턴() {
        // given
        LocalDate today = LocalDate.of(2025, 9, 1); // 어제 = 8/31 (지난달)

        // when
        int result = attendanceService.calculateContinuousDays(child, today);

        // then
        assertEquals(1, result);
    }

    @Test
    void 어제_출석없으면_1리턴() {
        // given
        LocalDate today = LocalDate.of(2025, 9, 3);

        when(attendanceRepository.existsByChildAndAttendanceDate(child, today.minusDays(1))).thenReturn(false);  // 9/2

        // when
        int result = attendanceService.calculateContinuousDays(child, today);

        // then
        assertEquals(1, result); // 오늘만 출석
    }

    @Test
    void 이틀만_연속출석하면_3리턴() {
        // given
        LocalDate today = LocalDate.of(2025, 9, 3);

        when(attendanceRepository.existsByChildAndAttendanceDate(child, today.minusDays(1))).thenReturn(true);  // 9/2
        when(attendanceRepository.existsByChildAndAttendanceDate(child, today.minusDays(2))).thenReturn(true);  // 9/1

        // when
        int result = attendanceService.calculateContinuousDays(child, today);

        // then
        assertEquals(3, result);
    }
}