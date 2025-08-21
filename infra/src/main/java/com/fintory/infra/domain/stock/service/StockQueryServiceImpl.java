package com.fintory.infra.domain.stock.service;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.LiveStockPriceResponse;
import com.fintory.domain.stock.dto.OrderBookResponse;
import com.fintory.domain.stock.dto.StockPriceHistoryResponse;
import com.fintory.domain.stock.dto.StockPriceHistoryWrapper;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.OrderBook;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import com.fintory.domain.stock.service.StockQueryService;
import com.fintory.infra.domain.stock.repository.LiveStockPriceRepository;
import com.fintory.infra.domain.stock.repository.OrderBookRepository;
import com.fintory.infra.domain.stock.repository.StockPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockQueryServiceImpl implements StockQueryService {

    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final OrderBookRepository orderBookRepository;
    private final LiveStockPriceRepository liveStockPriceRepository;

    //DB에서 기간별 시세 데이터 조회
    @Override
    public StockPriceHistoryWrapper getStockPriceHistory(Stock stock){
        List<StockPriceHistory> stockPriceHistoryList = stockPriceHistoryRepository.findByStock(stock);

        StockPriceHistoryWrapper wrapper = new StockPriceHistoryWrapper(stock.getCode(),stock.getName());

        stockPriceHistoryList
                .forEach(history->{
                    StockPriceHistoryResponse response = new StockPriceHistoryResponse(
                            history.getOpenPrice(),history.getClosePrice(),
                            history.getHighPrice(),history.getLowPrice(),
                            history.getIntervalType(),history.getDate());

                    wrapper.charData().computeIfAbsent(history.getIntervalType(),k-> new ArrayList<>())
                            .add(response);
                });

        return wrapper;

    }


    /* 해당 메소드는 웹소켓을 통해 받은 데이터를 리액트에서 띄우지만 서버 재시작등의 이유로 상태값을 잃었을 떄를 대비한 메소드 */
    //DB에서 현재가 데이터 조회 -> 전체 stock 대상
    @Override
    public List<LiveStockPriceResponse> getEachLiveStockPrice(List<Stock> stockList){
        return stockList.stream()
                .map(stock->{
                    LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock).orElseThrow(()-> new DomainException(DomainErrorCode.LIVE_STOCK_PRICE_NOT_FOUND));

                    return new LiveStockPriceResponse(
                            liveStockPrice.getCurrentPrice(),
                            liveStockPrice.getPriceChange(),
                            liveStockPrice.getPriceChangeRate(),
                            liveStockPrice.getStock().getName()
                    );
                })
                .toList();

    }

    //DB에서 현재가 데이터 조회 -> 특정 stock 대상
    @Override
    public LiveStockPriceResponse getEachLiveStockPrice(Stock stock){
        LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock).orElseThrow(()-> new DomainException(DomainErrorCode.LIVE_STOCK_PRICE_NOT_FOUND));

        return new LiveStockPriceResponse(
                liveStockPrice.getCurrentPrice(),
                liveStockPrice.getPriceChange(),
                liveStockPrice.getPriceChangeRate(),
                liveStockPrice.getStock().getName()
        );
    }


    /*
    //DB에서 호가 데이터 조회
    @Override
    public OrderBookResponse getOrderBook(Stock stock){
        OrderBook orderBook = orderBookRepository.findByStock(stock).orElseThrow(()->new DomainException(DomainErrorCode.ORDER_BOOK_NOT_FOUND));

        return OrderBookResponse.convertFromOrderBook(orderBook);
    }
*/

}
