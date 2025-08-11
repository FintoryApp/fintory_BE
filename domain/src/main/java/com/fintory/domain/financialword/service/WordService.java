package com.fintory.domain.financialword.service;

import com.fintory.domain.financialword.dto.WordResponse;
import com.fintory.domain.financialword.dto.WordSummaryResponse;
import com.fintory.domain.financialword.dto.WordTitleResponse;

import java.util.List;

public interface WordService {

    WordResponse getWordById(Long id);

    List<WordTitleResponse> getWordList();

    WordSummaryResponse getRandomWordSummary();

    List<WordTitleResponse> getSearchWordList(String keyword);
}
