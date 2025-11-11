package com.writer;

/**
 * ASTM Terminator Record (L) - Last record in ASTM message
 * Format: L|1|N
 */
public class TerminatorRecord extends ASTMRecord {
    private String terminationCode;
    
    public TerminatorRecord(String sequenceNumber) {
        super("L", sequenceNumber);
        this.terminationCode = "N"; // N = Normal termination
    }
    
    public String getTerminationCode() { return terminationCode; }
    public void setTerminationCode(String terminationCode) { this.terminationCode = terminationCode; }
    
    @Override
    public String toASTMString() {
        return recordType + "|" + sequenceNumber + "|" + terminationCode;
    }
} 