package com.paypal.merchant.retail.tools.util;

import com.paypal.merchant.retail.tools.exception.ConfigException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * Created by Paolo on 3/20/14.
 *
 * <p>Utility methods related to handling Properties files
 *
 * <p>Use enum-based singleton pattern which has key advantages over traditional singleton pattern
 * <li>Easier to write</li>
 * <li>Serialization is guaranteed by the jvm</li>
 * <li>Enum is thread-safe; no need for double checked locking</li>
 *
 */
public enum PropertyManager {
    INSTANCE;
    private static Logger logger = LoggerFactory.getLogger(PropertyManager.class);
    private Properties properties = null;
    public static final String DEFAULT_PROP_FILE_PATH = "sdk-tool-properties.xml";  //by default, look on classpath for the file

    /**
     * Load the default properties file
     * @throws ConfigException
     */
    public void loadProperties() throws ConfigException {
        loadCustomProperties(DEFAULT_PROP_FILE_PATH);
    }

    /**
     * Initializes the properties file with an existing Properties instance.
     * Used for unit testing to be able to mock properties or use custom properties.
     */
    public void loadCustomProperties(Properties props) throws ConfigException {
        properties = props;
    }

    /**
     * Initializes the PropertyManager instance with a given file.
     * This is public to allow loading of various properties file for testing purposes.
     */
    public void loadCustomProperties(String propertiesFileName) throws ConfigException {
        logger.debug("Attempting to load properties file: " + propertiesFileName);
        try {
            logger.debug("Loading properties file for the first time.");
            InputStream inputStream;

            // Allow the definition of the properties via command line (-DpropertiesFilename=/path/to/file")
            if (System.getProperty(propertiesFileName) != null) {
                logger.debug("Loading properties using System property: " + System.getProperty(propertiesFileName));
                try{
                    inputStream = new FileInputStream(System.getProperty(propertiesFileName));
                }
                catch (FileNotFoundException e){
                    throw new ConfigException("The properties file defined by System property (" + propertiesFileName + ") could not be found at " + System.getProperty(propertiesFileName));
                }
            }
            else {
                logger.debug("Loading properties from classpath");
                inputStream = PropertyManager.class.getClassLoader().getResourceAsStream(propertiesFileName);
                if (inputStream == null) {
                    throw new ConfigException("Properties file, " + propertiesFileName + ", could not be found on the classpath.");
                }
            }
            try {
                properties = new Properties();
                if (FilenameUtils.getExtension(propertiesFileName).equalsIgnoreCase("xml")) {
                    properties.loadFromXML(inputStream);
                }
                else {
                    //assume its a text based properties file
                    properties.load(inputStream);
                }
            }
            finally {
                inputStream.close();
            }
        }
        catch (InvalidPropertiesFormatException e) {
            throw new ConfigException("InvalidPropertiesFormatException: Problem loading properties file: " + propertiesFileName + ". " + e.getMessage());
        }
        catch (IOException e) {
            throw new ConfigException("IOException: Problem opening properties file: " + propertiesFileName + ". " + e.getMessage());
        }
    }

    public String getProperty(String propertyName) {
        if(properties == null) {
            try {
                loadProperties();
            }
            catch (ConfigException e) {
                logger.error(e.getMessage());
                return null;
            }
        }
        return properties.getProperty(propertyName);
    }

    /**
     * Allows us to override a loaded property if needed as in the case with unit testing
     * @param propertyName - The property to set - will only exist in scope of Properties
     * @param propertyValue - The value to set
     */
    public void setProperty(String propertyName, String propertyValue) {
        if(properties == null) {
            try {
                loadProperties();
                properties.setProperty(propertyName, propertyValue);
            }
            catch (ConfigException e) {
                logger.error(e.getMessage());
            }
        } else {
            properties.setProperty(propertyName, propertyValue);
        }

    }

    /**
     * Get a String property and return the default if property does not exist
     * @param propertyName - The key name of the property
     * @param defaultValue String
     * @return String
     */
    public String getProperty(String propertyName, String defaultValue) {
        if(properties == null) {
            try {
                loadProperties();
            }
            catch (ConfigException e) {
                logger.error(e.getMessage());
                return defaultValue;
            }
        }
        return properties.getProperty(propertyName, defaultValue);
    }

    /**
     * Get a boolean property and return the default if property does not exist
     * @param propertyName - The key name of the property
     * @param defaultValue boolean
     * @return boolean
     */
    public boolean getProperty(String propertyName, boolean defaultValue) {
        if(properties == null) {
            try {
                loadProperties();
            }
            catch (ConfigException e) {
                logger.error(e.getMessage());
                return defaultValue;
            }
        }
        String propValue = properties.getProperty(propertyName);
        if(StringUtils.isBlank(propValue)){
            return defaultValue;
        }

        try{
            return Boolean.parseBoolean(propValue);
        } catch(NumberFormatException e){
            return defaultValue;
        }
    }

    /**
     * Get a double property and return the default if property does not exist
     * @param propertyName - The key name of the property
     * @param defaultValue double
     * @return double
     */
    public double getProperty(String propertyName, double defaultValue) {
        if(properties == null) {
            try {
                loadProperties();
            }
            catch (ConfigException e) {
                logger.error(e.getMessage());
                return defaultValue;
            }
        }
        String propValue = properties.getProperty(propertyName);
        if(StringUtils.isBlank(propValue)){
            return defaultValue;
        }

        try{
            return Double.parseDouble(propValue);
        } catch(NumberFormatException e){
            return defaultValue;
        }
    }

    /**
     * Get an int property and return the default if property does not exist
     * @param propertyName - The key name of the property
     * @param defaultValue int
     * @return int
     */
    public int getProperty(String propertyName, int defaultValue) {
        if(properties == null) {
            try {
                loadProperties();
            }
            catch (ConfigException e) {
                logger.error(e.getMessage());
                return defaultValue;
            }
        }
        String propValue = properties.getProperty(propertyName);
        if(StringUtils.isBlank(propValue)){
            return defaultValue;
        }

        try{
            return Integer.parseInt(propValue);
        } catch(NumberFormatException e){
            return defaultValue;
        }
    }

    /**
     * Get a long property and return the default if property does not exist
     * @param propertyName - The key name of the property
     * @param defaultValue long
     * @return long
     */
    public long getProperty(String propertyName, long defaultValue) {
        if(properties == null) {
            try {
                loadProperties();
            }
            catch (ConfigException e) {
                logger.error(e.getMessage());
            }
        }
        String propValue = properties.getProperty(propertyName);
        if(StringUtils.isBlank(propValue)){
            return defaultValue;
        }

        try{
            return Long.parseLong(propValue);
        } catch(NumberFormatException e){
            return defaultValue;
        }
    }

    /**
     * Get a BigDecimal property and return the default if property does not exist
     * @param propertyName - The key name of the property
     * @param defaultValue BigDecimal
     * @return BigDecimal
     */
    public BigDecimal getProperty(String propertyName, BigDecimal defaultValue) {
        if(properties == null) {
            try {
                loadProperties();
            }
            catch (ConfigException e) {
                logger.error(e.getMessage());
            }
        }
        String propValue = properties.getProperty(propertyName);
        if(StringUtils.isBlank(propValue)){
            return defaultValue;
        }

        try{
            return BigDecimal.valueOf(Double.valueOf(propValue));
        } catch(NumberFormatException e){
            return defaultValue;
        }
    }
}
