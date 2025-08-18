package com.fintory.domain.financialword.dto;

import com.fintory.domain.financialword.model.FinancialWord;

public record WordResponse(
        String word,
        String definition,
        String moreInfo
) {

    public static WordResponse from(FinancialWord word) {
        return new WordResponse(
                word.getWord(),
                word.getDefinition(),
                word.getMoreInfo()
        );
    }

}
