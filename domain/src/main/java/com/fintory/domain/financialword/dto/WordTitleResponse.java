package com.fintory.domain.financialword.dto;

import com.fintory.domain.financialword.model.FinancialWord;

public record WordTitleResponse(

        Long id,
        String word
) {

    public static WordTitleResponse from(FinancialWord word) {
        return new WordTitleResponse(
                word.getId(),
                word.getWord()
        );
    }
}
