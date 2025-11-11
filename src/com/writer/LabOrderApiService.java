package com.writer;

import java.io.*;
import java.util.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import com.reader.LabConnectUtil;

/**
 * REST API service for fetching lab orders from the specified endpoint
 */
public class LabOrderApiService {
    
    private String baseUrl;
    private String apiKey;

    private static Integer WS_READ_TIMEOUT = 60000;
    private static Integer WS_CONNECT_TIMEOUT = 30000;
    private ObjectMapper objectMapper;
    
    public LabOrderApiService() {
        this.objectMapper = new ObjectMapper();
        this.apiKey = "MEDNET_LAB_INTERFACE";
    }
    
    /**
     * Configure the service with API endpoint
     * @param baseUrl Base URL for the API
     */
    public void configure(String baseUrl) {
        this.baseUrl = baseUrl;
        log("LabOrderApiService: Configured with base URL: " + baseUrl);
    }

    /**
     * Fetch lab order requests from the API
     * @param machineCode Machine code (e.g., "B121")
     * @param companyId Company ID (e.g., "1")
     * @return List of lab order data, or null if error
     */
    public List<LabOrderData> fetchLabOrderRequests(String machineCode, String companyId) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            log("LabOrderApiService: Base URL not configured");
            return null;
        }
        
        try {
            // Construct full URL
            String fullUrl = baseUrl;
            if (!baseUrl.endsWith("/")) {
                fullUrl += "/";
            }
            fullUrl += "mediInterfaceWS/getLabOrderRequests";
            
            log("LabOrderApiService: Fetching lab orders from: " + fullUrl);
            
            // Create request payload
            LabOrderRequest request = new LabOrderRequest();
            request.setMachineCode(Arrays.asList(machineCode));
            request.setCompanyID(companyId);
            
            String requestJson = objectMapper.writeValueAsString(request);
            log("LabOrderApiService: Request payload: " + requestJson);
            
            // Create web client
            WebClient client = WebClient.create(fullUrl);
            WebClient.getConfig(client).getHttpConduit().getClient().setReceiveTimeout(WS_READ_TIMEOUT);
            WebClient.getConfig(client).getHttpConduit().getClient().setConnectionTimeout(WS_CONNECT_TIMEOUT);
            client.type(MediaType.APPLICATION_JSON);
            client.accept(MediaType.APPLICATION_JSON);
            client.header("API_KEY", apiKey);
            
            // Make API call
            String jsonResponse = client.post(requestJson, String.class);
            log("LabOrderApiService: Received response: " + jsonResponse);
            
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                log("LabOrderApiService: Empty response from API");
                return new ArrayList<LabOrderData>();
            }
            
            // Parse JSON response
            List<LabOrderData> labOrderDataList = parseLabOrderResponse(jsonResponse);
            log("LabOrderApiService: Successfully parsed " + (labOrderDataList != null ? labOrderDataList.size() : 0) + " lab orders");
            
            return labOrderDataList;
            
        } catch (Exception ex) {
            log("LabOrderApiService: Error fetching lab orders: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * Parse the API response and convert to LabOrderData objects
     */
    private List<LabOrderData> parseLabOrderResponse(String jsonResponse) {
        try {
            List<LabOrderData> labOrderDataList = new ArrayList<LabOrderData>();
            
            // Parse the response JSON
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, 
                new TypeReference<Map<String, Object>>() {});
            
            // Check if response is successful
            Boolean success = (Boolean) responseMap.get("success");
            if (success == null) {
                log("LabOrderApiService: Invalid response - missing 'success' field");
                return null;
            }
            
            if (!success) {
                // Handle error responses (Case 3 & 4)
                handleErrorResponse(responseMap);
                return labOrderDataList; // Return empty list for error responses
            }
            
            // Handle successful responses
            Object dataObject = responseMap.get("data");
            if (dataObject == null) {
                log("LabOrderApiService: No data field in response");
                return labOrderDataList;
            }
            
            // Check if data is an array (Case 1 & 2) or error object (Case 3 & 4)
            if (dataObject instanceof List) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataObject;
                
                if (dataList.isEmpty()) {
                    // Case 2: Empty data array
                    log("LabOrderApiService: No lab orders available (empty data array)");
                    return labOrderDataList;
                }
                
                // Case 1: Data array with lab orders
                log("LabOrderApiService: Processing " + dataList.size() + " lab orders from API");
                
                // Convert each item to LabOrderData
                for (Map<String, Object> dataItem : dataList) {
                    LabOrderData labOrderData = convertApiDataToLabOrder(dataItem);
                    if (labOrderData != null) {
                        labOrderDataList.add(labOrderData);
                    }
                }
                
                log("LabOrderApiService: Successfully converted " + labOrderDataList.size() + " lab orders");
                
            } else {
                // Data is not an array - this might be an error object
                log("LabOrderApiService: Unexpected data format - expected array but got: " + dataObject.getClass().getSimpleName());
                return labOrderDataList;
            }
            
            return labOrderDataList;
            
        } catch (Exception ex) {
            log("LabOrderApiService: Error parsing response: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * Handle error responses from the API
     */
    private void handleErrorResponse(Map<String, Object> responseMap) {
        try {
            Object dataObject = responseMap.get("data");
            
            if (dataObject instanceof Map) {
                Map<String, Object> errorData = (Map<String, Object>) dataObject;
                
                // Extract error details
                Object errorCode = errorData.get("error_code");
                Object errorMessage = errorData.get("error_message");
                
                String errorCodeStr = errorCode != null ? errorCode.toString() : "UNKNOWN";
                String errorMessageStr = errorMessage != null ? errorMessage.toString() : "Unknown error";
                
                log("LabOrderApiService: API Error - Code: " + errorCodeStr + ", Message: " + errorMessageStr);
                
                // Handle specific error codes
                if ("204".equals(errorCodeStr)) {
                    if (errorMessageStr.contains("not found")) {
                        log("LabOrderApiService: Analyzer not found - check machine code configuration");
                    } else if (errorMessageStr.contains("not matched")) {
                        log("LabOrderApiService: Analyzer name mismatch - verify machine code mapping");
                    }
                }
                
            } else {
                log("LabOrderApiService: API returned error but data format is unexpected");
            }
            
        } catch (Exception ex) {
            log("LabOrderApiService: Error processing error response: " + ex.getMessage());
        }
    }
    
    /**
     * Convert API response data item to LabOrderData
     */
    private LabOrderData convertApiDataToLabOrder(Map<String, Object> dataItem) {
        try {
            LabOrderData labOrder = new LabOrderData();
            
            // Map fields from API response to LabOrderData
            labOrder.setPatientId(getStringValue(dataItem, "mrn"));
            labOrder.setPatientName(getStringValue(dataItem, "patientName"));
            labOrder.setPatientSex(getStringValue(dataItem, "gender"));
            labOrder.setPatientBirthDate(formatDate(getStringValue(dataItem, "patientDOB")));
            labOrder.setOrderId(getStringValue(dataItem, "sampleID"));
            labOrder.setTestType(getStringValue(dataItem, "investigationCode"));
            labOrder.setTestDescription(getStringValue(dataItem, "investigationName"));
            labOrder.setOrderDateTime(formatDateTime(getStringValue(dataItem, "acceptanceDate")));
            labOrder.setSpecimenType("SERUM"); // Default for lab tests
            labOrder.setPriority("ROUTINE"); // Default priority
            
            // Set additional fields for acknowledgment
            labOrder.setSampleId(getStringValue(dataItem, "sampleID"));
            labOrder.setInvestigationCode(getStringValue(dataItem, "investigationCode"));
            labOrder.setMrn(getStringValue(dataItem, "mrn"));
            labOrder.setAcceptanceDate(getStringValue(dataItem, "acceptanceDate"));
            
            return labOrder;
            
        } catch (Exception ex) {
            log("LabOrderApiService: Error converting API data: " + ex.getMessage());
            return null;
        }
    }
    
    /**
     * Get string value from map
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
    
    /**
     * Format date from API format to YYYYMMDD
     */
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        
        try {
            // API format appears to be "2016-02-05"
            String digitsOnly = dateStr.replaceAll("[^0-9]", "");
            if (digitsOnly.length() >= 8) {
                return digitsOnly.substring(0, 8);
            }
            return digitsOnly;
        } catch (Exception ex) {
            log("LabOrderApiService: Error formatting date: " + dateStr);
            return "";
        }
    }
    
    /**
     * Format datetime from API format to YYYYMMDDHHMM
     */
    private String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return "";
        }
        
        try {
            // API format appears to be "2024-01-16 15:26:00.0"
            String digitsOnly = dateTimeStr.replaceAll("[^0-9]", "");
            if (digitsOnly.length() >= 12) {
                return digitsOnly.substring(0, 12);
            } else if (digitsOnly.length() >= 8) {
                return digitsOnly + "1200"; // Add default time
            }
            return digitsOnly;
        } catch (Exception ex) {
            log("LabOrderApiService: Error formatting datetime: " + dateTimeStr);
            return "";
        }
    }
    
    /**
     * Request class for API call
     */
    public static class LabOrderRequest {
        private List<String> machineCode;
        private String companyID;
        
        public List<String> getMachineCode() { return machineCode; }
        public void setMachineCode(List<String> machineCode) { this.machineCode = machineCode; }
        
        public String getCompanyID() { return companyID; }
        public void setCompanyID(String companyID) { this.companyID = companyID; }
    }
    
    /**
     * Send acknowledgment for processed lab orders
     */
    public boolean sendOrderAcknowledgment(List<LabOrderData> processedOrders, String machineCode, String companyId, boolean success) {
        try {
            String fullUrl = baseUrl + "/mediInterfaceWS/updateOrderAcknowledgement";
            log("LabOrderApiService: Sending acknowledgment to: " + fullUrl);
            
            // Build acknowledgment request
            AcknowledgmentRequest ackRequest = new AcknowledgmentRequest();
            List<AcknowledgmentItem> ackList = new ArrayList<AcknowledgmentItem>();
            
            for (LabOrderData order : processedOrders) {
                AcknowledgmentItem ackItem = new AcknowledgmentItem();
                ackItem.setSampleID(order.getSampleId());
                ackItem.setInvID(order.getInvestigationCode());
                ackItem.setMRN(order.getMrn());
                ackItem.setAcceptanceDate(formatDateForAck(order.getAcceptanceDate()));
                ackItem.setMachineCode(machineCode);
                ackItem.setCompanyID(companyId);
                ackItem.setStatus(success ? "success" : "fail");
                ackList.add(ackItem);
            }
            
            ackRequest.setRequestAckList(ackList);
            
            // Convert to JSON
            String jsonPayload = objectMapper.writeValueAsString(ackRequest);
            log("LabOrderApiService: Acknowledgment payload: " + jsonPayload);
            
            // Create WebClient and set up request
            WebClient client = WebClient.create(fullUrl);
            WebClient.getConfig(client).getHttpConduit().getClient().setReceiveTimeout(WS_READ_TIMEOUT);
            WebClient.getConfig(client).getHttpConduit().getClient().setConnectionTimeout(WS_CONNECT_TIMEOUT);
            client.type(MediaType.APPLICATION_JSON);
            client.accept(MediaType.APPLICATION_JSON);
            client.header("API_KEY", apiKey);

            // Send POST request
            String responseBody = client.post(jsonPayload, String.class);
            
            if (responseBody != null) {
                log("LabOrderApiService: Acknowledgment sent successfully: " + responseBody);
                return true;
            } else {
                log("LabOrderApiService: Acknowledgment failed - no response received");
                return false;
            }
            
        } catch (Exception ex) {
            log("LabOrderApiService: Error sending acknowledgment: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * Format date for acknowledgment (DD-MM-YYYY format)
     */
    private String formatDateForAck(String acceptanceDate) {
        try {
            if (acceptanceDate == null || acceptanceDate.isEmpty()) {
                return "";
            }
            
            // Handle different input formats
            if (acceptanceDate.contains("-") && acceptanceDate.length() >= 10) {
                // Format: 2024-01-16 15:26:00.0 or 2016-02-05
                String datePart = acceptanceDate.substring(0, 10);
                String[] parts = datePart.split("-");
                if (parts.length == 3) {
                    // Convert YYYY-MM-DD to DD-MM-YYYY
                    return parts[2] + "-" + parts[1] + "-" + parts[0];
                }
            }
            
            return acceptanceDate; // Return as-is if format not recognized
            
        } catch (Exception ex) {
            log("LabOrderApiService: Error formatting date for acknowledgment: " + ex.getMessage());
            return acceptanceDate;
        }
    }
    
    /**
     * Acknowledgment request structure
     */
    public static class AcknowledgmentRequest {
        private List<AcknowledgmentItem> requestAckList;
        
        public List<AcknowledgmentItem> getRequestAckList() {
            return requestAckList;
        }
        
        public void setRequestAckList(List<AcknowledgmentItem> requestAckList) {
            this.requestAckList = requestAckList;
        }
    }
    
    /**
     * Individual acknowledgment item
     */
    public static class AcknowledgmentItem {
        private String sampleID;
        @JsonProperty("InvID")
        private String InvID;
        @JsonProperty("MRN")
        private String MRN;
        private String acceptanceDate;
        private String machineCode;
        private String companyID;
        private String status;
        
        // Getters and setters
        public String getSampleID() { return sampleID; }
        public void setSampleID(String sampleID) { this.sampleID = sampleID; }
        
        public String getInvID() { return InvID; }
        public void setInvID(String invID) { InvID = invID; }
        
        public String getMRN() { return MRN; }
        public void setMRN(String MRN) { this.MRN = MRN; }
        
        public String getAcceptanceDate() { return acceptanceDate; }
        public void setAcceptanceDate(String acceptanceDate) { this.acceptanceDate = acceptanceDate; }
        
        public String getMachineCode() { return machineCode; }
        public void setMachineCode(String machineCode) { this.machineCode = machineCode; }
        
        public String getCompanyID() { return companyID; }
        public void setCompanyID(String companyID) { this.companyID = companyID; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    /**
     * Log messages using the existing logging utility
     */
    private void log(String message) {
        LabConnectUtil.log(message);
    }
} 