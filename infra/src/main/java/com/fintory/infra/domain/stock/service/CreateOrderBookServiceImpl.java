//package com.fintory.infra.domain.stock.service;
//
//import com.fintory.common.exception.DomainErrorCode;
//import com.fintory.common.exception.DomainException;
//import com.fintory.domain.stock.dto.LiveStockPriceResponse;
//import com.fintory.domain.stock.dto.OrderBookResponse;
//import com.fintory.domain.stock.model.LiveStockPrice;
//import com.fintory.domain.stock.model.OrderBook;
//import com.fintory.domain.stock.model.Stock;
//import com.fintory.infra.domain.stock.repository.LiveStockPriceRepository;
//import com.fintory.infra.domain.stock.repository.OrderBookRepository;
//import com.fintory.infra.domain.stock.repository.StockRepository;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.atomic.AtomicLong;
//
//import static com.fintory.domain.stock.dto.OrderBookResponse.convertFromOrderBook;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class CreateOrderBookServiceImpl {
//
//    private final StockRepository stockRepository;
//    private final OrderBookRepository orderBookRepository;
//    private final LiveStockPriceRepository liveStockPriceRepository;
//
//    private final Random random = new Random();
//
//    private final long UPDATE_INTERVAL = 3600000; //REVIEW 1시간마다 DB에 저장
//    private final AtomicLong lastUpdatedTime = new AtomicLong(0);
//
//
//
//    public OrderBookResponse createOrderBook(String code){
//        long currentTime = System.currentTimeMillis();
//
//        Stock stock = stockRepository.findByCode(code).orElseThrow(()->new DomainException(DomainErrorCode.STOCK_NOT_FOUND));
//
//        OrderBook orderBook = orderBookRepository.findByStock(stock)
//                .orElseGet(()-> OrderBook.builder().stock(stock).build());
//
//        // 시세 데이터는 어플을 시작하면서 이미 DB에 저장되어 있는 구조
//        // currentPrice를 기반으로 호가 데이터를 만들기 때문에 위 코드와는 달리 DB에 값이 없으면 에러를 throw하는 방식
//        LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock).orElseThrow(()->DomainException(DomainErrorCode.LIVE_STOCK_PRICE_NOT_FOUND));
//
//        OrderBookResponse response = nextOrderBook(liveStockPrice.getCurrentPrice(),stock);
//
//        if(currentTime-lastUpdatedTime.get()>UPDATE_INTERVAL){
//            OrderBook.updateOrderBook(response);
//            orderBookRepository.save(orderBook);
//        }
//        return response;
//    }
//
//    //수량 분포 -> 최우선 호가에 몰려있고 갈수록 수량이 줄어듦 -> 지수 분포 + 가우시안 노이즈 추가
//
//    public OrderBookResponse nextOrderBook(BigDecimal currentPrice,Stock stock){
//
//        double basePrice= currentPrice.doubleValue();
//        double tickSize=10;
//        long levels = 10;
//
//        List<OrderBookResponse.PriceLevel> sellLevels = new ArrayList<>();
//        List<OrderBookResponse.PriceLevel> buyLevels = new ArrayList<>();
//
//        for(int i=1;i<=levels;i++){
//            double sellPrice= basePrice + i*tickSize; //현재가 보다 높은 가격
//            double buyPrice = basePrice - i*tickSize; //현재가 보다 낮은 가격
//
//
//            long sellQuantity = (long) Math.max(100, 1000 * Math.exp(-0.3*i) + random.nextGaussian()*50);
//            long buyQuantity = (long) Math.max(100, 1000 * Math.exp(-0.3*i) + random.nextGaussian()*50);
//
//            sellLevels.add(new OrderBookResponse.PriceLevel(BigDecimal.valueOf(sellPrice),sellQuantity));
//            buyLevels.add(new OrderBookResponse.PriceLevel(BigDecimal.valueOf(buyPrice),buyQuantity));
//        }
//
//        return new OrderBookResponse(sellLevels,buyLevels,stock.getCode(),stock.getName());
//    }
//}
