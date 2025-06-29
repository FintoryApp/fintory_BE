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
@Table(name="visit_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VisitLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id", nullable = false)
    private Child child;

    @Column(name="visited_date", nullable = false)
    private LocalDate visitedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

}
