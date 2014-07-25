package com.paypal.merchant.retail.tools.client;

import com.paypal.merchant.retail.log4jLogger.LogManager;
import com.paypal.merchant.retail.sdk.contract.PayPalMerchantRetailSDK;
import com.paypal.merchant.retail.sdk.contract.commands.*;
import com.paypal.merchant.retail.sdk.contract.entities.Location;
import com.paypal.merchant.retail.sdk.contract.exceptions.PPConfigurationException;
import com.paypal.merchant.retail.sdk.contract.exceptions.PPInvalidInputException;
import com.paypal.merchant.retail.sdk.internal.commands.PayPalMerchantRetailSDKImpl;
import com.paypal.merchant.retail.tools.exception.ClientException;
import com.paypal.merchant.retail.tools.util.PropertyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by Paolo on 7/23/2014.
 */
public enum SdkClient {
    INSTANCE;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private CommandBuilder commandBuilder;
    public final String LOCATION_ID = PropertyManager.INSTANCE.getProperty("sdk.location.id");
    public final String STORE_ID = PropertyManager.INSTANCE.getProperty("sdk.store.id");

    SdkClient() {
        try {
            logger.debug("Loading Config.xml for PayPal Merchant SDK");
            Source sdkConfig = new StreamSource(this.getClass().getClassLoader().getResourceAsStream("Config.xml"));

            logger.info("Creating new instance of PayPalMerchantRetailSDK | StoreId: " + STORE_ID + " | LocationId: " + LOCATION_ID);
            PayPalMerchantRetailSDKImpl sdkImpl = (PayPalMerchantRetailSDKImpl) PayPalMerchantRetailSDK.newInstance(sdkConfig);
            sdkImpl.registerLogManager(LogManager.newInstance());

            CommandBuilderContext builderContext = CommandBuilderContext.newInstance();
            builderContext.setStoreId(STORE_ID);
            commandBuilder = sdkImpl.newCommandBuilder(builderContext);

            logger.debug("Finished constructing SdkAdapterImpl");
        } catch (PPConfigurationException | PPInvalidInputException  e) {
            logger.error("Exception constructing SdkAdapterImpl ", e);
        }
    }

    /**
     * Returns SDK Merchant Location object
     * @return Location Object
     * @throws ClientException
     */
    public Location getSdkLocation() throws ClientException {
        try {
            logger.info("Calling out to the PayPal Merchant SDK: GetLocationRequest");
            GetLocationRequest request = GetLocationRequest.newInstance();
            request.setLookUpType(GetLocationRequest.IdLookUpType.LocationId);
            request.setId(LOCATION_ID);
            GetLocationCommand command = commandBuilder.build(request);
            executeCommand(command);
            GetLocationResponse response = command.getResponse();
            return response.getLocation();
        } catch (PPInvalidInputException e) {
            logger.error("Failed PayPal Merchant SDK: GetLocationRequest: ", e);
            throw new ClientException("Failed PayPal Merchant SDK: GetLocationRequest: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unknown exception: ", e);
            throw new ClientException("Failed PayPal Merchant SDK: GetLocationRequest: " + e.getMessage(), e);
        }
    }

    /**
     * Sets the Location to the desired state (Open or Closed)
     * @param sdkLocation
     * @param isOpen
     * @throws ClientException
     */
    public Location setLocationAvailability(Location sdkLocation, boolean isOpen) throws ClientException {
        try {
            logger.info("Calling out to the PayPal Merchant SDK: SetLocationAvailabilityRequest");
            logger.info("Setting isOpen to: " + isOpen);

            SetLocationAvailabilityRequest request = SetLocationAvailabilityRequest.newInstance();
            RequestBuilder.buildSetLocationAvailabilityRequest(request, sdkLocation, isOpen);
            SetLocationAvailabilityCommand command = commandBuilder.build(request);
            executeCommand(command);

            logger.info("Successfully set location availability to " + (isOpen ? "OPEN" : "CLOSED") + " for " + sdkLocation.getId());
            SetLocationAvailabilityResponse response = command.getResponse();
            return response.getLocation();
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



    /**
     * Private Builder Class - This will build the SDK Request Objects
     */
    private static class RequestBuilder {
        private static Logger logger = LoggerFactory.getLogger(RequestBuilder.class);
        private static void buildSetLocationAvailabilityRequest(final SetLocationAvailabilityRequest request, final Location sdkLocation, boolean isOpen) throws ClientException {
            try {
                request.setOpen(isOpen);
                request.setId(sdkLocation.getId());
            } catch (Exception e) {
                logger.error("Failed to build GetTabsRequest", e);
                throw new ClientException("Failed to build SetLocationAvailabilityRequest: " + e.getMessage(), e);
            }
        }
    }
}
