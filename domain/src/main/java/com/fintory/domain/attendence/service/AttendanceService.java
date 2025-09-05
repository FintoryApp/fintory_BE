package com.fintory.domain.attendence.service;

import com.fintory.domain.attendence.dto.AttendanceLogResponse;
import com.fintory.domain.child.model.Child;

import java.util.List;

public interface AttendanceService {


    int check(Child child);

    List<AttendanceLogResponse> getAttendanceLogs(Child child);
}
