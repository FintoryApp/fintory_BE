package com.fintory.websocket.publisher.service;

import com.fintory.domain.stock.dto.websocket.MarketStatusResponse;
import com.fintory.websocket.publisher.state.StockDataHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class MarketTimeService {

    private final StockDataHolder stockDataHolder;

    public boolean isKoreanMarketOpen() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        boolean weekday = now.getDayOfWeek() != DayOfWeek.SATURDAY && now.getDayOfWeek() != DayOfWeek.SUNDAY;
        return  weekday
                && !now.toLocalTime().isBefore(LocalTime.of(9, 0))
                &&  now.toLocalTime().isBefore(LocalTime.of(15, 30));
    }

    public boolean isOverseasMarketOpen() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
        boolean weekday = now.getDayOfWeek() != DayOfWeek.SATURDAY && now.getDayOfWeek() != DayOfWeek.SUNDAY;
        return weekday
                && !now.toLocalTime().isBefore(LocalTime.of(9, 0))
                &&  now.toLocalTime().isBefore(LocalTime.of(16, 0));
    }


    public MarketStatusResponse getMarketStatus() {
        // 국내 장 시간이면 "korean"
        if (stockDataHolder.getIsKoreanConnected().get() && isKoreanMarketOpen()) {
            return new MarketStatusResponse("korean");
        }

        // 해외 장 시간이면 "overseas"
        if (stockDataHolder.getIsOverseasConnected().get() && isOverseasMarketOpen()) {
            return new MarketStatusResponse("overseas");
        }

        // 둘 다 아니면 "no"
        return new MarketStatusResponse("no");
    }

}
