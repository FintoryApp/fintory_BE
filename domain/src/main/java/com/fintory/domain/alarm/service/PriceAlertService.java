package com.fintory.domain.alarm.service;

import com.fintory.domain.alarm.dto.PriceAlertRequest;
import com.fintory.domain.alarm.dto.PriceAlertResponse;
import com.fintory.domain.child.model.Child;

import java.util.List;

public interface PriceAlertService {

    void createPriceAlert(Child child,PriceAlertRequest priceAlertRequest);

    List<PriceAlertResponse> getPriceAlerts(Child child,String stockCode);

    void deletePriceAlert(Long priceAlertId, Child child);


}
