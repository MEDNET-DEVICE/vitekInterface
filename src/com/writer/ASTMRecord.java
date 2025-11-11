package com.writer;

/**
 * Base class for ASTM protocol records
 */
public abstract class ASTMRecord {
    protected String recordType;
    protected String sequenceNumber;
    
    public ASTMRecord(String recordType, String sequenceNumber) {
        this.recordType = recordType;
        this.sequenceNumber = sequenceNumber;
    }
    
    public String getRecordType() {
        return recordType;
    }
    
    public String getSequenceNumber() {
        return sequenceNumber;
    }
    
    /**
     * Convert the record to ASTM format string
     * @return ASTM formatted string
     */
    public abstract String toASTMString();
    
    /**
     * Escape special characters for ASTM protocol
     * @param value The value to escape
     * @return Escaped value
     */
    protected String escapeASTMValue(String value) {
        if (value == null) {
            return "";
        }
        // For most ASTM implementations, we don't escape ^ in the data
        // as it's used as a component separator in fields like patient names
        return value;
    }
} 