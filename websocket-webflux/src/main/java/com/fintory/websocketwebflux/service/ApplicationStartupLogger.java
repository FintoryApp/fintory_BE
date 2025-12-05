package com.fintory.websocketwebflux.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationStartupLogger {

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("------------------------------------------------------------------");
        log.info("Application is UP and READY to serve requests!");
        log.info("------------------------------------------------------------------");
    }
}
