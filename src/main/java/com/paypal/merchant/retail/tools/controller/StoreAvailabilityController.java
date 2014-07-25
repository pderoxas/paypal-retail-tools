package com.paypal.merchant.retail.tools.controller;

import com.paypal.merchant.retail.tools.Main;
import com.paypal.merchant.retail.tools.client.SdkClient;
import com.paypal.merchant.retail.tools.exception.ClientException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
public class StoreAvailabilityController implements Initializable, ManagedPane {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    PaneManager paneManager;

    private static SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @FXML
    private Label lbl_storeStatus;

    @FXML
    private Button btn_changeStatus;

    @FXML
    private TextArea txt_log;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO
        logger.debug("initializing storeAvailabilityPane...");


        try {
            updatePane();

            appendToLog("Store Location availability: " + (Main.sdkLocation.isOpen() ? "OPEN" : "CLOSED"));
        } catch (Exception e) {
            logger.error("Failed to initialize SDK Tool! ", e);
        }
    }

    @Override
    public void setParent(PaneManager paneManager) {
        this.paneManager = paneManager;
    }

    /**
     * Handles the change location status event
     *
     * @param event ActionEvent
     */
    @FXML
    protected void handleChangeLocationStatus(ActionEvent event) {
        logger.debug("Handling Change Location Status button");

        try {
            // Set the availability to the opposite of what it is currently
            appendToLog("Setting Store Location availability to: " + (Main.sdkLocation.isOpen() ? "CLOSED" : "OPEN"));

            // show glass pane
            Main.controller.showProcessing();

            final Task setLocationAvailabilityTask = new Task() {
                @Override
                protected Void call() throws InterruptedException {
                    updateMessage("Setting Store Location Availability . . .");
                    try {
                        Main.sdkLocation = SdkClient.INSTANCE.setLocationAvailability(Main.sdkLocation, !Main.sdkLocation.isOpen());
                    } catch (ClientException e) {
                        logger.error("Failed to set Store Location Availability");
                    }
                    updateMessage("Success");

                    // Update the screen after finished
                    Platform.runLater(() -> {
                        updatePane();
                        appendToLog("Store Location is: " + (Main.sdkLocation.isOpen() ? "OPEN" : "CLOSED"));
                        // hide the glass pane
                        Main.controller.hideProcessing();
                    });
                    return null;
                }
            };

            new Thread(setLocationAvailabilityTask).start();




        } catch (Exception e) {
            logger.error("Failed to handle Change Location Status ", e);
            appendToLog("Failed to change Store Location Availability");
        }

        event.consume();
    }


    private void appendToLog(String message) {
        txt_log.appendText(logDateFormat.format(new Date()) + " - " + message + "\n");
    }



    private void updatePane() {
        if (Main.sdkLocation == null) {
            logger.info("sdkLocation is null so clearing store information");
            lbl_storeStatus.setText("UNKNOWN");
            btn_changeStatus.setDisable(false);
            btn_changeStatus.setText("Click here to OPEN");
        } else {
            btn_changeStatus.setDisable(false);
            if (Main.sdkLocation.isOpen()) {
                lbl_storeStatus.setText("OPEN");
                btn_changeStatus.setText("Click here to CLOSE");
            } else {
                lbl_storeStatus.setText("CLOSED");
                btn_changeStatus.setText("Click here to OPEN");
            }
        }
    }

}
