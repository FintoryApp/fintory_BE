package com.fintory.domain.mapping;


import com.fintory.domain.child.model.Child;
import com.fintory.domain.parent.model.Parent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name="parent_child_mappings")
public class ParentChildMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private MappingStatus mappingStatus;

    @Column(name="invited_at")
    private LocalDateTime invitedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private Parent parent;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Child child;
}
