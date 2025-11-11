package com.writer;

import java.io.*;
import java.util.*;
import com.reader.LabConnectUtil;
import jssc.SerialPort;

/**
 * Main connector that orchestrates the complete RS232 data writing flow:
 * 1. Get data from DataProvider
 * 2. Convert to RS232 format
 * 3. Send to serial port
 * 4. Send acknowledgment back to DataProvider
 */
public class DataWriterConnector {
    
    private DataProvider dataProvider;
    private RS232DataWriter rs232DataWriter;
    private String portName;
    private int baudRate;
    private int dataBits;
    private int stopBits;
    private int parity;
    private long pollingIntervalMs;
    private boolean isRunning;
    
    public DataWriterConnector() {
        this.rs232DataWriter = new RS232DataWriter();
        this.pollingIntervalMs = 30000; // Default: poll every 30 seconds
        this.isRunning = false;
        
        // Default serial port settings (matching existing reader configuration)
        this.baudRate = 9600;
        this.dataBits = 8;
        this.stopBits = 1;
        this.parity = 0;
    }
    
    /**
     * Configure the connector with all necessary settings
     * @param portName Serial port name
     * @param dataProvider Data provider for lab orders
     */
    public void configure(String portName, DataProvider dataProvider) {
        this.portName = portName;
        this.dataProvider = dataProvider;
        log("DataWriterConnector: Configured with port: " + portName);
    }
    
    /**
     * Configure serial port settings
     */
    public void configureSerialPort(int baudRate, int dataBits, int stopBits, int parity) {
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
        log("DataWriterConnector: Serial port configured - " + baudRate + "," + dataBits + "," + stopBits + "," + parity);
    }
    
    /**
     * Set polling interval for checking new lab orders
     * @param intervalMs Interval in milliseconds
     */
    public void setPollingInterval(long intervalMs) {
        this.pollingIntervalMs = intervalMs;
        log("DataWriterConnector: Polling interval set to " + intervalMs + " ms");
    }
    
    /**
     * Start the connector - begins polling for lab orders and processing them
     */
    public void start(SerialPort serialPort) {
        if (isRunning) {
            log("DataWriterConnector: Already running");
            return;
        }
        
        log("DataWriterConnector: Starting...");

        
        isRunning = true;
        log("DataWriterConnector: Started successfully");
        
        // Start the main processing loop
        startProcessingLoop(serialPort);
    }
    
    /**
     * Stop the connector
     */
    public void stop(SerialPort serialPort) {
        if (!isRunning) {
            log("DataWriterConnector: Not running");
            return;
        }
        
        log("DataWriterConnector: Stopping...");
        isRunning = false;
        
        // Close serial port connection
        rs232DataWriter.closeConnection(serialPort);
        
        log("DataWriterConnector: Stopped");
    }
    
    /**
     * Process lab orders once (for manual triggering)
     * @return true if processing completed successfully
     */
    public boolean processLabOrders(SerialPort serialPort) {
        log("DataWriterConnector: Processing lab orders...");
        
        try {
            // Check if data provider is configured
            if (dataProvider == null) {
                log("DataWriterConnector: No data provider configured");
                return false;
            }
            
            // Step 1: Get lab order data from provider
            List<LabOrderData> labOrderDataList = dataProvider.getLabOrderData(serialPort);
            
            if (labOrderDataList == null) {
                log("DataWriterConnector: Failed to get lab order data from provider");
                return false;
            }
            
            if (labOrderDataList.isEmpty()) {
                log("DataWriterConnector: No lab orders to process");
                return true; // This is considered successful
            }
            
            log("DataWriterConnector: Retrieved " + labOrderDataList.size() + " lab orders");
            
            // Step 2: Send RS232 message directly (pipe-delimited format)
            // Generate a message ID for tracking
            String messageId = generateMessageId();
            log("DataWriterConnector: Prepared RS232 message with ID: " + messageId);
            
            // Step 3: Send RS232 message to serial port
            boolean transmissionSuccess = rs232DataWriter.sendPipeDelimitedMessage(labOrderDataList, serialPort);
            
            // Step 4: Send acknowledgment to data provider
            if (transmissionSuccess) {
                log("DataWriterConnector: RS232 transmission successful");
                dataProvider.acknowledgeProcessing(messageId, true, 
                    "RS232 transmission completed successfully with " + labOrderDataList.size() + " records");
                
                // Step 5: Send order acknowledgment via API (if using ApiDataProvider)
                if (dataProvider instanceof ApiDataProvider) {
                    log("DataWriterConnector: Sending order acknowledgment via API...");
                    ApiDataProvider apiProvider = (ApiDataProvider) dataProvider;
                    boolean ackResult = apiProvider.acknowledgeOrders(labOrderDataList, true);
                    
                    if (ackResult) {
                        log("DataWriterConnector: Order acknowledgment sent successfully");
                    } else {
                        log("DataWriterConnector: Failed to send order acknowledgment (processing will continue)");
                    }
                }
                
                return true;
            } else {
                log("DataWriterConnector: RS232 transmission failed");
                dataProvider.acknowledgeProcessing(messageId, false, "Serial port transmission failed");
                
                // Send failure acknowledgment via API (if using ApiDataProvider)
                if (dataProvider instanceof ApiDataProvider) {
                    log("DataWriterConnector: Sending failure acknowledgment via API...");
                    ApiDataProvider apiProvider = (ApiDataProvider) dataProvider;
                    boolean ackResult = apiProvider.acknowledgeOrders(labOrderDataList, false);
                    
                    if (ackResult) {
                        log("DataWriterConnector: Failure acknowledgment sent successfully");
                    } else {
                        log("DataWriterConnector: Failed to send failure acknowledgment");
                    }
                }
                
                return false;
            }
            
        } catch (Exception ex) {
            log("DataWriterConnector: Error during processing: " + ex.getMessage());
            ex.printStackTrace();
            sendFailureAcknowledgment("UNKNOWN", "Processing error: " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Start the continuous processing loop
     */
    private void startProcessingLoop(SerialPort serialPort) {
        Thread processingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                log("DataWriterConnector: Processing loop started");
                
                while (isRunning) {
                    try {
                        // Only process if data is available
                        if (dataProvider != null && dataProvider.hasDataAvailable()) {
                            processLabOrders(serialPort);
                        }
                        
                        // Wait for next polling interval
                        Thread.sleep(pollingIntervalMs);
                        
                    } catch (InterruptedException ex) {
                        log("DataWriterConnector: Processing loop interrupted");
                        break;
                    } catch (Exception ex) {
                        log("DataWriterConnector: Error in processing loop: " + ex.getMessage());
                        // Continue processing even if there's an error
                        try {
                            Thread.sleep(5000); // Wait 5 seconds before retrying
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }
                
                log("DataWriterConnector: Processing loop stopped");
            }
        });
        
        processingThread.setDaemon(true);
        processingThread.start();
    }
    
    /**
     * Generate a unique message ID
     */
    private String generateMessageId() {
        return "ASTM_" + System.currentTimeMillis();
    }
    
    /**
     * Send failure acknowledgment with unknown message ID
     */
    private void sendFailureAcknowledgment(String messageId, String errorMessage) {
        try {
            if (dataProvider != null) {
                dataProvider.acknowledgeProcessing(messageId, false, errorMessage);
            }
        } catch (Exception ex) {
            log("DataWriterConnector: Failed to send failure acknowledgment: " + ex.getMessage());
        }
    }
    
    /**
     * Test the connection and configuration
     * @return true if test successful
     */

    
    /**
     * Get current status
     */
    public String getStatus() {
        StringBuilder status = new StringBuilder();
        status.append("DataWriterConnector Status:\n");
        status.append("- Running: ").append(isRunning).append("\n");
        status.append("- Port: ").append(portName).append("\n");
        status.append("- Serial Connected: ").append(rs232DataWriter.isConnected()).append("\n");
        status.append("- Polling Interval: ").append(pollingIntervalMs).append(" ms\n");
        return status.toString();
    }
    
    /**
     * Log messages using the existing logging utility
     */
    private void log(String message) {
        LabConnectUtil.log(message);
    }
} 