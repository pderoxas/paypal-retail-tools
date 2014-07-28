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
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MainControllerTest {

    @org.junit.Rule
    public JavaFXThreadingRule rule = new JavaFXThreadingRule();

    static Stage primaryStage;
    static PaneManager paneManager;
    static Pane mainPane;
    static Location sdkLocation;

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
    public void testMainFxmlWithAddressLine2() {

        Main.setLocation(sdkLocation);

        paneManager.loadPane("main", "/fxml/main.fxml");
        paneManager.setPane("main");

        mainPane = (Pane) paneManager.getPane("main");
        assertTrue(mainPane.isVisible());


        Label storeId = (Label) mainPane.lookup("#lbl_storeId");
        assertEquals(storeId.getText(), "TestStoreId");

        Label locationId = (Label) mainPane.lookup("#lbl_locationId");
        assertEquals(locationId.getText(), "TestLocationId");

        Label addressLine1 = (Label) mainPane.lookup("#lbl_addressLine1");
        assertEquals(addressLine1.getText(), "123 Main St");

        Label addressLine2 = (Label) mainPane.lookup("#lbl_addressLine2");
        assertEquals(addressLine2.getText(), "Suite 200");

        Label addressLine3 = (Label) mainPane.lookup("#lbl_addressLine3");
        assertEquals(addressLine3.getText(), "Providence, RI");

        Label addressLine4 = (Label) mainPane.lookup("#lbl_addressLine4");
        assertEquals(addressLine4.getText(), "02903");

        Label phone = (Label) mainPane.lookup("#lbl_phone");
        assertEquals(phone.getText(), "800-555-1234");

        Label manager = (Label) mainPane.lookup("#lbl_manager");
        assertEquals(manager.getText(), "Joseph Smith");
    }

    @Test
    public void testMainFxmlWithoutAddressLine2() {
        sdkLocation.getAddress().setLine2(null);
        Main.setLocation(sdkLocation);

        paneManager.loadPane("main", "/fxml/main.fxml");
        paneManager.setPane("main");

        mainPane = (Pane) paneManager.getPane("main");
        assertTrue(mainPane.isVisible());

        Label storeId = (Label) mainPane.lookup("#lbl_storeId");
        assertEquals(storeId.getText(), "TestStoreId");

        Label locationId = (Label) mainPane.lookup("#lbl_locationId");
        assertEquals(locationId.getText(), "TestLocationId");

        Label addressLine1 = (Label) mainPane.lookup("#lbl_addressLine1");
        assertEquals(addressLine1.getText(), "123 Main St");

        Label addressLine2 = (Label) mainPane.lookup("#lbl_addressLine2");
        assertEquals(addressLine2.getText(), "Providence, RI");

        Label addressLine3 = (Label) mainPane.lookup("#lbl_addressLine3");
        assertEquals(addressLine3.getText(), "02903");

        Label addressLine4 = (Label) mainPane.lookup("#lbl_addressLine4");
        assertNull(addressLine4.getText());

        Label phone = (Label) mainPane.lookup("#lbl_phone");
        assertEquals(phone.getText(), "800-555-1234");

        Label manager = (Label) mainPane.lookup("#lbl_manager");
        assertEquals(manager.getText(), "Joseph Smith");
    }
}