package com.fintory.websocket.monitoring.listener;

import com.fintory.websocket.monitoring.config.WebSocketMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


//REVIEW 서비스 코드는 아니고, 리스너는 인터페이스가 필요없어서 따로 패키지를 만들었는데, 논리상 파일 위치에 문제 있을 시 리뷰 주시면 반영하겠습니다!
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final WebSocketMetrics webSocketMetrics;

    // 클라이언트 stomp 연결(STMOP CONNECTED로 응답 완료 후 ) -> SessionConntecdEvent 발행
    @EventListener
    public void handleSessionConnect(SessionConnectedEvent event) {
        webSocketMetrics.incrementConnection();
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        webSocketMetrics.decrementConnection();
    }
}
