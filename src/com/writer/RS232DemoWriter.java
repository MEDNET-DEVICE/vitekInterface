package com.writer;

import java.util.ArrayList;
import java.util.List;
import jssc.SerialPort;
import jssc.SerialPortException;
import com.reader.LabConnectUtil;

/**
 * Demo class for testing RS232 data writer functionality
 */
public class RS232DemoWriter {
    
    public static void main(String[] args) {
        log("RS232DemoWriter: Starting RS232 communication demo");
        
        // Test different RS232 communication modes
        if (args.length > 0) {
            String mode = args[0].toLowerCase();
            switch (mode) {
                case "simple":
                    testSimpleMessage();
                    break;
                case "pipe":
                    testPipeDelimitedMessage();
                    break;
                case "structured":
                    testStructuredMessage();
                    break;
                default:
                    printUsage();
                    break;
            }
        } else {
            // Run all tests
            testSimpleMessage();
            testPipeDelimitedMessage();
            testStructuredMessage();
        }
        
        log("RS232DemoWriter: Demo completed");
    }
    
    /**
     * Test simple string message
     */
    private static void testSimpleMessage() {
        log("RS232DemoWriter: Testing simple message transmission");
        
        RS232DataWriter writer = new RS232DataWriter();
        SerialPort demoPort = null; // In real usage, this would be initialized
        
        // Example simple messages
        String[] testMessages = {
            "Hello VITEK2 Compact",
            "Test Message 1",
            "Status Check",
            "Configuration Update"
        };
        
        for (String message : testMessages) {
            log("RS232DemoWriter: Would send simple message: " + message);
            // Note: Actual serial port transmission would be:
            // writer.sendSimpleMessage(message, demoPort);
        }
        
        log("RS232DemoWriter: Simple message test completed");
    }
    
    /**
     * Test pipe-delimited message format
     */
    private static void testPipeDelimitedMessage() {
        log("RS232DemoWriter: Testing pipe-delimited message transmission");
        
        RS232DataWriter writer = new RS232DataWriter();
        SerialPort demoPort = null; // In real usage, this would be initialized
        
        // Create sample lab order data
        List<LabOrderData> labOrderList = createSampleLabOrders();
        
        log("RS232DemoWriter: Would send pipe-delimited message for " + labOrderList.size() + " orders:");
        for (LabOrderData order : labOrderList) {
            StringBuilder message = new StringBuilder();
            if (order.getSampleId() != null) {
                message.append("ci").append(order.getSampleId()).append("|");
            }
            if (order.getPatientId() != null) {
                message.append("pi").append(order.getPatientId()).append("|");
            }
            if (order.getOrderId() != null) {
                message.append("oi").append(order.getOrderId()).append("|");
            }
            if (order.getTestType() != null) {
                message.append("tc").append(order.getTestType()).append("|");
            }
            
            String finalMessage = message.toString();
            if (finalMessage.endsWith("|")) {
                finalMessage = finalMessage.substring(0, finalMessage.length() - 1);
            }
            
            log("RS232DemoWriter: Would send: " + finalMessage);
        }
        
        // Note: Actual transmission would be:
        // writer.sendPipeDelimitedMessage(labOrderList, demoPort);
        
        log("RS232DemoWriter: Pipe-delimited message test completed");
    }
    
    /**
     * Test structured RS232 message
     */
    private static void testStructuredMessage() {
        log("RS232DemoWriter: Testing structured RS232 message transmission");
        
        RS232DataWriter writer = new RS232DataWriter();
        SerialPort demoPort = null; // In real usage, this would be initialized
        
        // Create structured message
        List<LabOrderData> labOrderList = createSampleLabOrders();
        RS232Message message = RS232Message.fromLabOrderData(labOrderList);
        
        if (message != null) {
            log("RS232DemoWriter: Would send structured message with " + message.getMessageLines().size() + " lines:");
            for (int i = 0; i < message.getMessageLines().size(); i++) {
                log("RS232DemoWriter: Line " + (i + 1) + ": " + message.getMessageLines().get(i));
            }
            
            // Note: Actual transmission would be:
            // writer.sendRS232Message(message, demoPort);
        }
        
        log("RS232DemoWriter: Structured message test completed");
    }
    
    /**
     * Create sample lab order data for testing
     */
    private static List<LabOrderData> createSampleLabOrders() {
        List<LabOrderData> labOrderList = new ArrayList<LabOrderData>();
        
        // Sample Order 1
        LabOrderData order1 = new LabOrderData();
        order1.setSampleId("SAMPLE001");
        order1.setPatientId("PAT001");
        order1.setPatientName("John Doe");
        order1.setOrderId("ORD001");
        order1.setTestType("BLOOD_CULTURE");
        order1.setTestDescription("Blood Culture Identification");
        order1.setOrderDateTime("20250929120000");
        labOrderList.add(order1);
        
        // Sample Order 2
        LabOrderData order2 = new LabOrderData();
        order2.setSampleId("SAMPLE002");
        order2.setPatientId("PAT002");
        order2.setPatientName("Jane Smith");
        order2.setOrderId("ORD002");
        order2.setTestType("URINE_CULTURE");
        order2.setTestDescription("Urine Culture and Sensitivity");
        order2.setOrderDateTime("20250929130000");
        labOrderList.add(order2);
        
        // Sample Order 3
        LabOrderData order3 = new LabOrderData();
        order3.setSampleId("SAMPLE003");
        order3.setPatientId("PAT003");
        order3.setPatientName("Bob Johnson");
        order3.setOrderId("ORD003");
        order3.setTestType("WOUND_CULTURE");
        order3.setTestDescription("Wound Culture and Antibiotic Sensitivity");
        order3.setOrderDateTime("20250929140000");
        labOrderList.add(order3);
        
        return labOrderList;
    }
    
    /**
     * Print usage information
     */
    private static void printUsage() {
        log("RS232DemoWriter Usage:");
        log("  java com.writer.RS232DemoWriter [mode]");
        log("  Modes:");
        log("    simple     - Test simple string messages");
        log("    pipe       - Test pipe-delimited messages");
        log("    structured - Test structured RS232 messages");
        log("    (no args)  - Run all tests");
    }
    
    /**
     * Log messages using the existing logging utility
     */
    private static void log(String message) {
        LabConnectUtil.log(message);
    }
} 