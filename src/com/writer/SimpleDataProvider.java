package com.writer;

import java.util.*;
import com.reader.LabConnectUtil;
import jssc.SerialPort;

/**
 * Simple in-memory data provider for testing and demonstration
 * External systems can implement DataProvider interface for their specific needs
 */
public class SimpleDataProvider implements DataProvider {
    
    private Queue<List<LabOrderData>> dataQueue;
    private boolean hasNewData;
    
    public SimpleDataProvider() {
        this.dataQueue = new LinkedList<List<LabOrderData>>();
        this.hasNewData = false;
    }
    
    /**
     * Add lab order data to the provider (for testing/demonstration)
     * @param labOrderDataList List of lab order data to add
     */
    public void addLabOrderData(List<LabOrderData> labOrderDataList) {
        if (labOrderDataList != null && !labOrderDataList.isEmpty()) {
            dataQueue.offer(labOrderDataList);
            hasNewData = true;
            log("SimpleDataProvider: Added " + labOrderDataList.size() + " lab orders to queue");
        }
    }
    
    /**
     * Add single lab order data to the provider
     * @param labOrderData Single lab order data to add
     */
    public void addLabOrderData(LabOrderData labOrderData) {
        if (labOrderData != null) {
            List<LabOrderData> dataList = Arrays.asList(labOrderData);
            addLabOrderData(dataList);
        }
    }
    
    @Override
    public List<LabOrderData> getLabOrderData(SerialPort serialPort) {
        if (dataQueue.isEmpty()) {
            return new ArrayList<LabOrderData>();
        }
        
        List<LabOrderData> data = dataQueue.poll();
        if (dataQueue.isEmpty()) {
            hasNewData = false;
        }
        
        log("SimpleDataProvider: Returning " + (data != null ? data.size() : 0) + " lab orders");
        return data != null ? data : new ArrayList<LabOrderData>();
    }
    
    @Override
    public void acknowledgeProcessing(String messageId, boolean success, String details) {
        log("SimpleDataProvider: Acknowledgment - ID: " + messageId + 
            ", Success: " + success + ", Details: " + details);
    }
    
    @Override
    public boolean hasDataAvailable() {
        return hasNewData && !dataQueue.isEmpty();
    }
    
    /**
     * Get the number of pending data batches
     * @return Number of data batches in queue
     */
    public int getPendingDataCount() {
        return dataQueue.size();
    }
    
    /**
     * Clear all pending data
     */
    public void clearData() {
        dataQueue.clear();
        hasNewData = false;
        log("SimpleDataProvider: Cleared all pending data");
    }
    
    /**
     * Create sample lab order data for testing
     * @return Sample lab order data
     */
    public static LabOrderData createSampleLabOrder(String patientId, String patientName, 
            String orderId, String testType, String testDescription) {
        LabOrderData order = new LabOrderData();
        order.setPatientId(patientId);
        order.setPatientName(patientName);
        order.setPatientSex("M");
        order.setPatientBirthDate("19800101");
        order.setOrderId(orderId);
        order.setTestType(testType);
        order.setTestDescription(testDescription);
        order.setOrderDateTime("202509161200");
        order.setSpecimenType("BLOOD");
        order.setPriority("ROUTINE");
        return order;
    }
    
    private void log(String message) {
        LabConnectUtil.log(message);
    }
} 