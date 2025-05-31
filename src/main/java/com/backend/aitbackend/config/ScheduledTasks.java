package com.backend.aitbackend.config;

import com.backend.aitbackend.service.UnoGameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ScheduledTasks {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    
    @Autowired
    private UnoGameService unoGameService;

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupInactiveRooms() {
        logger.debug("Running cleanup task for inactive rooms");
        unoGameService.cleanupInactiveRooms();
    }
}
