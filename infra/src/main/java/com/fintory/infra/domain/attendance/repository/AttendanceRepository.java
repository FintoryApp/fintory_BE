package com.fintory.infra.domain.attendance.repository;

import com.fintory.domain.attendence.model.AttendanceLog;
import com.fintory.domain.child.model.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceLog, Long> {


    boolean existsByChildAndAttendanceDate(Child child, LocalDate today);
}
