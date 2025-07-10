package com.fintory.child.portfolio.service;

import com.fintory.domain.portfolio.model.StockTransaction;
import com.fintory.domain.portfolio.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTransactionServiceImpl implements StockTransactionService {

    private final StockTransactionRepository stockTransactionRepository;


    @Override
    public List<StockTransaction> getStockTransactions() {
        return stockTransactionRepository.findAll();
    }
}

