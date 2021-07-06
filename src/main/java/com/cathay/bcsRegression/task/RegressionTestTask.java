package com.cathay.bcsRegression.task;

import com.cathay.bcsRegression.service.RegressionTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author 0100065352
 */
@Component
@Slf4j
public class RegressionTestTask {

    @Autowired
    private RegressionTestService regressionTestService;

    @Scheduled(cron = "*/5 * * * * ?")
    public void testSchedule(){
        regressionTestService.doTest();
    }
}
