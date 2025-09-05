package com.fintory.infra.domain.stock.service.common;


import com.fintory.domain.stock.dto.korean.response.StockSearchResponse;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.service.common.CommonStockService;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonStockServiceImpl implements CommonStockService {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    @Override
    public List<StockSearchResponse> searchStock(String keyword) {
        List<StockSearchResponse> results = new ArrayList<>();
        // 이름, 영어 이름, symbol(code) 검색 가능
        List<Stock> matches = stockRepository.findByNameContainingOrCodeContaining(keyword);

        for (Stock stock : matches) {
            StockSearchResponse searchStock = new StockSearchResponse(
                    stock.getCode(),
                    stock.getName());

            results.add(searchStock);
        }
        return results;
    }
}
