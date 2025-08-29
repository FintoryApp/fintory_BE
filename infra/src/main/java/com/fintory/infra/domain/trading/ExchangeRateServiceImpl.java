package com.fintory.infra.domain.trading;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    @Value("${eos.api-key}")
    private String apiKey;

    //환율 가져오기
    //REVIEW 실시간 환율은 제휴 회사 OR 유료라서 -> REST API로 하루에 한번씩 가져오려고 하는데 어떤지 리뷰
    @Override
    public BigDecimal getExchangeRate(){
        try{
            String stringUrl = "https://ecos.bok.or.kr/api/KeyStatisticList/"+apiKey+"/xml/kr/1/10";

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

            log.info(data);
            return parseExchangeRate(data);
        }catch (Exception e){
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
