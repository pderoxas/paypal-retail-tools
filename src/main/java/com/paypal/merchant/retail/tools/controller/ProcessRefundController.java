package com.paypal.merchant.retail.tools.controller;

import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Paolo on 7/21/2014.
 */
public class ProcessRefundController implements Initializable, ManagedPane {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    PaneManager paneManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO
        logger.debug("initializing Process Refund Pane");
    }

    @Override
    public void setParent(PaneManager paneManager) {
        this.paneManager = paneManager;
    }
}
