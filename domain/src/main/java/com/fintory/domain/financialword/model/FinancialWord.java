package com.fintory.domain.financialword.model;

import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.quiz.model.Quiz;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="financial_word")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinancialWord extends BaseEntity {

    private String content;

    private String title;

    @OneToOne(mappedBy = "word")
    private Quiz quiz;
}
