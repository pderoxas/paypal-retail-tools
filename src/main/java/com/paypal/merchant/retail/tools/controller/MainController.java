package com.paypal.merchant.retail.tools.controller;

import com.paypal.merchant.retail.tools.Main;
import com.paypal.merchant.retail.tools.util.PropertyManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

/**
 * Created by Paolo on 7/21/2014.
 */
public class MainController implements Initializable, ManagedPane {
    private static Logger logger = LoggerFactory.getLogger(MainController.class);
    private PaneManager paneManager;

    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    public static final String STORE_AVAILABILITY = "storeAvailability";
    public static final String STORE_AVAILABILITY_FXML = "/fxml/storeAvailability.fxml";

    public static final String PROCESS_REFUND = "processRefund";
    public static final String PROCESS_REFUND_FXML = "/fxml/processRefund.fxml";


    @FXML
    private HBox mainHBox;

    @FXML
    private Pane processingPane;

    @FXML
    private Label lbl_dateTime, lbl_storeId, lbl_locationId, lbl_addressLine1, lbl_addressLine2, lbl_addressLine3, lbl_addressLine4, lbl_phone, lbl_manager;

    @FXML
    private ImageView img_processing;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logger.debug("initializing...");
            this.lbl_manager.setText(PropertyManager.INSTANCE.getProperty("store.manager"));
            bindToTime(lbl_dateTime);

            paneManager = new PaneManager();
            paneManager.setId("rootPaneManager");
            paneManager.loadPane(STORE_AVAILABILITY, STORE_AVAILABILITY_FXML);
            paneManager.loadPane(PROCESS_REFUND, PROCESS_REFUND_FXML);
            paneManager.setPane(STORE_AVAILABILITY);
            mainHBox.getChildren().add(paneManager);

            final Image processing = new Image(Main.class.getResourceAsStream("/images/LoadingWheel.gif"));
            img_processing.setImage(processing);

            updateStoreInfoControls();
        } catch (Exception e) {
            logger.error("Failed to initialize SDK Tool! ", e);
        }
    }

    @Override
    public void setParent(PaneManager paneManager) {
        this.paneManager = paneManager;
    }

    /**
     * Handles the manage store event
     *
     * @param event ActionEvent
     */
    @FXML
    protected void handleManageStore(ActionEvent event) {
        logger.debug("Handling Manage Store button");
        paneManager.setPane(STORE_AVAILABILITY);
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
        paneManager.setPane(PROCESS_REFUND);
        event.consume();
    }

    public void updateStoreInfoControls() {
        if (Main.getLocation() == null) {
            logger.info("sdkLocation is null so clearing store information");
            lbl_storeId.setText(null);
            lbl_locationId.setText(null);
            lbl_phone.setText(null);
            lbl_addressLine1.setText(null);
            lbl_addressLine2.setText(null);
            lbl_addressLine3.setText(null);
            lbl_addressLine4.setText(null);
        } else {
            lbl_storeId.setText(Main.getLocation().getStoreId());
            lbl_locationId.setText(Main.getLocation().getId());
            lbl_phone.setText(Main.getLocation().getPhoneNumber());
            if(Main.getLocation().getAddress() != null){
                lbl_addressLine1.setText(Main.getLocation().getAddress().getLine1());
                String cityState = Main.getLocation().getAddress().getCity() + ", " +
                        Main.getLocation().getAddress().getState();
                if(StringUtils.isNotBlank(Main.getLocation().getAddress().getLine2())) {
                    lbl_addressLine2.setText(Main.getLocation().getAddress().getLine2());
                    lbl_addressLine3.setText(cityState);
                    lbl_addressLine4.setText(Main.getLocation().getAddress().getPostalCode());
                } else {
                    lbl_addressLine2.setText(cityState);
                    lbl_addressLine3.setText(Main.getLocation().getAddress().getPostalCode());
                    lbl_addressLine4.setText(null);
                }
            }
        }
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

    public void showProcessing() {
        processingPane.setVisible(true);
    }

    public void hideProcessing() {
        processingPane.setVisible(false);
    }

}
