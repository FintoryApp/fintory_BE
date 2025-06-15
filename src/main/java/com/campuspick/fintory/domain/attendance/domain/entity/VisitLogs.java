package com.campuspick.fintory.domain.attendance.domain.entity;

import com.campuspick.fintory.domain.child.domain.entity.Childs;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name="visit_logs")
public class VisitLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="visited_at")
    private LocalDateTime visitedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Childs child;
}
