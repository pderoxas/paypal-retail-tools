package com.paypal.merchant.retail.tools.util;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class OneTimeTaskSchedulerTest extends TestCase {
    private TaskScheduler scheduler;
    private int counter = 0;

    @Before
    public void setUp() throws Exception {
        counter = 0;
        final Runnable runnable = () -> counter++;
        scheduler = new OneTimeTaskScheduler(runnable, 0, 1, TimeUnit.MILLISECONDS);
    }

    @After
    public void tearDown() throws Exception {
        scheduler.stop();
    }

    @Test
    public void testStart() throws Exception {
        scheduler.start();

        // Need to give it time to execute since it is on another thread
        Thread.sleep(1);
        System.out.println("testStart counter: " + counter);
        assertTrue(counter > 0);
    }

    @Test
    public void testStop() throws Exception {
        scheduler.start();
        scheduler.stop();

        // Sleep for 10 and ensure that counter is still 1
        Thread.sleep(10);
        System.out.println("testStop counter: " + counter);
        assertTrue(counter <= 1);
    }

    @Test
    public void testGetDelayTime() throws Exception {
        scheduler.start();
        assertTrue(scheduler.getDelayTime() == 0);
    }
}