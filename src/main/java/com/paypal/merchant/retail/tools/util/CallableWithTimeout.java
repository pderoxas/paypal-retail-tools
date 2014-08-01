package com.paypal.merchant.retail.tools.util;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.paypal.merchant.retail.tools.exception.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by Paolo on 7/30/2014.
 */
public class CallableWithTimeout<T> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Callable<T> callable;
    private long timeOut;
    private TimeUnit timeUnit;
    private static TimeLimiter timeLimiter = new SimpleTimeLimiter();

    public CallableWithTimeout(Callable<T> callable, long timeOut, TimeUnit timeUnit) {
        this.callable = callable;
        this.timeOut = timeOut;
        this.timeUnit = timeUnit;
    }

    public T call() throws ClientException {
        try {
            return timeLimiter.callWithTimeout(callable, timeOut, timeUnit, false);
        } catch (ClientException e) {
            throw e;
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted while executing callable", e);
            throw new ClientException(e.getMessage());
        } catch (UncheckedTimeoutException e) {
            logger.error("Timed Out while executing callable", e);
            throw new ClientException(e.getMessage());
        } catch (Exception e) {
            logger.error("Exception while executing callable", e);
            throw new ClientException(e.getMessage());
        }
    }
}
