package com.fintory.domain.challenge.model;

import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="challenge_categories")
public class ChallengeCategory extends BaseEntity {

    private String name;
}
