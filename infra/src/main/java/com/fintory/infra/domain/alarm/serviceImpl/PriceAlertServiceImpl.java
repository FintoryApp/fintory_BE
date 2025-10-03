package com.fintory.infra.domain.alarm.serviceImpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.alarm.dto.PriceAlertRequest;
import com.fintory.domain.alarm.dto.PriceAlertResponse;
import com.fintory.domain.alarm.model.PriceAlert;
import com.fintory.domain.alarm.service.PriceAlertService;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.stock.model.Stock;
import com.fintory.infra.domain.alarm.repository.PriceAlertRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceAlertServiceImpl implements PriceAlertService {

    private final PriceAlertRepository priceAlertRepository;
    private final StockRepository stockRepository;

    @Transactional
    @Override
    public void createPriceAlert(Child child,PriceAlertRequest priceAlertRequest) {
        Stock stock = stockRepository.findByCode(priceAlertRequest.stockCode()).orElseThrow(()->new DomainException(DomainErrorCode.STOCK_NOT_FOUND));
        PriceAlert priceAlert = PriceAlert.builder()
                .targetPrice(priceAlertRequest.targetPrice())
                .child(child)
                .stock(stock)
                .build();

        priceAlertRepository.save(priceAlert);
    }

    @Override
    public List<PriceAlertResponse> getPriceAlerts(Child child,String stockCode) {
        List<PriceAlert> priceAlertList = priceAlertRepository.findByChildIdAndStockCode(child.getId(), stockCode);

        return priceAlertList.stream()
                .map(priceAlert ->new PriceAlertResponse(priceAlert.getId(),priceAlert.getTargetPrice()))
                .toList();
    }

    @Transactional
    @Override
    public void deletePriceAlert(Long priceAlertId,Child child) {
        PriceAlert priceAlert = priceAlertRepository.findById(priceAlertId).orElseThrow(()-> new DomainException(DomainErrorCode.PRICE_ALERT_NOT_FOUND));
        if(!priceAlert.getChild().equals(child)) {
            throw new DomainException(DomainErrorCode.PRICE_ALERT_FORBIDDEN);
        }
        priceAlertRepository.delete(priceAlert);
    }
}
