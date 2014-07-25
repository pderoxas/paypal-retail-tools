package com.paypal.merchant.retail.tools.exception;

/**
 * Created by Paolo on 7/23/2014.
 */
public class ClientException extends Exception {
    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Exception e) {
        super(message, e);
    }
}
