package com.fintory.infra.domain.portfolio.serviceImpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.portfolio.service.ExchangeRateService;
import com.fintory.infra.domain.portfolio.properties.EosProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final EosProperties eosProperties;

    /* 앱 시작 시 환율 초기화 */
    @PostConstruct
    public void initExchangeRate() {
        log.info("앱 시작 - 환율 정보 갱신");
        fetchExchangeRate();
    }

    /* 매일 오전 9시에 환율 갱신 */
    @Scheduled(cron = "0 0 9 * * * ") //매일 오전 9시에 시행
    public void refreshExchangeRate(){
        log.info("스케쥴러 실행 - 환율 정보 갱신");
        fetchExchangeRate();
    }

    /* Redis 조회 */
    @Override
    public BigDecimal getExchangeRate(){
        String cached = (String) redisTemplate.opsForValue().get("exchangeRate");
        if(cached == null){
            return fetchExchangeRate();
        }
        return new BigDecimal(cached);
    }


    /* API 호출 및 Redis 저장 */
    private BigDecimal fetchExchangeRate(){
        try{
            String stringUrl = "https://ecos.bok.or.kr/api/KeyStatisticList/"+eosProperties.getApiKey()+"/xml/kr/1/10";

            URL url = new URL(stringUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoOutput(true);

            InputStream is = urlConnection.getInputStream();

            byte[] buffer = new byte[2048];
            int len =-1;
            StringBuffer sb = new StringBuffer();

            while((len=is.read(buffer,0,buffer.length))!=-1){
                sb.append(new String(buffer,0,len));
            }

            String data = sb.toString();

            BigDecimal exchangeRate = parseExchangeRate(data);

            redisTemplate.opsForValue().set("exchangeRate",exchangeRate.toString(),26, TimeUnit.HOURS); //여유 있게 26시간 설정

            return exchangeRate;
        }catch (Exception e){

            String cached = (String) redisTemplate.opsForValue().get("exchangeRate");
            if (cached != null) {
                log.warn("API 호출 실패 - 기존 캐시 사용");
                return new BigDecimal(cached);
            }
            log.error("환율 정보 조회 시 에러 발생");
            throw new DomainException(DomainErrorCode.EXCHANGE_RATE_ERROR);
        }
    }

    //가져온 데이터 파싱
    private BigDecimal parseExchangeRate(String data){
        try {
            // xml 파싱 빌드업
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            //xml 파일을 document로 파싱
            Document document = builder.parse(new ByteArrayInputStream(data.getBytes()));

            NodeList nodeList = document.getElementsByTagName("row");
            Node node = nodeList.item(2);


            Element element = (Element) node;
            String dataValue = element.getElementsByTagName("DATA_VALUE").item(0).getTextContent();
            return BigDecimal.valueOf(Double.parseDouble(dataValue));

        }catch (Exception e){
            log.error("환율 정보 파싱 중 에러 발생"+e.getMessage());
            throw new DomainException(DomainErrorCode.PARSING_ERROR);
        }
    }
}
