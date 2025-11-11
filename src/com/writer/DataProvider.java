package com.writer;

import jssc.SerialPort;

import java.util.List;

/**
 * Interface for external systems to provide lab order data to the ASTM writer
 * This allows integration with various data sources without REST API dependencies
 */
public interface DataProvider {
    
    /**
     * Fetch available lab order data
     * @return List of lab order data, or empty list if no data available
     */
    List<LabOrderData> getLabOrderData(SerialPort serialPort);
    
    /**
     * Acknowledge processing of lab orders
     * @param messageId Unique identifier for the message
     * @param success Whether processing was successful
     * @param details Additional details about the processing
     */
    void acknowledgeProcessing(String messageId, boolean success, String details);
    
    /**
     * Check if data provider has new data available
     * @return true if new data is available
     */
    boolean hasDataAvailable();
} 