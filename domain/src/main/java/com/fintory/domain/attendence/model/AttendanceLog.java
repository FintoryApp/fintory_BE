package com.fintory.domain.attendence.model;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "attendance_log", uniqueConstraints =
@UniqueConstraint(name = "uk_child_attendance_date", columnNames = {"child_id", "attendance_date"}))
public class AttendanceLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id", nullable = false)
    private Child child;

    @Column(name="attendance_date", nullable = false)
    private LocalDate attendanceDate;

    public AttendanceLog(Child child, LocalDate attendanceDate) {
        this.child = child;
        this.attendanceDate = attendanceDate;
    }
}
