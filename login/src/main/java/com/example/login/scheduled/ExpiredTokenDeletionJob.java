package com.example.login.scheduled;

import java.util.List;
import java.util.stream.Collectors;

import com.example.login.model.persistance.RegistrationToken;
import com.example.login.service.RegistrationTokenService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class ExpiredTokenDeletionJob extends QuartzScheduledJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpiredTokenDeletionJob.class);

    @Value("${jobs.expired-token-deletion.name}")
    private String jobName;

    @Value("${jobs.expired-token-deletion.cron-expression}")
    private String cronExpression;

    private final RegistrationTokenService registrationTokenService;

    public ExpiredTokenDeletionJob(RegistrationTokenService registrationTokenService) {
        this.registrationTokenService = registrationTokenService;
    }

    @Override
    public void executeInternal(JobExecutionContext jec) {
        List<Long> expiredTokenIds = registrationTokenService.getExpiredRegistrationTokens().stream()
                .map(RegistrationToken::getId)
                .collect(Collectors.toList());
        LOGGER.info("Expired tokens to be deleted: {}", expiredTokenIds);
        expiredTokenIds.forEach(registrationTokenService::deleteById);
    }

    public String getJobName() {
        return jobName;
    }

    public String triggerCron() {
        return cronExpression;
    }
}
