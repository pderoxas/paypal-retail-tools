package com.paypal.merchant.retail.tools.controller;

import com.paypal.merchant.retail.tools.Main;
import com.paypal.merchant.retail.tools.client.SdkClient;
import com.paypal.merchant.retail.tools.exception.ClientException;
import com.paypal.merchant.retail.tools.util.PropertyManager;
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

/**
 * Created by Paolo on 7/21/2014.
 *
 */
public class StoreAvailabilityController implements Initializable, ManagedPane {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private PaneManager paneManager;
    private TaskScheduler locationAvailabilityUpdater;

    private SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private long retryInterval = PropertyManager.INSTANCE.getProperty("update.location.retry.interval.seconds", 60);
    private LocationStatus currentLocationAvailability = LocationStatus.UNKNOWN;

    @FXML
    private Label lbl_currentLocationStatus, lbl_countdown;

    @FXML
    private Button btn_openLocation, btn_closeLocation, btn_cancel;

    @FXML
    private TextArea txt_log;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.debug("initializing storeAvailabilityPane...");

        try {
            if (Main.getLocation() == null) {
                currentLocationAvailability = LocationStatus.UNKNOWN;
                appendToLog("FAILED to initialize. Unable to get store location information!" );
            } else {
                if (Main.getLocation().isOpen()) {
                    currentLocationAvailability = LocationStatus.OPEN;
                } else {
                    currentLocationAvailability = LocationStatus.CLOSED;
                }
            }

            // Initialize the task scheduler
            locationAvailabilityUpdater = new TaskScheduler(updateLocationAvailability, 0, retryInterval, 1);
            bindCountdownTimer(lbl_countdown);
            updatePane();

            appendToLog("Initial store location availability: " + currentLocationAvailability.toString());
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
        logger.debug("Handling OPEN Location button");
        try {
            currentLocationAvailability = LocationStatus.OPEN_PENDING;
            locationAvailabilityUpdater.start();
        } catch (Exception e) {
            logger.error("Failed to handle Change Location Status ", e);
            appendToLog("Failed to change Store Location Availability");
        }
        event.consume();
    }

    /**
     * Handles close location button
     *
     * @param event ActionEvent
     */
    @FXML
    protected void handleCloseLocation(ActionEvent event) {
        logger.debug("Handling CLOSE Location button");
        try {
            currentLocationAvailability = LocationStatus.CLOSE_PENDING;
            locationAvailabilityUpdater.start();
        } catch (Exception e) {
            logger.error("Failed to handle Change Location Status ", e);
            appendToLog("Failed to change Store Location Availability");
        }
        event.consume();
    }

    /**
     * Handles cancel button
     *
     * @param event ActionEvent
     */
    @FXML
    protected void handleCancel(ActionEvent event) {
        logger.debug("Handling Cancel button");
        btn_cancel.setDisable(true);

        // If successful, it will stop the scheduler.  Otherwise, the scheduler will continue
        locationAvailabilityUpdater.stop();

        // Show the right action button based on current location availability
        switch (currentLocationAvailability) {
            case OPEN:
            case OPEN_PENDING:
                currentLocationAvailability = LocationStatus.CLOSED;
                btn_closeLocation.setVisible(false);
                btn_openLocation.setVisible(true);
                break;
            case CLOSED:
            case CLOSE_PENDING:
                currentLocationAvailability = LocationStatus.OPEN;
                btn_closeLocation.setVisible(true);
                btn_openLocation.setVisible(false);
                break;
            default:
                currentLocationAvailability = LocationStatus.UNKNOWN;
                break;
        }

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
        // This will only poll if PayCode Entry type is BLE
        try {
            boolean desiredState = currentLocationAvailability.equals(LocationStatus.OPEN) || currentLocationAvailability.equals(LocationStatus.OPEN_PENDING);

            Platform.runLater(() -> {
                updatePane();
                lbl_countdown.setText("");
                appendToLog("Attempting to set Store Location Availability to: " + (desiredState ? "OPEN" : "CLOSED"));
                // hide the glass pane
                Main.getController().showProcessing();
            });

            if(Main.getLocation() == null) {
                logger.debug("SDK Location is null so retrieve it before trying to set the desired availability");
                Main.setLocation(SdkClient.INSTANCE.getSdkLocation());
            }

            logger.debug("Call out to SDK to set the location availability to: " + desiredState);
            Main.setLocation(SdkClient.INSTANCE.setLocationAvailability(Main.getLocation(), desiredState));

            if(Main.getLocation().isOpen()) {
                currentLocationAvailability = LocationStatus.OPEN;
            } else {
                currentLocationAvailability = LocationStatus.CLOSED;
            }

            // If successful, it will stop the scheduler.  Otherwise, the scheduler will continue
            locationAvailabilityUpdater.stop();
        } catch (ClientException e) {
            logger.error("Failed to set Store Location Availability");
            Platform.runLater(() -> appendToLog(e.getMessage()));
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
                                    (currentLocationAvailability.equals(LocationStatus.OPEN_PENDING) ||
                                            currentLocationAvailability.equals(LocationStatus.CLOSE_PENDING) )) {

                                label.setText("Automatic retry in " + getCountdownString(locationAvailabilityUpdater.getDelayTime()));
                                btn_cancel.setDisable(false);
                                btn_openLocation.setDisable(true);
                                btn_closeLocation.setDisable(true);
                            } else {
                                label.setText("");
                                btn_cancel.setDisable(true);
                                btn_openLocation.setDisable(false);
                                btn_closeLocation.setDisable(false);
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

    enum LocationStatus {
        UNKNOWN("Unknown"),
        OPEN("Open"),
        OPEN_PENDING("Open (Pending)"),
        CLOSED("Closed"),
        CLOSE_PENDING("Closed (Pending)");

        private String status;
        private LocationStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString(){
            return status;
        }
    }

}
