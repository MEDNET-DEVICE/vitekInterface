package com.writer;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete ASTM message containing all records
 */
public class ASTMMessage {
    private HeaderRecord headerRecord;
    private List<PatientRecord> patientRecords;
    private List<OrderRecord> orderRecords;
    private TerminatorRecord terminatorRecord;
    
    public ASTMMessage() {
        this.patientRecords = new ArrayList<PatientRecord>();
        this.orderRecords = new ArrayList<OrderRecord>();
    }
    
    public HeaderRecord getHeaderRecord() { return headerRecord; }
    public void setHeaderRecord(HeaderRecord headerRecord) { this.headerRecord = headerRecord; }
    
    public List<PatientRecord> getPatientRecords() { return patientRecords; }
    public void setPatientRecords(List<PatientRecord> patientRecords) { this.patientRecords = patientRecords; }
    
    public List<OrderRecord> getOrderRecords() { return orderRecords; }
    public void setOrderRecords(List<OrderRecord> orderRecords) { this.orderRecords = orderRecords; }
    
    public TerminatorRecord getTerminatorRecord() { return terminatorRecord; }
    public void setTerminatorRecord(TerminatorRecord terminatorRecord) { this.terminatorRecord = terminatorRecord; }
    
    public void addPatientRecord(PatientRecord patientRecord) {
        this.patientRecords.add(patientRecord);
    }
    
    public void addOrderRecord(OrderRecord orderRecord) {
        this.orderRecords.add(orderRecord);
    }
    
    /**
     * Convert the complete message to ASTM format strings
     * @return List of ASTM formatted strings
     */
    public List<String> toASTMStrings() {
        List<String> astmStrings = new ArrayList<String>();
        
        // Add header record
        if (headerRecord != null) {
            astmStrings.add(headerRecord.toASTMString());
        }
        
        // Add patient records
        for (PatientRecord patientRecord : patientRecords) {
            astmStrings.add(patientRecord.toASTMString());
        }
        
        // Add order records
        for (OrderRecord orderRecord : orderRecords) {
            astmStrings.add(orderRecord.toASTMString());
        }
        
        // Add terminator record
        if (terminatorRecord != null) {
            astmStrings.add(terminatorRecord.toASTMString());
        }
        
        return astmStrings;
    }
} 