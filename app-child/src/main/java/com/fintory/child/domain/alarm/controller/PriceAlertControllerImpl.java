package com.fintory.child.domain.alarm.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.alarm.dto.PriceAlertRequest;
import com.fintory.domain.alarm.dto.PriceAlertResponse;
import com.fintory.domain.alarm.service.PriceAlertService;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.child.service.ChildService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/child/price-alerts")
@RequiredArgsConstructor
public class PriceAlertControllerImpl implements PriceAlertController{

    private final PriceAlertService priceAlertService;
    private final ChildService childService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPriceAlert(@AuthenticationPrincipal CustomUserDetails user, @RequestBody PriceAlertRequest request){
        Child child = childService.getChild(user.getUsername());
        priceAlertService.createPriceAlert(child,request);
        return ResponseEntity.ok(ApiResponse.ok(null));

    }

    @Override
    @GetMapping("/{stockCode}")
    public ResponseEntity<ApiResponse<List<PriceAlertResponse>>> getPriceAlerts(
            @PathVariable String stockCode,
            @AuthenticationPrincipal CustomUserDetails user){
        Child child = childService.getChild(user.getUsername());
        List<PriceAlertResponse> priceAlertResponses = priceAlertService.getPriceAlerts(child,stockCode);
        return ResponseEntity.ok(ApiResponse.ok(priceAlertResponses));
    }


    @Override
    @DeleteMapping("/{priceAlertId}")
    public ResponseEntity<ApiResponse<Void>> deletePriceAlert(@PathVariable Long priceAlertId, @AuthenticationPrincipal CustomUserDetails user){
        Child child = childService.getChild(user.getUsername());
        priceAlertService.deletePriceAlert(priceAlertId,child);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}
