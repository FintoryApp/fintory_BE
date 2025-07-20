package com.fintory.child.stock.service;

import com.fintory.child.stock.dto.KoreanSearchStock;
import com.fintory.common.exception.BaseException;
import com.fintory.common.exception.ErrorCode;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.repository.StockRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonStockServiceImpl {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public List<KoreanSearchStock> searchStock(String keyword) {
        List<KoreanSearchStock> results = new ArrayList<>();
        Set<String> addedCodes = new HashSet<>();

        List<Stock> nameMatches = stockRepository.findByNameContaining(keyword);
        List<Stock> codeMatches = stockRepository.findByCodeContaining(keyword);


        for(Stock stock : nameMatches) {
            if(!addedCodes.contains(stock.getCode())) {

                KoreanSearchStock searchStock = KoreanSearchStock.builder()
                        .stockCode(stock.getCode())
                        .stockName(stock.getName())
                        .build();

                results.add(searchStock);
                addedCodes.add(stock.getCode());
            }

        }

        for(Stock stock : codeMatches) {
            if(!addedCodes.contains(stock.getCode())) {
                KoreanSearchStock searchStock = KoreanSearchStock.builder()
                        .stockCode(stock.getCode())
                        .stockName(stock.getName())
                        .build();
                results.add(searchStock);
                addedCodes.add(stock.getCode());
            }
        }

        return results;
    }
}
