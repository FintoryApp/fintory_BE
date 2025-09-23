package com.fintory.infra.domain.stock.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
public class WebSocketEventListener implements ApplicationListener<SessionDisconnectEvent> {

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        log.info("🔌 STOMP 세션 연결이 종료되었습니다. session id: {}, 상태: {}", event.getSessionId(), event.getCloseStatus());
    }
}
