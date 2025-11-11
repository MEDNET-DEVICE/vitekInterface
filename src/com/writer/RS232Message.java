package com.writer;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple RS232 message containing lines of text
 */
public class RS232Message {
    private List<String> messageLines;
    
    public RS232Message() {
        this.messageLines = new ArrayList<String>();
    }
    
    public List<String> getMessageLines() { 
        return messageLines; 
    }
    
    public void setMessageLines(List<String> messageLines) { 
        this.messageLines = messageLines; 
    }
    
    public void addMessageLine(String messageLine) {
        this.messageLines.add(messageLine);
    }
    
    /**
     * Create RS232 message from lab order data with specific segment format
     * Segments: mt(3)|pi(16)|pn(40)|pl|si|ci(20)
     * @param labOrderDataList List of lab order data
     * @return RS232 message with pipe-delimited format
     */
    public static RS232Message fromLabOrderData(List<LabOrderData> labOrderDataList) {
        return fromLabOrderData(labOrderDataList, null);
    }
    
    /**
     * Create RS232 message from lab order data with specific segment format
     * @param labOrderDataList List of lab order data
     * @param plValue Patient location value (verify on worklist page of BCI software)
     * @return RS232 message with pipe-delimited format
     */
    public static RS232Message fromLabOrderData(List<LabOrderData> labOrderDataList, String plValue) {
        if (labOrderDataList == null || labOrderDataList.isEmpty()) {
            return null;
        }
        
        RS232Message message = new RS232Message();
        
        for (LabOrderData labOrder : labOrderDataList) {
            String line = createRS232SegmentMessage(labOrder, plValue);
            if (!line.isEmpty()) {
                message.addMessageLine(line);
            }
        }
        
        return message;
    }
    
    /**
     * Create RS232 segment message with required fields
     * Format: mt|pi|pn|pl|si|ss|sp|ci|zz|
     */
    private static String createRS232SegmentMessage(LabOrderData labOrder, String plValue) {
        StringBuilder lineBuilder = new StringBuilder();
        
        // mt - static value "mpr" (size=3)
        lineBuilder.append("mt").append(padOrTruncate("mpr", 3)).append("|");
        
        // pi - patient MRN from order (size=16)
        String mrn = labOrder.getMrn() != null ? labOrder.getMrn() : "";
        lineBuilder.append("pi").append(padOrTruncate(mrn, 16)).append("|");
        
        // pn - last name, first name from order (size=40)
        String patientName = formatPatientName(labOrder.getPatientName());
        lineBuilder.append("pn").append(padOrTruncate(patientName, 40)).append("|");
        
        // pl - verify on worklist page of BCI software
        String location = plValue != null ? plValue : "";
        lineBuilder.append("pl").append(location).append("|");
        
        // si - blank but create the element
        lineBuilder.append("si").append("|");
        
        // ss - sample type from order (specimenType)
        String sampleType = labOrder.getSpecimenType() != null ? labOrder.getSpecimenType() : "";
        lineBuilder.append("ss").append(sampleType).append("|");
        
        // sp - sample type from order (same as ss)
        lineBuilder.append("sp").append(sampleType).append("|");
        
        // ci - sample id from order (size=20)
        String sampleId = labOrder.getSampleId() != null ? labOrder.getSampleId() : "";
        lineBuilder.append("ci").append(padOrTruncate(sampleId, 20)).append("|");
        
        // zz - end of message marker (empty value)
        lineBuilder.append("zz").append("|");
        
        return lineBuilder.toString();
    }
    
    /**
     * Format patient name as "LastName, FirstName"
     * Removes configured prefixes before formatting
     * If already in that format, return as is. If single name, use it.
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
     * Create simple RS232 message from string list
     * @param lines List of message lines
     * @return RS232 message
     */
    public static RS232Message fromStringList(List<String> lines) {
        RS232Message message = new RS232Message();
        if (lines != null) {
            message.setMessageLines(new ArrayList<String>(lines));
        }
        return message;
    }
    
    /**
     * Create simple RS232 message from single string
     * @param content Single message content
     * @return RS232 message
     */
    public static RS232Message fromString(String content) {
        RS232Message message = new RS232Message();
        if (content != null && !content.trim().isEmpty()) {
            message.addMessageLine(content);
        }
        return message;
    }
} 