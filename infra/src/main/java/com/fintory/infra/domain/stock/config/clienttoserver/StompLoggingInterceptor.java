package com.fintory.infra.domain.stock.config.clienttoserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StompLoggingInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() != null) {
            log.info("📢 [STOMP IN] Command: {}, SessionId: {}, Headers: {}",
                    accessor.getCommand(),
                    accessor.getSessionId(),
                    accessor.toNativeHeaderMap());

            if (accessor.getCommand().getMessageType().equals(org.springframework.messaging.simp.SimpMessageType.MESSAGE)) {
                log.info("   Destination: {}, Payload: {}",
                        accessor.getDestination(),
                        new String((byte[]) message.getPayload()));
            }
        }

        return message;
    }
}
