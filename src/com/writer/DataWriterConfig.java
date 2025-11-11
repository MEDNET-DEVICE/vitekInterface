package com.writer;

import java.io.*;
import java.util.*;

/**
 * Configuration class for ASTM Data Writer
 * Loads configuration from ResourceBundle following the same pattern as DataReader
 */
public class DataWriterConfig {
    
    public ResourceBundle bundle;
    private String configFilePath;

    // Default configuration values
    private static final int DEFAULT_BAUD_RATE = 9600;
    private static final int DEFAULT_DATA_BITS = 8;
    private static final int DEFAULT_STOP_BITS = 1;
    private static final int DEFAULT_PARITY = 0;
    private static final long DEFAULT_POLLING_INTERVAL = 30000; // 30 seconds

    public DataWriterConfig() {
        // Initialize with null, will load in loadConfiguration()
    }

    /**
     * Load configuration from ResourceBundle
     * Follows the same pattern as DataReader and LabConnectorThread
     */
    public boolean loadConfiguration() {
        try {
            String rootDrive = "D://";
            if (System.getProperty("rootDrive") != null) {
                rootDrive = System.getProperty("rootDrive");
            }

            String propertyFileSuffix = "VITEK2COMPACT";
            if (System.getProperty("propertyFileSuffix") != null) {
                propertyFileSuffix = System.getProperty("propertyFileSuffix");
            }

            configFilePath = rootDrive + File.separator + "mednet" + File.separator +
                           "windowsService" + propertyFileSuffix + ".properties";

            File configFile = new File(configFilePath);
            if (configFile.exists()) {
                bundle = new PropertyResourceBundle(new FileInputStream(configFile));
                log("DataWriterConfig: Loaded configuration from " + configFilePath);
                return true;
            } else {
                log("DataWriterConfig: Configuration file not found at " + configFilePath + ", using defaults");
                return false;
            }

        } catch (Exception ex) {
            log("DataWriterConfig: Error loading configuration: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Create a default configuration file for reference
     */

    /**
     * Load default properties using ResourceBundle pattern
     */


    public String getComPort(ResourceBundle bundle) {
        return getStringProperty("comPort", bundle.getString("comPort"));
    }

    public int getBaudRate() {
        return getIntProperty("baudRate", DEFAULT_BAUD_RATE);
    }

    public int getDataBits() {
        return getIntProperty("dataBits", DEFAULT_DATA_BITS);
    }

    public int getStopBits() {
        return getIntProperty("stopBits", DEFAULT_STOP_BITS);
    }

    public int getParity() {
        return getIntProperty("parity", DEFAULT_PARITY);
    }

    public long getPollingInterval() {
        return getLongProperty("pollingInterval", DEFAULT_POLLING_INTERVAL);
    }

    public boolean isEnabled() {
        return getBooleanProperty("enabled", true);
    }

    public boolean isDataWriterEnabled() {
        return getBooleanProperty("dataWriterEnabled", true);
    }

    public boolean isDataWriterEnabled(ResourceBundle bundle) {
        return getBooleanProperty("dataWriterEnabled", Boolean.parseBoolean(bundle.getString("dataWriterEnabled")));
    }

    public String getApiBaseUrl(ResourceBundle bundle) {
        return getStringProperty("apiBaseUrl", bundle.getString("apiBaseUrl"));
    }

    public String getMachineCode(ResourceBundle bundle) {
        return getStringProperty("machineCode", bundle.getString("machineCode"));
    }

    public String getCompanyId(ResourceBundle bundle) {
        return getStringProperty("companyId", bundle.getString("companyId"));
    }

    // Additional ASTM Protocol Configuration getters
    public String getSenderName() {
        return getStringProperty("senderName", "DummyLIS");
    }

    public String getReceiverName() {
        return getStringProperty("receiverName", "VITEK2");
    }

    public String getProcessingId() {
        return getStringProperty("processingId", "P");
    }

    public String getVersionNumber() {
        return getStringProperty("versionNumber", "1");
    }

    public String getLogLevel() {
        return getStringProperty("logLevel", "INFO");
    }

    // Additional configuration getters for extra ResourceBundle properties
    public int getConnectionTimeout() {
        return getIntProperty("connectionTimeout", 5000);
    }

    public int getReadTimeout() {
        return getIntProperty("readTimeout", 10000);
    }

    public int getMaxRetryAttempts() {
        return getIntProperty("maxRetryAttempts", 3);
    }

    public int getRetryDelay() {
        return getIntProperty("retryDelay", 1000);
    }

    public int getMaxFrameSize() {
        return getIntProperty("maxFrameSize", 1024);
    }

    public String getMessageEncoding() {
        return getStringProperty("messageEncoding", "ASCII");
    }

    // ASTM Control Characters
    public String getStxChar() {
        return getStringProperty("stxChar", "0x02");
    }

    public String getEtxChar() {
        return getStringProperty("etxChar", "0x03");
    }

    public String getEnqChar() {
        return getStringProperty("enqChar", "0x05");
    }

    public String getAckChar() {
        return getStringProperty("ackChar", "0x06");
    }

    public String getEotChar() {
        return getStringProperty("eotChar", "0x04");
    }

    public String getNakChar() {
        return getStringProperty("nakChar", "0x15");
    }

    // ASTM Separators
    public String getRecordSeparator() {
        return getStringProperty("recordSeparator", "|");
    }

    public String getFieldSeparator() {
        return getStringProperty("fieldSeparator", "^");
    }

    public String getComponentSeparator() {
        return getStringProperty("componentSeparator", "&");
    }

    public String getRepeatSeparator() {
        return getStringProperty("repeatSeparator", "~");
    }

    public String getEscapeChar() {
        return getStringProperty("escapeChar", "\\");
    }

    // API Endpoints
    public String getOrderApiEndpoint() {
        return getStringProperty("orderApiEndpoint", "/api/lab/orders");
    }

    public String getOrderApiEndpoint(ResourceBundle bundle) {
        return getStringProperty("orderApiEndpoint", bundle.getString("orderApiEndpoint"));
    }

    public String getAcknowledgeApiEndpoint() {
        return getStringProperty("acknowledgeApiEndpoint", "/api/lab/acknowledge");
    }

    public String getAcknowledgeApiEndpoint(ResourceBundle bundle) {
        return getStringProperty("acknowledgeApiEndpoint", bundle.getString("acknowledgeApiEndpoint"));
    }

    public String getResultEndpoint() {
        return getStringProperty("resultEndpoint", "/api/lab/results");
    }

    public String getStatusEndpoint() {
        return getStringProperty("statusEndpoint", "/api/lab/status");
    }

    // Security Configuration
    public String getApiKey() {
        return getStringProperty("apiKey", "");
    }

    public String getApiUsername() {
        return getStringProperty("apiUsername", "");
    }

    public String getApiPassword() {
        return getStringProperty("apiPassword", "");
    }

    // SSL/TLS Configuration
    public boolean isUseSSL() {
        return getBooleanProperty("useSSL", false);
    }

    public boolean isVerifySSLCertificates() {
        return getBooleanProperty("verifySSLCertificates", true);
    }

    public String getSSLTruststorePath() {
        return getStringProperty("sslTruststorePath", "");
    }

    public String getSSLTruststorePassword() {
        return getStringProperty("sslTruststorePassword", "");
    }

    // Utility methods for type conversion with ResourceBundle support
    private String getStringProperty(String key, String defaultValue) {
        try {
            if (bundle == null) return defaultValue;
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            return defaultValue;
        }
    }

    private int getIntProperty(String key, int defaultValue) {
        try {
            String value = getStringProperty(key, null);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException ex) {
            log("DataWriterConfig: Invalid integer value for " + key + ", using default");
            return defaultValue;
        }
    }

    private long getLongProperty(String key, long defaultValue) {
        try {
            String value = getStringProperty(key, null);
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (NumberFormatException ex) {
            log("DataWriterConfig: Invalid long value for " + key + ", using default");
            return defaultValue;
        }
    }

    private boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getStringProperty(key, null);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Get all configuration as a formatted string
     */
    public String getConfigurationSummary() {
        try {
            StringBuilder summary = new StringBuilder();
            summary.append("ASTM Data Writer Configuration:\n");

            if (bundle != null) {
                // Use ResourceBundle values if available
                summary.append("- Serial Port: ").append(getComPort(bundle)).append("\n");
                summary.append("- API Base URL: ").append(getApiBaseUrl(bundle)).append("\n");
                summary.append("- Machine Code: ").append(getMachineCode(bundle)).append("\n");
                summary.append("- Company ID: ").append(getCompanyId(bundle)).append("\n");
                summary.append("- Data Writer Enabled: ").append(isDataWriterEnabled(bundle)).append("\n");
                summary.append("- Order API Endpoint: ").append(getOrderApiEndpoint(bundle)).append("\n");
                summary.append("- Acknowledge API Endpoint: ").append(getAcknowledgeApiEndpoint(bundle)).append("\n");
            } else {
                // Use default values if ResourceBundle not available
                summary.append("- Serial Port: ").append(getComPort(bundle)).append("\n");
                summary.append("- API Base URL: ").append(getApiBaseUrl(bundle)).append("\n");
                summary.append("- Machine Code: ").append(getMachineCode(bundle)).append("\n");
                summary.append("- Company ID: ").append(getCompanyId(bundle)).append("\n");
                summary.append("- Data Writer Enabled: ").append(isDataWriterEnabled()).append("\n");
                summary.append("- Order API Endpoint: ").append(getOrderApiEndpoint()).append("\n");
                summary.append("- Acknowledge API Endpoint: ").append(getAcknowledgeApiEndpoint()).append("\n");
            }
            
            summary.append("- Baud Rate: ").append(getBaudRate()).append("\n");
            summary.append("- Data Bits: ").append(getDataBits()).append("\n");
            summary.append("- Stop Bits: ").append(getStopBits()).append("\n");
            summary.append("- Parity: ").append(getParity()).append("\n");
            summary.append("- Polling Interval: ").append(getPollingInterval()).append(" ms\n");
            summary.append("- Service Enabled: ").append(isEnabled()).append("\n");
            summary.append("- Sender Name: ").append(getSenderName()).append("\n");
            summary.append("- Receiver Name: ").append(getReceiverName()).append("\n");
            summary.append("- Processing ID: ").append(getProcessingId()).append("\n");
            summary.append("- Version Number: ").append(getVersionNumber()).append("\n");
            summary.append("- Log Level: ").append(getLogLevel()).append("\n");
            summary.append("- Connection Timeout: ").append(getConnectionTimeout()).append(" ms\n");
            summary.append("- Read Timeout: ").append(getReadTimeout()).append(" ms\n");
            summary.append("- Max Retry Attempts: ").append(getMaxRetryAttempts()).append("\n");
            summary.append("- Max Frame Size: ").append(getMaxFrameSize()).append(" bytes\n");
            summary.append("- Message Encoding: ").append(getMessageEncoding()).append("\n");
            summary.append("- Use SSL: ").append(isUseSSL()).append("\n");
            summary.append("- Result Endpoint: ").append(getResultEndpoint()).append("\n");
            return summary.toString();
        } catch (Exception ex) {
            return "Configuration summary error: " + ex.getMessage();
        }
    }
    
    /**
     * Validate configuration
     */
    public boolean validateConfiguration() {
        boolean valid = true;
        
        String comPort = (bundle != null) ? getComPort(bundle) : null;
        if (comPort == null || comPort.trim().isEmpty()) {
            log("DataWriterConfig: Invalid COM port");
            valid = false;
        }
        
        if (getBaudRate() <= 0) {
            log("DataWriterConfig: Invalid baud rate");
            valid = false;
        }
        
        if (getPollingInterval() < 1000) {
            log("DataWriterConfig: Polling interval too small (minimum 1 second)");
            valid = false;
        }
        
        String apiBaseUrl = (bundle != null) ? getApiBaseUrl(bundle) : null;
        if (apiBaseUrl == null || apiBaseUrl.trim().isEmpty()) {
            log("DataWriterConfig: Invalid API base URL");
            valid = false;
        }
        
        String machineCode = (bundle != null) ? getMachineCode(bundle) : null;
        if (machineCode == null || machineCode.trim().isEmpty()) {
            log("DataWriterConfig: Invalid machine code");
            valid = false;
        }
        
        String companyId = (bundle != null) ? getCompanyId(bundle) : null;
        if (companyId == null || companyId.trim().isEmpty()) {
            log("DataWriterConfig: Invalid company ID");
            valid = false;
        }
        
        return valid;
    }
    
    /**
     * Log messages (using simple System.out.println since LabConnectUtil might not be available during config loading)
     */
    private void log(String message) {
        System.out.println(message);
        // Also try to log using LabConnectUtil if available
        try {
            com.reader.LabConnectUtil.log(message);
        } catch (Exception ex) {
            // Ignore if LabConnectUtil is not available
        }
    }
} 