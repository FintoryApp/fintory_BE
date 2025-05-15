package com.fintory.fintory.backend.project.stock.service;

import com.fintory.fintory.backend.project.stock.entity.StockTransaction;
import com.fintory.fintory.backend.project.stock.repository.StockTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockTransactionServiceImpl implements StockTransactionService {

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Override
    public List<StockTransaction> getStockTransactions() {
        return stockTransactionRepository.findAll();
    }
}
