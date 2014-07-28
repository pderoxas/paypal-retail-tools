package com.paypal.merchant.retail.tools.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class RepeatingTaskSchedulerTest {
    private RepeatingTaskScheduler scheduler;
    private int counter = 0;

    @Before
    public void setUp() throws Exception {
        final Runnable runnable = () -> counter++;
        scheduler = new RepeatingTaskScheduler(runnable, 0, 1, 1);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testStart() throws Exception {
        scheduler.start();

        // Need to give it time to execute since it is on another thread
        Thread.sleep(1);
        assertTrue(counter <= 1);
    }

    @Test
    public void testStop() throws Exception {
        scheduler.start(TimeUnit.MILLISECONDS);
        scheduler.stop();

        // Sleep for 10 and ensure that counter was not incremented more than twice
        // We have to allow for some variance when dealing with milliseconds
        Thread.sleep(10);
        assertTrue(counter <= 1);
    }

    @Test
    public void testGetDelayTime() throws Exception {
        scheduler.start(TimeUnit.SECONDS);
        assertTrue(scheduler.getDelayTime() == 0 || scheduler.getDelayTime() == 1);
    }
}