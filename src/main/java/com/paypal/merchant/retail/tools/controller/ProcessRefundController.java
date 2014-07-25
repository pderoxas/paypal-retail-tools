package com.paypal.merchant.retail.tools.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Created by Paolo on 7/21/2014.
 */
public class ProcessRefundController implements Initializable, ManagedPane {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    PaneManager paneManager;

    private static SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @FXML
    private TextArea txt_log;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO
        logger.debug("initializing Process Refund Pane");
    }

    @Override
    public void setParent(PaneManager paneManager) {
        this.paneManager = paneManager;
    }



    private void appendToLog(String message) {
        txt_log.appendText(logDateFormat.format(new Date()) + " - " + message + "\n");
    }


}
