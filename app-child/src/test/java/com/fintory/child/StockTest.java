package com.fintory.child;

import com.fintory.child.stock.service.StockServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

@SpringBootTest
class StockTest {

    @Autowired
    private StockServiceImpl stockService;

    @Test
    void getKoreanStockMarketCapTop20() throws IOException {
        //Given

        //When
        stockService.getKoreanStockMarketCapTop20();

        //Then
    }
}
