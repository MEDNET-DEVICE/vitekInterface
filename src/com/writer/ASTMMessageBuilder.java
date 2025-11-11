package com.writer;

import jssc.SerialPort;

import java.util.List;

/**
 * Builder class to construct ASTM messages from lab order data
 */
public class ASTMMessageBuilder {
    
    /**
     * Build ASTM message from lab order data
     * @param labOrderDataList List of lab order data from REST API
     * @return Complete ASTM message
     */
    public static ASTMMessage buildASTMMessage(List<LabOrderData> labOrderDataList) {
        if (labOrderDataList == null || labOrderDataList.isEmpty()) {
            return null;
        }
        
        ASTMMessage astmMessage = new ASTMMessage();
        
        // Create header record
        HeaderRecord headerRecord = new HeaderRecord("1");
        astmMessage.setHeaderRecord(headerRecord);
        
        int patientSequence = 1;
        int orderSequence = 1;
        
        // Process each lab order
        for (LabOrderData labOrderData : labOrderDataList) {
            // Create patient record
            PatientRecord patientRecord = createPatientRecord(labOrderData, String.valueOf(patientSequence));
            astmMessage.addPatientRecord(patientRecord);
            
            // Create order record
            OrderRecord orderRecord = createOrderRecord(labOrderData, String.valueOf(orderSequence));
            astmMessage.addOrderRecord(orderRecord);
            
            patientSequence++;
            orderSequence++;
        }
        
        // Create terminator record
        TerminatorRecord terminatorRecord = new TerminatorRecord("1");
        astmMessage.setTerminatorRecord(terminatorRecord);
        
        return astmMessage;
    }
    
    /**
     * Create patient record from lab order data
     */
    private static PatientRecord createPatientRecord(LabOrderData labOrderData, String sequenceNumber) {
        PatientRecord patientRecord = new PatientRecord(sequenceNumber);
        
        patientRecord.setPatientId(labOrderData.getPatientId());
        patientRecord.setPatientName(formatPatientName(labOrderData.getPatientName()));
        patientRecord.setPatientSex(labOrderData.getPatientSex());
        patientRecord.setBirthdate(formatBirthdate(labOrderData.getPatientBirthDate()));
        
        return patientRecord;
    }
    
    /**
     * Create order record from lab order data
     */
    private static OrderRecord createOrderRecord(LabOrderData labOrderData, String sequenceNumber) {
        OrderRecord orderRecord = new OrderRecord(sequenceNumber);
        
        orderRecord.setSpecimenId(labOrderData.getOrderId());
        orderRecord.setUniversalTestId(formatUniversalTestId(labOrderData.getTestType(), labOrderData.getTestDescription()));
        orderRecord.setRequestedOrderedDateTime(formatDateTime(labOrderData.getOrderDateTime()));
        
        return orderRecord;
    }
    
    /**
     * Format patient name in ASTM format (Last^First)
     */
    private static String formatPatientName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        
        String[] nameParts = fullName.trim().split("\\s+");
        if (nameParts.length >= 2) {
            // Assume format: "First Last" and convert to "Last^First"
            return nameParts[nameParts.length - 1] + "^" + nameParts[0];
        } else {
            // Single name
            return fullName.trim();
        }
    }
    
    /**
     * Format birthdate to YYYYMMDD format
     */
    private static String formatBirthdate(String birthdate) {
        if (birthdate == null) {
            return "";
        }
        
        // Remove any non-digit characters and ensure 8 digits
        String digitsOnly = birthdate.replaceAll("\\D", "");
        if (digitsOnly.length() >= 8) {
            return digitsOnly.substring(0, 8);
        }
        
        return digitsOnly;
    }
    
    /**
     * Format universal test ID in ASTM format (TestType^TestDescription)
     */
    private static String formatUniversalTestId(String testType, String testDescription) {
        StringBuilder testId = new StringBuilder();
        
        if (testType != null && !testType.trim().isEmpty()) {
            testId.append(testType.trim());
        }
        
        if (testDescription != null && !testDescription.trim().isEmpty()) {
            if (testId.length() > 0) {
                testId.append("^");
            }
            testId.append(testDescription.trim());
        }
        
        return testId.toString();
    }
    
    /**
     * Format date time to YYYYMMDDHHMMSS format
     */
    private static String formatDateTime(String dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        // Remove any non-digit characters
        String digitsOnly = dateTime.replaceAll("\\D", "");
        
        // Ensure minimum 12 digits for YYYYMMDDHHMM, pad with zeros if needed
        if (digitsOnly.length() >= 12) {
            return digitsOnly.substring(0, Math.min(14, digitsOnly.length()));
        } else if (digitsOnly.length() >= 8) {
            // Add default time if only date provided
            return digitsOnly + "1200"; // Default to 12:00
        }
        
        return digitsOnly;
    }
} 