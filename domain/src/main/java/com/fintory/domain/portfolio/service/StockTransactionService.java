package com.fintory.domain.portfolio.service;


import com.fintory.domain.portfolio.model.StockTransaction;

import java.util.List;

public interface StockTransactionService {
    public List<StockTransaction> getStockTransactions();
}