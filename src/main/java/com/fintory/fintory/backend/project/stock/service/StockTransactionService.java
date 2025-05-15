package com.fintory.fintory.backend.project.stock.service;

import com.fintory.fintory.backend.project.stock.entity.StockTransaction;
import org.springframework.stereotype.Service;

import java.util.List;


public interface StockTransactionService {

    //모든 거래 내역을 가져오는 비즈니스 로직 -> 실제에서는 사용자의 id를 이용해서 조회해야 함
    public List<StockTransaction> getStockTransactions();
}
