package com.campuspick.fintory.domain.alarms.domain.entity;


import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.domain.child.domain.entity.Child;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="alarms")
public class Alarm extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String content;

    @Column(name="is_read")
    private boolean isRead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Child child;
}
