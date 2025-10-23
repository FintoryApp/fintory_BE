package com.fintory.infra.domain.alarm.config;


import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/* firebase 서비스를 이용하기 위한 연결 설정 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config}")
    private String firebaseConfig;

    @PostConstruct
    public void init(){
        try{
            // firebase 설정 파일 로드
            InputStream serviceAccount = new ByteArrayInputStream(firebaseConfig.getBytes());

            // firebase 인증 정보 설정
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // firebase 앱 초기화
            if(FirebaseApp.getApps().isEmpty()){
                FirebaseApp.initializeApp(options);
            }
        }catch(Exception e){
            log.error("firebase 초기화 실패",e);
            throw new DomainException(DomainErrorCode.FIREBASE_ERROR);
        }
    }
}
