package com.paypal.merchant.retail.tools.util;

/**
 * Created by Paolo on 7/28/2014.
 */
public interface TaskScheduler {
    public void start();
    public void stop();
    public long getDelayTime();
}
