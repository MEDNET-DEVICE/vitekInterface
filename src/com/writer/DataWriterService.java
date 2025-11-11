package com.writer;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.reader.LabConnectUtil;
import jssc.SerialPort;

/**
 * Main service class for ASTM Data Writer
 * Provides the entry point for starting/stopping the data writer functionality
 */
public class DataWriterService {
    
    private static DataWriterConnector writerConnector;
    private static DataWriterConfig config;
    private static ApiDataProvider dataProvider;
    private static boolean isInitialized = false;
    
    /**
     * Initialize the data writer service
     */
    public static boolean initialize() {
        if (isInitialized) {
            log("DataWriterService: Already initialized");
            return true;
        }
        
        try {
            log("DataWriterService: Initializing...");
            
            // Load configuration
            config = new DataWriterConfig();
            boolean configLoaded = config.loadConfiguration();
            
            if (!configLoaded) {
                log("DataWriterService: Using default configuration");
            }
            
            // Validate configuration
            if (!config.validateConfiguration()) {
                log("DataWriterService: Configuration validation failed");
                return false;
            }
            
            // Check if writer is enabled
            if (!config.isEnabled()) {
                log("DataWriterService: Data writer is disabled in configuration");
                return false;
            }

            String propertyFileSuffix = "VITEK2COMPACT";
            ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream("D://"+ File.separator+"mednet"+File.separator+"windowsService"+propertyFileSuffix+".properties"));

            log("DataWriterService: Configuration loaded successfully");
            log(config.getConfigurationSummary());
            
            // Initialize data provider and connector
            dataProvider = new ApiDataProvider();
            dataProvider.configure(
                config.getApiBaseUrl(bundle),
                config.getMachineCode(bundle),
                config.getCompanyId(bundle)
            );
            dataProvider.setFetchInterval(config.getPollingInterval());
            
            // Configure additional API settings from ResourceBundle
            configureApiService(dataProvider, config);
            
            writerConnector = new DataWriterConnector();
            writerConnector.configure(config.getComPort(bundle), dataProvider);
            
            writerConnector.configureSerialPort(
                config.getBaudRate(),
                config.getDataBits(),
                config.getStopBits(),
                config.getParity()
            );
            
            writerConnector.setPollingInterval(config.getPollingInterval());
            
            // Test configuration
            /*if (!writerConnector.testConfiguration()) {
                log("DataWriterService: Configuration test failed");
                return false;
            }*/
            
            // Test API connection
            if (!dataProvider.testConnection()) {
                log("DataWriterService: API connection test failed, but continuing...");
                // Don't fail initialization if API is temporarily unavailable
            }
            
            isInitialized = true;
            log("DataWriterService: Initialized successfully");
            return true;
            
        } catch (Exception ex) {
            log("DataWriterService: Initialization failed: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * Start the data writer service
     */
    public static boolean start(SerialPort serialPort) {
        if (!isInitialized) {
            log("DataWriterService: Not initialized, attempting to initialize...");
            if (!initialize()) {
                log("DataWriterService: Initialization failed, cannot start");
                return false;
            }
        }
        
        try {
            log("DataWriterService: Starting...");
            writerConnector.start(serialPort);
            log("DataWriterService: Started successfully");
            return true;
            
        } catch (Exception ex) {
            log("DataWriterService: Failed to start: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * Stop the data writer service
     */
    public static void stop(SerialPort serialPort) {
        try {
            log("DataWriterService: Stopping...");
            
            if (writerConnector != null) {
                writerConnector.stop(serialPort);
            }
            
            log("DataWriterService: Stopped successfully");
            
        } catch (Exception ex) {
            log("DataWriterService: Error during stop: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    
    /**
     * Get API data provider configuration
     */
    public static String getApiConfiguration() {
        if (dataProvider != null) {
            return dataProvider.getConfigurationSummary();
        } else {
            return "Data provider not initialized";
        }
    }
    
    /**
     * Test API connection
     */
    public static boolean testApiConnection() {
        if (dataProvider != null) {
            return dataProvider.testConnection();
        } else {
            log("DataWriterService: Data provider not initialized");
            return false;
        }
    }
    
    /**
     * Get the data provider for external systems to add data
     * @return The data provider instance
     */
    public static DataProvider getDataProvider() {
        return dataProvider;
    }
    
    /**
     * Get service status
     */
    public static String getStatus() {
        StringBuilder status = new StringBuilder();
        status.append("ASTM Data Writer Service Status:\n");
        status.append("- Initialized: ").append(isInitialized).append("\n");
        
        if (config != null) {
            status.append("- Enabled: ").append(config.isEnabled()).append("\n");
        }
        
        if (writerConnector != null) {
            status.append(writerConnector.getStatus());
        }
        
        return status.toString();
    }

    
    /**
     * Log messages using the existing logging utility
     */
    /**
     * Configure API service with additional ResourceBundle properties
     */
    private static void configureApiService(ApiDataProvider dataProvider, DataWriterConfig config) {
        try {
            // Get the internal API service and configure timeouts and security
            // This demonstrates how ResourceBundle values can be used
            log("DataWriterService: Configuring API service with ResourceBundle values...");
            log("DataWriterService: - Connection Timeout: " + config.getConnectionTimeout() + " ms");
            log("DataWriterService: - Read Timeout: " + config.getReadTimeout() + " ms");
            log("DataWriterService: - Max Retry Attempts: " + config.getMaxRetryAttempts());
            log("DataWriterService: - Use SSL: " + config.isUseSSL());
            log("DataWriterService: - Order API Endpoint: " + config.getOrderApiEndpoint(config.bundle));
            log("DataWriterService: - Acknowledge API Endpoint: " + config.getAcknowledgeApiEndpoint(config.bundle));
            
            // Check if API key is configured
            if (!config.getApiKey().isEmpty()) {
                log("DataWriterService: - API Key: ***configured***");
            } else {
                log("DataWriterService: - API Key: not configured");
            }
            
            // Check authentication settings
            if (!config.getApiUsername().isEmpty()) {
                log("DataWriterService: - Basic Auth: enabled for user " + config.getApiUsername());
            } else {
                log("DataWriterService: - Basic Auth: not configured");
            }
            
            // Log ASTM protocol settings from ResourceBundle
            log("DataWriterService: ASTM Protocol Configuration from ResourceBundle:");
            log("DataWriterService: - Sender Name: " + config.getSenderName());
            log("DataWriterService: - Receiver Name: " + config.getReceiverName());
            log("DataWriterService: - Processing ID: " + config.getProcessingId());
            log("DataWriterService: - Message Encoding: " + config.getMessageEncoding());
            log("DataWriterService: - Max Frame Size: " + config.getMaxFrameSize() + " bytes");
            
            // Log ASTM separators from ResourceBundle
            log("DataWriterService: - Field Separator: '" + config.getFieldSeparator() + "'");
            log("DataWriterService: - Component Separator: '" + config.getComponentSeparator() + "'");
            log("DataWriterService: - Record Separator: '" + config.getRecordSeparator() + "'");
            
        } catch (Exception ex) {
            log("DataWriterService: Error configuring API service: " + ex.getMessage());
        }
    }
    
    /**
     * Get enhanced configuration summary including ResourceBundle values
     */
    
    
    private static void log(String message) {
        LabConnectUtil.log(message);
    }
} 