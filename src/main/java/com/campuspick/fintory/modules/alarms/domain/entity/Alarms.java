package com.campuspick.fintory.modules.alarms.domain.entity;


import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.modules.child.domain.entity.Childs;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="alarms")
public class Alarms extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Childs child;

    private String type;

    private String content;

    @Column(name="is_read")
    private boolean isRead;
}
