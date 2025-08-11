package com.fintory.infra.domain.word.serviceimpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.financialword.dto.WordResponse;
import com.fintory.domain.financialword.dto.WordSummaryResponse;
import com.fintory.domain.financialword.dto.WordTitleResponse;
import com.fintory.domain.financialword.service.WordService;
import com.fintory.infra.domain.word.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WordServiceImpl implements WordService {

    private final WordRepository wordRepository;

    @Override
    public WordResponse getWordById(Long id) {
        return wordRepository.findById(id)
                .map(WordResponse::from)
                .orElseThrow(() -> new DomainException(DomainErrorCode.WORD_NOT_FOUND));
    }

    @Override
    public List<WordTitleResponse> getWordList() {
        return wordRepository.findAll()
                .stream()
                .map(WordTitleResponse::from)
                .toList();
    }

    @Override
    public WordSummaryResponse getRandomWordSummary() {

        return wordRepository.wordRepositoryRandomWord()
                .map(WordSummaryResponse::from)
                .orElseThrow(() -> new DomainException(DomainErrorCode.WORD_NOT_FOUND));
    }

    @Override
    public List<WordTitleResponse> getSearchWordList(String keyword) {

        return wordRepository.searchByKeyword(keyword)
                .stream()
                .map(WordTitleResponse::from)
                .toList();
    }

}
