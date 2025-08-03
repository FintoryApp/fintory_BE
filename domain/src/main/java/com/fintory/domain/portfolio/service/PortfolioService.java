package com.fintory.domain.portfolio.service;

import com.fintory.domain.portfolio.dto.OwnedStockList;
import com.fintory.domain.portfolio.dto.PortfolioSummary;

import java.util.List;

public interface PortfolioService {
    public List<OwnedStockList> getOwnedStockList();
    public PortfolioSummary getPortfolioSummary();
}
