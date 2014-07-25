package com.paypal.merchant.retail.tools.controller;

import com.paypal.merchant.retail.tools.Main;
import com.paypal.merchant.retail.tools.client.SdkClient;
import com.paypal.merchant.retail.tools.exception.ClientException;
import com.paypal.merchant.retail.tools.util.PropertyManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Created by Paolo on 7/21/2014.
 */
public class MainController implements Initializable, ManagedPane {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    PaneManager paneManager;

    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
    private static SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @FXML
    private StackPane rootPane;

    @FXML
    private Pane mainPane, processingPane;

    @FXML
    private Label lbl_dateTime, lbl_storeStatus, lbl_storeId, lbl_locationId, lbl_street, lbl_city, lbl_state, lbl_phone, lbl_manager;

    @FXML
    private Button btn_changeStatus;

    @FXML
    private TextArea txt_log;

    @FXML
    private ImageView img_processing;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO
        logger.debug("initializing...");
        lbl_manager.setText(PropertyManager.INSTANCE.getProperty("store.manager"));
        bindToTime(lbl_dateTime);

        final Image processing = new Image(Main.class.getResourceAsStream("/images/LoadingWheel.gif"));
        img_processing.setImage(processing);

        for (Node n : rootPane.getChildren()) {
            logger.debug(n.getId());
        }

        appendToLog("SDK Tool initialized");

        try {
            updateScreen();
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
            processingPane.setVisible(true);

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
                        updateScreen();
                        appendToLog("Store Location is: " + (Main.sdkLocation.isOpen() ? "OPEN" : "CLOSED"));

                        // hide the glass pane
                        processingPane.setVisible(false);
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

    /**
     * Handles the manage store event
     *
     * @param event ActionEvent
     */
    @FXML
    protected void handleManageStore(ActionEvent event) {
        logger.debug("Handling Manage Store button");
        event.consume();
    }

    /**
     * Handles the manage store event
     *
     * @param event ActionEvent
     */
    @FXML
    protected void handleIssueRefund(ActionEvent event) {
        logger.debug("Handling Issue Refund button");
        event.consume();
    }

    private void updateScreen() {
        if (Main.sdkLocation == null) {
            logger.info("sdkLocation is null so clearing store information");

            lbl_storeId.setText(null);
            lbl_locationId.setText(null);
            lbl_street.setText(null);
            lbl_city.setText(null);
            lbl_state.setText(null);
            lbl_phone.setText(null);

            lbl_storeStatus.setText("UNKNOWN");
            btn_changeStatus.setDisable(true);
        } else {
            lbl_storeId.setText(Main.sdkLocation.getStoreId());
            lbl_locationId.setText(Main.sdkLocation.getId());
            lbl_street.setText(Main.sdkLocation.getAddress().getLine1());
            lbl_city.setText(Main.sdkLocation.getAddress().getCity());
            lbl_state.setText(Main.sdkLocation.getAddress().getState());
            lbl_phone.setText(Main.sdkLocation.getPhoneNumber());

            btn_changeStatus.setDisable(false);
            if (Main.sdkLocation.isOpen()) {
                lbl_storeStatus.setText("OPEN");
                btn_changeStatus.setText("Click here to CLOSE");
            } else {
                lbl_storeStatus.setText("CLOSED");
                btn_changeStatus.setText("Click here to OPEN");
            }
        }
        return;
    }

    private static void bindToTime(Labeled label) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        actionEvent -> {
                            Calendar time = Calendar.getInstance();
                            label.setText(dateTimeFormat.format(time.getTime()));
                        }
                ),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void appendToLog(String message) {
        txt_log.appendText(logDateFormat.format(new Date()) + " - " + message + "\n");
    }


}
