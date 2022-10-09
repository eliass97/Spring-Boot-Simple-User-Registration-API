package com.example.login.scheduled;

import org.springframework.scheduling.quartz.QuartzJobBean;

public abstract class QuartzScheduledJob extends QuartzJobBean {

    public abstract String getJobName();

    public abstract String triggerCron();
}
