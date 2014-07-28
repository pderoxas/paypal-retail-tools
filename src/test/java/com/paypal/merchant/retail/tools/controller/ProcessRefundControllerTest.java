package com.paypal.merchant.retail.tools.controller;

import com.paypal.merchant.retail.tools.JavaFXThreadingRule;
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
import static org.junit.Assert.assertTrue;

public class ProcessRefundControllerTest {

    @org.junit.Rule
    public JavaFXThreadingRule rule = new JavaFXThreadingRule();

    static Stage primaryStage;
    static PaneManager paneManager;
    static Pane unitUnderTest;


    private static final String PANE = "UUT";
    private static final String PANE_FXML = "/fxml/processRefund.fxml";

    @Before
    public void setUp() throws Exception {

        primaryStage = new Stage();
        paneManager = new PaneManager();

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
    public void testFxml() {

        paneManager.loadPane(PANE, PANE_FXML);
        paneManager.setPane(PANE);
        unitUnderTest = (Pane) paneManager.getPane(PANE);

        assertTrue(unitUnderTest.isVisible());

        Label lbl_message = (Label) unitUnderTest.lookup("#lbl_message");
        assertEquals("Reserved for Process Refund", lbl_message.getText());

    }

}