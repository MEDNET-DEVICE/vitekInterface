package com.writer;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple demo for RS232 message formatting (without serial port dependencies)
 */
public class RS232MessageDemo {
    
    public static void main(String[] args) {
        System.out.println("RS232MessageDemo: Starting RS232 message formatting demo");
        
        // Test different message creation modes
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
        
        System.out.println("RS232MessageDemo: Demo completed");
    }
    
    /**
     * Test simple message creation
     */
    private static void testSimpleMessage() {
        System.out.println("\n=== Simple Message Test ===");
        
        // Example simple messages
        String[] testMessages = {
            "Hello VITEK2 Compact",
            "Test Message 1", 
            "Status Check",
            "Configuration Update"
        };
        
        for (String message : testMessages) {
            System.out.println("Simple message: " + message);
            System.out.println("RS232 format: " + formatSimpleMessage(message));
        }
    }
    
    /**
     * Test pipe-delimited message format
     */
    private static void testPipeDelimitedMessage() {
        System.out.println("\n=== Pipe-Delimited Message Test ===");
        
        // Create sample lab order data
        List<LabOrderData> labOrderList = createSampleLabOrders();
        
        System.out.println("Lab orders to convert: " + labOrderList.size());
        for (LabOrderData order : labOrderList) {
            String pipeMessage = createPipeDelimitedMessage(order);
            System.out.println("Order: " + order.getSampleId() + " -> " + pipeMessage);
            System.out.println("RS232 format: " + formatFramedMessage(pipeMessage));
        }
    }
    
    /**
     * Test structured RS232 message
     */
    private static void testStructuredMessage() {
        System.out.println("\n=== Structured Message Test ===");
        
        // Create structured message
        List<LabOrderData> labOrderList = createSampleLabOrders();
        RS232Message message = RS232Message.fromLabOrderData(labOrderList);
        
        if (message != null) {
            System.out.println("Structured message lines: " + message.getMessageLines().size());
            for (int i = 0; i < message.getMessageLines().size(); i++) {
                String line = message.getMessageLines().get(i);
                System.out.println("Line " + (i + 1) + ": " + line);
                System.out.println("RS232 format: " + formatFramedMessage(line));
            }
        }
    }
    
    /**
     * Create sample lab order data for testing
     */
    private static List<LabOrderData> createSampleLabOrders() {
        List<LabOrderData> labOrderList = new ArrayList<LabOrderData>();
        
        // Sample Order 1 - Regular name with BLOOD specimen
        LabOrderData order1 = new LabOrderData();
        order1.setSampleId("SAMPLE001");
        order1.setMrn("MRN1234567890");
        order1.setPatientId("PAT001");
        order1.setPatientName("John Doe");
        order1.setSpecimenType("BLOOD");
        order1.setOrderId("ORD001");
        order1.setTestType("BLOOD_CULTURE");
        order1.setTestDescription("Blood Culture Identification");
        order1.setOrderDateTime("20250929120000");
        labOrderList.add(order1);
        
        // Sample Order 2 - Name with Dr. prefix (will be removed), URINE specimen
        LabOrderData order2 = new LabOrderData();
        order2.setSampleId("SAMPLE002");
        order2.setMrn("MRN9876543210");
        order2.setPatientId("PAT002");
        order2.setPatientName("Dr. SOURAV KUMAR");
        order2.setSpecimenType("URINE");
        order2.setOrderId("ORD002");
        order2.setTestType("URINE_CULTURE");
        order2.setTestDescription("Urine Culture and Sensitivity");
        order2.setOrderDateTime("20250929130000");
        labOrderList.add(order2);
        
        // Sample Order 3 - Name with Baby boy of prefix (will be removed), WOUND specimen
        LabOrderData order3 = new LabOrderData();
        order3.setSampleId("SAMPLE003");
        order3.setMrn("MRN5555555555");
        order3.setPatientId("PAT003");
        order3.setPatientName("Baby boy of Krishna Sharma");
        order3.setSpecimenType("WOUND");
        order3.setOrderId("ORD003");
        order3.setTestType("WOUND_CULTURE");
        order3.setTestDescription("Wound Culture and Antibiotic Sensitivity");
        order3.setOrderDateTime("20250929140000");
        labOrderList.add(order3);
        
        return labOrderList;
    }
    
    /**
     * Create RS232 segment message from lab order
     * Format: mt(3)|pi(16)|pn(40)|pl|si|ss|sp|ci(20)|zz|
     */
    private static String createPipeDelimitedMessage(LabOrderData order) {
        StringBuilder messageBuilder = new StringBuilder();
        
        // mt - static value "mpr" (size=3)
        messageBuilder.append("mt").append(padOrTruncate("mpr", 3)).append("|");
        
        // pi - patient MRN from order (size=16)
        String mrn = order.getMrn() != null ? order.getMrn() : "";
        messageBuilder.append("pi").append(padOrTruncate(mrn, 16)).append("|");
        
        // pn - last name, first name from order (size=40)
        String patientName = formatPatientName(order.getPatientName());
        messageBuilder.append("pn").append(padOrTruncate(patientName, 40)).append("|");
        
        // pl - verify on worklist page of BCI software (empty for demo)
        messageBuilder.append("pl").append("|");
        
        // si - blank but create the element
        messageBuilder.append("si").append("|");
        
        // ss - sample type from order (specimenType)
        String sampleType = order.getSpecimenType() != null ? order.getSpecimenType() : "";
        messageBuilder.append("ss").append(sampleType).append("|");
        
        // sp - sample type from order (same as ss)
        messageBuilder.append("sp").append(sampleType).append("|");
        
        // ci - sample id from order (size=20)
        String sampleId = order.getSampleId() != null ? order.getSampleId() : "";
        messageBuilder.append("ci").append(padOrTruncate(sampleId, 20)).append("|");
        
        // zz - end of message marker (empty value)
        messageBuilder.append("zz").append("|");
        
        return messageBuilder.toString();
    }
    
    /**
     * Format patient name as "LastName, FirstName"
     * Removes configured prefixes before formatting
     */
    private static String formatPatientName(String patientName) {
        if (patientName == null || patientName.trim().isEmpty()) {
            return "";
        }
        
        // Remove configured prefixes (Dr., Mr., Mrs., Baby boy of, etc.)
        String nameWithoutPrefix = PatientNameConfig.removePrefixes(patientName);
        
        // If already contains comma, assume it's in correct format
        if (nameWithoutPrefix.contains(",")) {
            return nameWithoutPrefix;
        }
        
        // If contains space, assume it's "FirstName LastName" and reformat
        String[] parts = nameWithoutPrefix.trim().split("\\s+", 2);
        if (parts.length == 2) {
            return parts[1] + ", " + parts[0]; // LastName, FirstName
        }
        
        // Single name, return as is
        return nameWithoutPrefix;
    }
    
    /**
     * Truncate string if it exceeds max length (no padding)
     */
    private static String padOrTruncate(String value, int maxLength) {
        if (value == null) {
            value = "";
        }
        
        // Truncate if exceeds max length
        if (value.length() > maxLength) {
            return value.substring(0, maxLength);
        }
        
        // No padding - return as is
        return value;
    }
    
    /**
     * Format message with simple CR+LF termination
     */
    private static String formatSimpleMessage(String message) {
        return message + "\\r\\n";
    }
    
    /**
     * Format message with STX/ETX framing
     */
    private static String formatFramedMessage(String message) {
        return "\\x02" + message + "\\x03\\r\\n";
    }
    
    /**
     * Print usage information
     */
    private static void printUsage() {
        System.out.println("RS232MessageDemo Usage:");
        System.out.println("  java com.writer.RS232MessageDemo [mode]");
        System.out.println("  Modes:");
        System.out.println("    simple     - Test simple string messages");
        System.out.println("    pipe       - Test pipe-delimited messages");
        System.out.println("    structured - Test structured RS232 messages");
        System.out.println("    (no args)  - Run all tests");
    }
} 