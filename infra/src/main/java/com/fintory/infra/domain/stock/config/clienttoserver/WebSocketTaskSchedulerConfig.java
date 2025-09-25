//package com.fintory.infra.domain.stock.config.clienttoserver;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.TaskScheduler;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
//
//@Configuration
//public class WebSocketTaskSchedulerConfig {
//
//    @Bean(name = "webSocketTaskScheduler")
//    public TaskScheduler webSocketTaskScheduler() {
//        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
//        scheduler.setPoolSize(1);
//        scheduler.setThreadNamePrefix("webSocket-heartbeat-thread-");
//        return scheduler;
//    }
//}
