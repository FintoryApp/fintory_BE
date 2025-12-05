package com.fintory.infra.domain.alarm.config;

import com.fintory.infra.domain.alarm.serviceImpl.PriceAlertEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Spring이 Redis Pub/Sub을 자동으로 구독하도록 설정 => 누가 어떤 채널을 듣고 어떤 메소드로 처리하는지 정의
 */
@Configuration
@RequiredArgsConstructor
public class RedisSubscribeConfig {

    private static final String PRICE_ALERT_CHANNEL = "price:alert:channel";

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer( //Redis로부터 메시지를 받아서 지정된 리스너에게 전달하는 도우미 백그라운드 쓰레드 풀
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new ChannelTopic(PRICE_ALERT_CHANNEL)); //Redis 메시지가 들어올 때 자동으로 PriceAlertEventListener 호출
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(PriceAlertEventListener subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }
}