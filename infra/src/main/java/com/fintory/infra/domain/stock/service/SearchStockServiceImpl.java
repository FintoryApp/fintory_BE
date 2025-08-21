package com.fintory.infra.domain.stock.service;

import com.fintory.domain.stock.dto.StockSearchRequest;
import com.fintory.domain.stock.dto.StockSearchResponse;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.service.SearchStockService;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchStockServiceImpl implements SearchStockService {
    private final StockRepository stockRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StockSearchResponse> searchStock(StockSearchRequest keyword) {
        List<StockSearchResponse> results = new ArrayList<>();

        // 이름, 영어 이름, symbol(code) 검색 가능
        List<Stock> matches = stockRepository.findByNameContainingOrCodeContaining(keyword.keyword());

        for (Stock stock : matches) {
                StockSearchResponse searchStock = new StockSearchResponse(
                        stock.getCode(),
                        stock.getName());

                results.add(searchStock);
        }
        return results;
    }
}
