package com.campuspick.fintory.modules.child.domain.entity;


import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.modules.parent.domain.entity.Parents;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Parent;

@Entity
@NoArgsConstructor
@Getter
@Table(name="parent_child_mappings")
public class ParentChildMappings extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name="parent_id")
    private Parents parent;

    @OneToOne
    @JoinColumn(name="child_id")
    private Childs child;

    private MappingStatus mappingStatus;
}
