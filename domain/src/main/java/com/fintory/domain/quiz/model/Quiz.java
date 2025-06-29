package com.fintory.domain.quiz.model;

import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.financialword.model.FinancialWord;
import com.fintory.domain.mapping.MyQuiz;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Table(name="quiz")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends BaseEntity {

    private String question;

    private String answer;

    private String explanation;

    //현재는 term - quiz가 일대일 관계로 변경했으나, 수정 자유
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="financial_word_id")
    private FinancialWord word;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "quiz")
    private List<MyQuiz> myQuiz;
    //myquiz 없애고 아이가 자신의 퀴즈 기록은 못보고 오늘의 퀴즈 형식으로 하나씩 나오고 맞추면 즉시 포인트 지급, 못맞추면 정답 바로 공개 식으로 가도 되지않을까 라는 생각이..
}
