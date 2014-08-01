package com.paypal.merchant.retail.tools.util;

import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Paolo
 * Created on 5/14/14 1:22 PM
 */
public class OneTimeTaskScheduler implements TaskScheduler {
    Logger logger = Logger.getLogger(this.getClass());

    private ScheduledExecutorService service;
    private ScheduledFuture<?> scheduledFuture;
    private Runnable runnable;
    private long initialWait;
    private TimeUnit timeUnit;

    public OneTimeTaskScheduler(final Runnable runnableTask, long initialWait, int threadPoolSize, TimeUnit timeUnit) {
        this.service = Executors.newScheduledThreadPool(threadPoolSize);
        this.runnable = runnableTask;
        this.initialWait = initialWait;
        this.timeUnit = timeUnit;
    }

    @Override
    public void start() {
        try{
            logger.debug("Setting the schedule of tasks.");
            scheduledFuture = service.schedule(this.runnable, initialWait, timeUnit);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void stop() {
        if(scheduledFuture != null) {
            logger.info("Stop the task scheduler. No more future tasks will be executed.");
            scheduledFuture.cancel(true);
        }
    }

    @Override
    public long getDelayTime() {
        return 0;
    }
}
