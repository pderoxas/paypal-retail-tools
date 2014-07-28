package com.paypal.merchant.retail.tools.controller;

import com.paypal.merchant.retail.sdk.contract.entities.Address;
import com.paypal.merchant.retail.sdk.contract.entities.Location;
import com.paypal.merchant.retail.sdk.internal.entities.AddressImpl;
import com.paypal.merchant.retail.sdk.internal.entities.LocationImpl;
import com.paypal.merchant.retail.tools.JavaFXThreadingRule;
import com.paypal.merchant.retail.tools.Main;
import com.paypal.merchant.retail.tools.util.PropertyManager;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StoreAvailabilityControllerTest {

    @org.junit.Rule
    public JavaFXThreadingRule rule = new JavaFXThreadingRule();

    static Stage primaryStage;
    static PaneManager paneManager;
    static Pane unitUnderTest;
    static Location sdkLocation;

    private static final String PANE = "UUT";
    private static final String PANE_FXML = "/fxml/storeAvailability.fxml";

    @Before
    public void setUp() throws Exception {

        primaryStage = new Stage();
        paneManager = new PaneManager();

        sdkLocation = new LocationImpl();
        sdkLocation.setId("TestLocationId");
        sdkLocation.setStoreId("TestStoreId");
        sdkLocation.setPhoneNumber("800-555-1234");

        Address address = new AddressImpl();
        address.setLine1("123 Main St");
        address.setLine2("Suite 200");
        address.setCity("Providence");
        address.setState("RI");
        address.setPostalCode("02903");
        sdkLocation.setAddress(address);

        Group root = new Group();
        root.getChildren().addAll(paneManager);
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/main.css");

        Stage primaryStage = new Stage();
        primaryStage.setScene(scene);
        primaryStage.setTitle(PropertyManager.INSTANCE.getProperty("application.title"));
        primaryStage.show();

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }

    @After
    public void tearDown() throws Exception {
        primaryStage.close();
    }

    @Test
    public void testFxmlWithOpenLocation() {

        sdkLocation.setOpen(true);
        Main.setLocation(sdkLocation);

        paneManager.loadPane(PANE, PANE_FXML);
        paneManager.setPane(PANE);
        unitUnderTest = (Pane) paneManager.getPane(PANE);

        assertTrue(unitUnderTest.isVisible());

        Label lbl_currentLocationStatus = (Label) unitUnderTest.lookup("#lbl_currentLocationStatus");
        assertEquals("Open", lbl_currentLocationStatus.getText());

        Button btn_closeLocation = (Button) unitUnderTest.lookup("#btn_closeLocation");
        assertEquals(true, btn_closeLocation.isVisible());

        Button btn_openLocation = (Button) unitUnderTest.lookup("#btn_openLocation");
        assertEquals(false, btn_openLocation.isVisible());

        Button btn_cancel = (Button) unitUnderTest.lookup("#btn_cancel");
        assertEquals(true, btn_cancel.isDisabled());
    }

    @Test
    public void testFxmlWithClosedLocation() {

        sdkLocation.setOpen(false);
        Main.setLocation(sdkLocation);

        paneManager.loadPane(PANE, PANE_FXML);
        paneManager.setPane(PANE);
        unitUnderTest = (Pane) paneManager.getPane(PANE);

        assertTrue(unitUnderTest.isVisible());

        Label lbl_currentLocationStatus = (Label) unitUnderTest.lookup("#lbl_currentLocationStatus");
        assertEquals("Closed", lbl_currentLocationStatus.getText());

        Button btn_closeLocation = (Button) unitUnderTest.lookup("#btn_closeLocation");
        assertEquals(false, btn_closeLocation.isVisible());

        Button btn_openLocation = (Button) unitUnderTest.lookup("#btn_openLocation");
        assertEquals(true, btn_openLocation.isVisible());

        Button btn_cancel = (Button) unitUnderTest.lookup("#btn_cancel");
        assertEquals(true, btn_cancel.isDisabled());
    }

    @Test
    public void testFxmlWithNullLocation() {
        Main.setLocation(null);

        paneManager.loadPane(PANE, PANE_FXML);
        paneManager.setPane(PANE);
        unitUnderTest = (Pane) paneManager.getPane(PANE);

        assertTrue(unitUnderTest.isVisible());

        Label lbl_currentLocationStatus = (Label) unitUnderTest.lookup("#lbl_currentLocationStatus");
        assertEquals("Unknown", lbl_currentLocationStatus.getText());

        Button btn_closeLocation = (Button) unitUnderTest.lookup("#btn_closeLocation");
        assertEquals(false, btn_closeLocation.isVisible());

        Button btn_openLocation = (Button) unitUnderTest.lookup("#btn_openLocation");
        assertEquals(true, btn_openLocation.isVisible());

        Button btn_cancel = (Button) unitUnderTest.lookup("#btn_cancel");
        assertEquals(true, btn_cancel.isDisabled());
    }

}