package com.fintory.domain.financialword.dto;

import com.fintory.domain.financialword.model.FinancialWord;

public record WordSummaryResponse(
        Long id,
        String word,
        String summary
) {
    public static WordSummaryResponse from(FinancialWord word) {
        return new WordSummaryResponse(
                word.getId(),
                word.getWord(),
                word.getDefinition()
        );
    }
}
