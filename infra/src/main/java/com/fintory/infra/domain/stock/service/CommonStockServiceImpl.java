package com.fintory.infra.domain.stock.service;


import com.fintory.domain.stock.dto.korean.response.StockSearchResponse;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.service.CommonStockService;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonStockServiceImpl implements CommonStockService {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    @Override
    public List<StockSearchResponse> searchStock(String keyword) {
        List<StockSearchResponse> results = new ArrayList<>();
        Set<String> addedCodes = new HashSet<>();

        List<Stock> nameMatches = stockRepository.findByNameContaining(keyword);
        List<Stock> codeMatches = stockRepository.findByCodeContaining(keyword);


        for(Stock stock : nameMatches) {
            if(!addedCodes.contains(stock.getCode())) {

                StockSearchResponse searchStock = new StockSearchResponse(
                         stock.getCode(),
                         stock.getName());


                results.add(searchStock);
                addedCodes.add(stock.getCode());
            }

        }

        for(Stock stock : codeMatches) {
            if(!addedCodes.contains(stock.getCode())) {
                StockSearchResponse searchStock = new StockSearchResponse(
                        stock.getCode(),
                        stock.getName());

                results.add(searchStock);
                addedCodes.add(stock.getCode());
            }
        }

        return results;
    }
}
