package com.paypal.merchant.retail.tools.controller;

import com.paypal.merchant.retail.tools.Main;
import com.paypal.merchant.retail.tools.client.SdkClient;
import com.paypal.merchant.retail.tools.exception.ClientException;
import com.paypal.merchant.retail.tools.util.OneTimeTaskScheduler;
import com.paypal.merchant.retail.tools.util.PropertyManager;
import com.paypal.merchant.retail.tools.util.RepeatingTaskScheduler;
import com.paypal.merchant.retail.tools.util.TaskScheduler;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

/**
 * Created by Paolo on 7/21/2014.
 *
 */
public class StoreAvailabilityController implements Initializable, ManagedPane {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final long RETRY_INTERVAL = PropertyManager.INSTANCE.getProperty("sdk.service.retry.interval.seconds", 60);
    private final boolean IS_RETRY_ENABLED = PropertyManager.INSTANCE.getProperty("sdk.service.retry.enabled.flag", false);
    private final int RETRY_MAX_ATTEMPTS = PropertyManager.INSTANCE.getProperty("sdk.service.retry.max.attempts", 5);

    private PaneManager paneManager;
    private TaskScheduler locationAvailabilityUpdater;
    private SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private LocationAvailability currentLocationAvailability = LocationAvailability.UNKNOWN;
    private int openCloseAttemptCount = 0;

    @FXML
    private Label lbl_currentLocationStatus, lbl_countdown;

    @FXML
    private Button btn_openLocation, btn_closeLocation, btn_cancel;

    @FXML
    private TextArea txt_log;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.debug("initializing storeAvailabilityPane...");
        txt_log.setEditable(false);

        try {
            if (Main.getLocation() == null) {
                currentLocationAvailability = LocationAvailability.UNKNOWN;
                appendToLog("FAILED to initialize. Unable to get store location information!" );
            } else {
                if (Main.getLocation().isOpen()) {
                    currentLocationAvailability = LocationAvailability.OPEN;
                } else {
                    currentLocationAvailability = LocationAvailability.CLOSED;
                }
            }

            // Initialize the task scheduler

            if(IS_RETRY_ENABLED) {
                bindCountdownTimer(lbl_countdown);
                locationAvailabilityUpdater = new RepeatingTaskScheduler(updateLocationAvailability, 0, RETRY_INTERVAL, 1, TimeUnit.SECONDS);
            } else {
                locationAvailabilityUpdater = new OneTimeTaskScheduler(updateLocationAvailability, 0, 1, TimeUnit.SECONDS);
            }


            updatePane();
            btn_openLocation.setDisable(false);
            btn_closeLocation.setDisable(false);

            appendToLog("Initial store location availability: " + currentLocationAvailability.name());
        } catch (Exception e) {
            logger.error("Failed to initialize SDK Tool! ", e);
        }
    }

    @Override
    public void setParent(PaneManager paneManager) {
        this.paneManager = paneManager;
    }

    /**
     * Handles open location button
     *
     * @param event ActionEvent
     */
    @FXML
    protected void handleOpenLocation(ActionEvent event) {
        openCloseLocation(LocationAvailability.OPEN_PENDING);
        event.consume();
    }

    /**
     * Handles close location button
     *
     * @param event ActionEvent
     */
    @FXML
    protected void handleCloseLocation(ActionEvent event) {
        openCloseLocation(LocationAvailability.CLOSE_PENDING);
        event.consume();
    }

    private void openCloseLocation(LocationAvailability desiredAvailability) {
        btn_openLocation.setDisable(true);
        btn_closeLocation.setDisable(true);
        logger.debug("Setting location availability to: " + desiredAvailability.name());
        try {
            currentLocationAvailability = desiredAvailability;
            locationAvailabilityUpdater.start();
        } catch (Exception e) {
            logger.error("Setting location availability to: " + desiredAvailability.name(), e);
            appendToLog("Failed to set Store Location Availability to:" + desiredAvailability.toString());
        }
    }

    /**
     * Handles cancel button
     *
     * @param event ActionEvent
     */
    @FXML
    protected void handleCancel(ActionEvent event) {
        logger.debug("Handling Cancel button");
        // stop the current action
        locationAvailabilityUpdater.stop();

        btn_cancel.setDisable(true);
        btn_openLocation.setDisable(false);
        btn_closeLocation.setDisable(false);


        // Show the right action button based on current location availability
        switch (currentLocationAvailability) {
            case OPEN:
            case OPEN_PENDING:
                currentLocationAvailability = LocationAvailability.CLOSED;
                btn_closeLocation.setVisible(false);
                btn_openLocation.setVisible(true);
                break;
            case CLOSED:
            case CLOSE_PENDING:
                currentLocationAvailability = LocationAvailability.OPEN;
                btn_closeLocation.setVisible(true);
                btn_openLocation.setVisible(false);
                break;
            default:
                currentLocationAvailability = LocationAvailability.UNKNOWN;
                break;
        }

        appendToLog("Previous action canceled by user. Store Location is: " + currentLocationAvailability.toString());

        updatePane();
        event.consume();
    }

    private void updatePane() {
        lbl_currentLocationStatus.setText(currentLocationAvailability.toString());

        switch (currentLocationAvailability) {
            case OPEN:
            case OPEN_PENDING:
                btn_closeLocation.setVisible(true);
                btn_openLocation.setVisible(false);
                break;
            case CLOSED:
            case CLOSE_PENDING:
                btn_closeLocation.setVisible(false);
                btn_openLocation.setVisible(true);
                break;
            default:
                btn_closeLocation.setVisible(false);
                btn_openLocation.setVisible(true);
                break;
        }
    }


    /**
     * This set the location availability via the SDK
     */
    public final Runnable updateLocationAvailability = () -> {
        boolean isSuccessful = false;
        try {
            // if either open or open pending, set desiredState to true
            boolean desiredIsOpen = currentLocationAvailability.equals(LocationAvailability.OPEN_PENDING) ||
                    currentLocationAvailability.equals(LocationAvailability.OPEN);

            Platform.runLater(() -> {
                Main.getController().showProcessing();
                updatePane();
                lbl_countdown.setText("");
                appendToLog((openCloseAttemptCount > 0 ? "Retry #" + openCloseAttemptCount + ": " : "") +
                        "Attempting to set Store Location Availability to: " + (desiredIsOpen ? "OPEN" : "CLOSED"));
                // hide the glass pane
            });

            if(Main.getLocation() == null) {
                logger.debug("SDK Location is null so retrieve it before trying to set the desired availability");
                Main.setLocation(SdkClient.INSTANCE.getSdkLocation());
            }

            logger.debug("Call out to SDK to set the location isOpen to: " + desiredIsOpen);
            Main.setLocation(SdkClient.INSTANCE.setLocationAvailability(Main.getLocation(), desiredIsOpen));

            if(Main.getLocation().isOpen()) {
                currentLocationAvailability = LocationAvailability.OPEN;
            } else {
                currentLocationAvailability = LocationAvailability.CLOSED;
            }

            // If successful, it will stop the scheduler.  Otherwise, the scheduler will continue
            locationAvailabilityUpdater.stop();
            openCloseAttemptCount = 0;

            Platform.runLater(() -> {
                btn_openLocation.setDisable(false);
                btn_closeLocation.setDisable(false);
            });
        } catch (ClientException e) {
            logger.error("Failed to set Store Location Availability");

            if(IS_RETRY_ENABLED) {
                if(openCloseAttemptCount >= RETRY_MAX_ATTEMPTS){
                    openCloseAttemptCount = 0;
                    logger.debug("Maximum number of retry attempts. Stopping the scheduler.");
                    Platform.runLater(() -> {
                        appendToLog(e.getMessage());
                        appendToLog("Maximum number of retry attempts have been reached.");
                        handleCancel(null);
                    });
                } else {
                    openCloseAttemptCount++;
                    Platform.runLater(() -> {
                        appendToLog(e.getMessage());
                        appendToLog("Automatic retry in " + getCountdownString(RETRY_INTERVAL));
                    });
                }
            } else {
                locationAvailabilityUpdater.stop();
                Platform.runLater(() -> {
                    appendToLog(e.getMessage());
                    appendToLog("Manual retry is required.");
                    //re-enable the buttons
                    btn_openLocation.setDisable(false);
                    btn_closeLocation.setDisable(false);

                    // Set the current location availability to the last known value
                    if(currentLocationAvailability.equals(LocationAvailability.OPEN_PENDING)){
                        currentLocationAvailability = LocationAvailability.CLOSED;
                        lbl_countdown.setText("Last attempt to Open the store location failed. Manual retry is required.");
                    } else if(currentLocationAvailability.equals(LocationAvailability.CLOSE_PENDING)){
                        currentLocationAvailability = LocationAvailability.OPEN;
                        lbl_countdown.setText("Last attempt to Close the store location failed. Manual retry is required.");
                    }
                });
            }
        } finally {
            Platform.runLater(() -> {
                updatePane();
                appendToLog("Store Location is: " + currentLocationAvailability.toString());
                // hide the glass pane
                Main.getController().hideProcessing();
            });
        }
    };

    /**
     * Binds the status message label to the number or remaining seconds until the next time
     * call to set location availability is made
     * @param label - The label to bind
     */
    private void bindCountdownTimer(Labeled label) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(500),
                        actionEvent -> {
                            if(locationAvailabilityUpdater.getDelayTime() > 0 &&
                                (currentLocationAvailability.equals(LocationAvailability.OPEN_PENDING) ||
                                currentLocationAvailability.equals(LocationAvailability.CLOSE_PENDING)) ) {
                                label.setText("Automatic retry in " + getCountdownString(locationAvailabilityUpdater.getDelayTime()));
                                btn_cancel.setDisable(false);
                            } else {
                                label.setText("");
                                btn_cancel.setDisable(true);
                            }
                        }
                ),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Returns a count down string in mm:ss format
     * @param totalSeconds - The total number of remaining seconds
     * @return String formatted as mm:ss
     */
    private static String getCountdownString(long totalSeconds) {
        String minutes = String.valueOf(totalSeconds / 60);
        String seconds = String.valueOf(totalSeconds % 60);
        return org.apache.commons.lang3.StringUtils.leftPad(minutes, 2, "0") + ":" +
                org.apache.commons.lang3.StringUtils.leftPad(seconds, 2, "0");
    }

    /**
     * Appends text to the log textarea with a timestamp
     * @param message - The message to add
     */
    private void appendToLog(String message) {
        txt_log.appendText(logDateFormat.format(new Date()) + " - " + message + "\n");
    }

    enum LocationAvailability {
        UNKNOWN("Unknown"),
        OPEN("Open"),
        OPEN_PENDING("Open (Pending)"),
        CLOSED("Closed"),
        CLOSE_PENDING("Closed (Pending)");

        private String status;
        private LocationAvailability(String status) {
            this.status = status;
        }

        @Override
        public String toString(){
            return status;
        }
    }

}
