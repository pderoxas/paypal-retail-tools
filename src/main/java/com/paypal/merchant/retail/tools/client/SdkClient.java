package com.paypal.merchant.retail.tools.client;

import com.paypal.merchant.retail.log4jLogger.LogManager;
import com.paypal.merchant.retail.sdk.contract.PayPalMerchantRetailSDK;
import com.paypal.merchant.retail.sdk.contract.commands.*;
import com.paypal.merchant.retail.sdk.contract.entities.Location;
import com.paypal.merchant.retail.sdk.internal.commands.PayPalMerchantRetailSDKImpl;
import com.paypal.merchant.retail.tools.exception.ClientException;
import com.paypal.merchant.retail.tools.util.CallableWithTimeout;
import com.paypal.merchant.retail.tools.util.PropertyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by Paolo on 7/23/2014.
 *
 */
public enum SdkClient {
    INSTANCE;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private CommandBuilder commandBuilder;
    private String locationId;
    private final String STORE_ID = PropertyManager.INSTANCE.getProperty("sdk.store.id");
    private final long TIMEOUT_INTERVAL = PropertyManager.INSTANCE.getProperty("sdk.service.timeout.seconds", 30);
    private final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private SdkClient() {
        try {
            initialize();
        } catch (ClientException e) {
            logger.error("Failed to initialize the SdkClient singleton");
        }
    }

    private boolean initialize() throws ClientException {
        try {
            logger.debug("Loading Config.xml for PayPal Merchant SDK");
            Source sdkConfig = new StreamSource(this.getClass().getClassLoader().getResourceAsStream("Config.xml"));

            logger.info("Creating new instance of PayPalMerchantRetailSDKImpl | StoreId: " + STORE_ID);
            PayPalMerchantRetailSDKImpl sdkImpl =(PayPalMerchantRetailSDKImpl) PayPalMerchantRetailSDK.newInstance(sdkConfig);

            sdkImpl.registerLogManager(LogManager.newInstance());
            logger.info("Successfully created a new instance of PayPalMerchantRetailSDKImpl | StoreId: " + STORE_ID);

            locationId = sdkImpl.getSdkConfig().getStoreConfig(STORE_ID).getLocationId();

            CommandBuilderContext builderContext = CommandBuilderContext.newInstance();
            builderContext.setStoreId(STORE_ID);
            commandBuilder = sdkImpl.newCommandBuilder(builderContext);

            logger.debug("Finished initializing SdkClient");
            return true;
        } catch (Exception e) {
            logger.error("Exception initializing SdkClient", e);
            throw new ClientException("Exception initializing SdkClient", e);
        }
    }

    /**
     * Returns SDK Merchant Location object
     *
     * @return Location Object
     * @throws ClientException
     */
    public Location getSdkLocation() throws ClientException {
        try {
            Callable<Location> callable = () -> {
                logger.info("Calling out to the PayPal Merchant SDK: GetLocationRequest");
                GetLocationRequest request = GetLocationRequest.newInstance();
                request.setLookUpType(GetLocationRequest.IdLookUpType.LocationId);
                request.setId(locationId);
                GetLocationCommand command = commandBuilder.build(request);
                executeCommand(command);
                GetLocationResponse response = command.getResponse();
                return response.getLocation();
            };

            CallableWithTimeout<Location> callableWithTimeout = new CallableWithTimeout<>(callable,
                    TIMEOUT_INTERVAL,
                    TIMEOUT_UNIT);
            return callableWithTimeout.call();
        } catch (Exception e) {
            logger.error("Unknown exception: ", e);
            throw new ClientException("Failed PayPal Merchant SDK: GetLocationRequest: " + e.getMessage(), e);
        }
    }

    /**
     * Sets the Location to the desired state (Open or Closed)
     *
     * @param sdkLocation - The Location to update
     * @param isOpen - The desired state (true=Open, false=Closed)
     * @throws ClientException
     */
    public Location setLocationAvailability(Location sdkLocation, boolean isOpen) throws ClientException {
        try {
            Callable<Location> callable = () -> {

                boolean test = true;
                while(test) {
                    // do nothing
                }
                logger.info("Calling out to the PayPal Merchant SDK: SetLocationAvailabilityRequest");
                logger.info("Setting isOpen to: " + isOpen);

                SetLocationAvailabilityRequest request = SetLocationAvailabilityRequest.newInstance();
                request.setOpen(isOpen);
                request.setId(sdkLocation.getId());
                SetLocationAvailabilityCommand command = commandBuilder.build(request);
                executeCommand(command);

                logger.info("Successfully set location availability to " + (isOpen ? "OPEN" : "CLOSED") + " for " + sdkLocation.getId());
                SetLocationAvailabilityResponse response = command.getResponse();
                return response.getLocation();
            };

            CallableWithTimeout<Location> callableWithTimeout = new CallableWithTimeout<>(callable,
                    TIMEOUT_INTERVAL,
                    TIMEOUT_UNIT);
            return callableWithTimeout.call();

        } catch (Exception e) {
            logger.error("Failed PayPal Merchant SDK: SetLocationAvailabilityRequest: ", e);
            throw new ClientException("Failed PayPal Merchant SDK: SetLocationAvailabilityRequest: " + e.getMessage(), e);
        }
    }

    /**
     * Method that generically executes a command and throws an exception if error info is present
     *
     * @param command The command to execute
     * @throws ClientException
     */
    private void executeCommand(Command command) throws ClientException {
        CommandResult result = command.execute();
        ErrorInfo errorInfo = command.getErrorInfo();
        logger.info(command.getClass().getSimpleName() + " Result: " + result.name());
        if (result != CommandResult.Success && errorInfo != null && errorInfo.getData().size() > 0) {
            logger.error(command.getClass().getSimpleName() + ": Error Code: " + errorInfo.getCode().name());
            logger.error(command.getClass().getSimpleName() + ": Error ID: " + errorInfo.getData().get(0).getErrorId());
            logger.error(command.getClass().getSimpleName() + ": Error Msg: " + errorInfo.getData().get(0).getMessage());
            throw new ClientException(errorInfo.getData().get(0).getMessage());
        }
    }

//    /**
//     * Private Builder Class - This will build the SDK Request Objects
//     */
//    private static class RequestBuilder {
//        private static Logger logger = LoggerFactory.getLogger(RequestBuilder.class);
//
//        private static void buildSetLocationAvailabilityRequest(final SetLocationAvailabilityRequest request, final Location sdkLocation, boolean isOpen) throws ClientException {
//            try {
//                request.setOpen(isOpen);
//                request.setId(sdkLocation.getId());
//            } catch (Exception e) {
//                logger.error("Failed to build GetTabsRequest", e);
//                throw new ClientException("Failed to build SetLocationAvailabilityRequest: " + e.getMessage(), e);
//            }
//        }
//    }
}
