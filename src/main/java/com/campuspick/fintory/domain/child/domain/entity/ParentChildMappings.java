package com.campuspick.fintory.domain.child.domain.entity;


import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.domain.parent.domain.entity.Parents;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name="parent_child_mappings")
public class ParentChildMappings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private MappingStatus mappingStatus;

    @Column(name="invited_at")
    private LocalDateTime invitedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private Parents parent;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Childs child;
}
