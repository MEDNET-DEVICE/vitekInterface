package com.writer;

import java.util.*;
import com.reader.LabConnectUtil;
import jssc.SerialPort;

/**
 * Data provider that fetches lab orders from REST API
 */
public class ApiDataProvider implements DataProvider {
    
    private LabOrderApiService apiService;
    private String machineCode;
    private String companyId;
    private long lastFetchTime;
    private long fetchIntervalMs;
    
    public ApiDataProvider() {
        this.apiService = new LabOrderApiService();
        this.machineCode = "B121"; // Default machine code
        this.companyId = "1"; // Default company ID
        this.lastFetchTime = 0;
        this.fetchIntervalMs = 30000; // Default: fetch every 30 seconds
    }
    
    /**
     * Configure the API data provider
     * @param baseUrl Base URL for the API
     * @param machineCode Machine code for requests
     * @param companyId Company ID for requests
     */
    public void configure(String baseUrl, String machineCode, String companyId) {
        this.apiService.configure(baseUrl);
        this.machineCode = machineCode;
        this.companyId = companyId;
        log("ApiDataProvider: Configured with machine code: " + machineCode + ", company ID: " + companyId);
    }
    
    /**
     * Set fetch interval
     * @param intervalMs Interval in milliseconds
     */
    public void setFetchInterval(long intervalMs) {
        this.fetchIntervalMs = intervalMs;
    }
    
    @Override
    public List<LabOrderData> getLabOrderData(SerialPort serialPort) {
        try {
            log("ApiDataProvider: Fetching lab orders from API...");
            List<LabOrderData> labOrders = apiService.fetchLabOrderRequests(machineCode, companyId);
            
            // Update last fetch time regardless of result
            lastFetchTime = System.currentTimeMillis();
            
            if (labOrders != null) {
                if (labOrders.isEmpty()) {
                    log("ApiDataProvider: No lab orders available at this time");
                } else {
                    log("ApiDataProvider: Successfully fetched " + labOrders.size() + " lab orders");
                }
                return labOrders;
            } else {
                log("ApiDataProvider: Failed to fetch lab orders - API communication error");
                return new ArrayList<LabOrderData>();
            }
            
        } catch (Exception ex) {
            log("ApiDataProvider: Exception while fetching lab orders: " + ex.getMessage());
            lastFetchTime = System.currentTimeMillis(); // Still update fetch time to avoid rapid retries
            return new ArrayList<LabOrderData>();
        }
    }
    
    @Override
    public void acknowledgeProcessing(String messageId, boolean success, String details) {
        log("ApiDataProvider: Acknowledgment - ID: " + messageId + 
            ", Success: " + success + ", Details: " + details);
        
        // Individual acknowledgments are handled via acknowledgeOrders method
    }
    
    /**
     * Send acknowledgment for a list of processed lab orders
     */
    public boolean acknowledgeOrders(List<LabOrderData> processedOrders, boolean success) {
        try {
            if (processedOrders == null || processedOrders.isEmpty()) {
                log("ApiDataProvider: No orders to acknowledge");
                return true;
            }
            
            log("ApiDataProvider: Sending acknowledgment for " + processedOrders.size() + " orders, Success: " + success);
            
            boolean result = apiService.sendOrderAcknowledgment(processedOrders, machineCode, companyId, success);
            
            if (result) {
                log("ApiDataProvider: Acknowledgment sent successfully for " + processedOrders.size() + " orders");
            } else {
                log("ApiDataProvider: Failed to send acknowledgment for " + processedOrders.size() + " orders");
            }
            
            return result;
            
        } catch (Exception ex) {
            log("ApiDataProvider: Error sending acknowledgment: " + ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean hasDataAvailable() {
        // Check if enough time has passed since last fetch
        long currentTime = System.currentTimeMillis();
        boolean shouldFetch = (currentTime - lastFetchTime) >= fetchIntervalMs;
        
        if (shouldFetch) {
            log("ApiDataProvider: Time to fetch new data");
        }
        
        return shouldFetch;
    }
    
    /**
     * Test the API connection
     * @return true if API is accessible
     */
    public boolean testConnection() {
        try {
            log("ApiDataProvider: Testing API connection...");
            List<LabOrderData> testData = apiService.fetchLabOrderRequests(machineCode, companyId);
            
            if (testData != null) {
                log("ApiDataProvider: Connection test successful - API responded correctly");
                if (testData.isEmpty()) {
                    log("ApiDataProvider: No lab orders available during test (this is normal)");
                } else {
                    log("ApiDataProvider: Test returned " + testData.size() + " lab orders");
                }
                return true;
            } else {
                log("ApiDataProvider: Connection test failed - API communication error");
                return false;
            }
        } catch (Exception ex) {
            log("ApiDataProvider: Connection test exception: " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Get current configuration summary
     */
    public String getConfigurationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("API Data Provider Configuration:\n");
        summary.append("- Machine Code: ").append(machineCode).append("\n");
        summary.append("- Company ID: ").append(companyId).append("\n");
        summary.append("- Fetch Interval: ").append(fetchIntervalMs).append(" ms\n");
        summary.append("- Last Fetch: ").append(lastFetchTime > 0 ? new Date(lastFetchTime) : "Never").append("\n");
        return summary.toString();
    }
    
    private void log(String message) {
        LabConnectUtil.log(message);
    }
} 