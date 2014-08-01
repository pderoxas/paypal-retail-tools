package com.paypal.merchant.retail.tools.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class RepeatingTaskSchedulerTest {
    private TaskScheduler scheduler;
    private int counter = 0;

    @Before
    public void setUp() throws Exception {
        counter = 0;
        final Runnable runnable = () -> counter++;
        scheduler = new RepeatingTaskScheduler(runnable, 0, 1, 1, TimeUnit.MILLISECONDS);
    }

    @After
    public void tearDown() throws Exception {
        scheduler.stop();
    }

    @Test
    public void testStart() throws Exception {
        scheduler.start();

        Thread.sleep(10);
        int value1 = counter;
        System.out.println("testStart value1: " + value1);

        Thread.sleep(10);
        int value2 = counter;
        System.out.println("testStart value2: " + value2);

        Thread.sleep(10);
        int value3 = counter;
        System.out.println("testStart value3: " + value3);

        assertTrue(value1 < value2);
        assertTrue(value2 < value3);
    }

    @Test
    public void testStop() throws Exception {
        scheduler.start();
        scheduler.stop();
        int value = counter;

        // Sleep for 10 and ensure that counter was not incremented
        Thread.sleep(10);
        System.out.println("testStart value: " + value);
        System.out.println("testStart counter: " + counter);
        assertTrue(value == counter);
    }

    @Test
    public void testGetDelayTime() throws Exception {
        scheduler.start();
        System.out.println("testGetDelayTime counter: " + counter);
        assertTrue(scheduler.getDelayTime() <= 1);
    }
}