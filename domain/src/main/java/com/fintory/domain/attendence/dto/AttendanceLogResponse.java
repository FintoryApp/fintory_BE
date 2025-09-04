package com.fintory.domain.attendence.dto;

import com.fintory.domain.attendence.model.AttendanceLog;

import java.time.LocalDate;

public record AttendanceLogResponse(
        LocalDate attendanceLog
) {
    public static AttendanceLogResponse from(AttendanceLog log) {
        return new AttendanceLogResponse(
                log.getAttendanceDate()
        );
    }
}
