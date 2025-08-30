package com.employee.managment.demo;

import com.employee.managment.demo.service.CronService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CronScheduler {
    @Autowired
    CronService cronService;

    @Scheduled(cron = "0 * * * * *")
    public void executeCron() throws Exception{
        cronService.testScheduler();
    }
}
