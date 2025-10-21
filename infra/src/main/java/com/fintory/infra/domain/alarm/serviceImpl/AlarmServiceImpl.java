package com.fintory.infra.domain.alarm.serviceImpl;


import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.alarm.model.FcmToken;
import com.fintory.domain.alarm.model.NotificationType;
import com.fintory.domain.alarm.service.AlarmService;
import com.fintory.domain.child.model.Child;
import com.fintory.infra.domain.alarm.repository.FcmTokenRepository;
import com.fintory.infra.domain.child.repository.ChildRepository;
import com.fintory.infra.util.SecurityUtil;
import com.google.cloud.storage.Acl;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmServiceImpl implements AlarmService {

    private final FcmTokenRepository fcmTokenRepository;
    private final SecurityUtil securityUtil;
    private final ChildRepository childRepository;

    //프론트에서 받은 토큰을 DB에 저장하는 메소드
    @Override
    @Transactional
    public void saveToken(Child child, String token){

        //토큰 중복 저장 방지
        if(fcmTokenRepository.existsByToken(token)){
            log.info("이미 존재하는 FCM 토큰:{}", token);
            return;
        }
        FcmToken fcmToken = FcmToken.builder()
                .token(token).child(child)
                .build();

        fcmTokenRepository.save(fcmToken);
    }

    //fcm에게 푸시 알림 전송
    @Override
    @Transactional(readOnly = true)
    public void pushMessage(Long childId,
                            NotificationType notificationType,
                            String title,
                            String body){

        Child child = childRepository.findById(childId).orElseThrow(()-> new DomainException(DomainErrorCode.USER_NOT_FOUND));
        List<FcmToken> fcmTokens = getToken(child);

        if(fcmTokens.isEmpty()){
            throw new DomainException(DomainErrorCode.FCMTOKEN_EMPTY);
        }

        // 모든 기기에 알림 전송
        for(FcmToken fcmToken : fcmTokens) {
            sendToDevice(fcmToken.getToken(),notificationType, title, body);
        }
    }

    @Override
    @Transactional
    public void deleteToken( String token){
        fcmTokenRepository.deleteByToken(token);
        log.info("FCM 토큰 삭제:{}", token);
    }

    private void sendToDevice(String fcmToken, NotificationType notificationType, String title, String body){

        //fcm 메시지 생성
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("type",notificationType.name())
                .build();


        //fcm 서버로 전송
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("fcm 요청 후 받은 응답:{} ",response); //테스트 로그 -> 이후 삭제
        }catch (FirebaseMessagingException e){
            // 예외를 던지면 다른 디바이스에도 영향을 줌
            log.info("fcm 전송 실패(토큰 만료) "+e.getMessage());
            deleteToken(fcmToken);
        }
    }

    //토큰 가져오기
    private List<FcmToken> getToken(Child child){
        List<FcmToken> tokens = fcmTokenRepository.findByChild(child);
        return tokens;
    }



}
