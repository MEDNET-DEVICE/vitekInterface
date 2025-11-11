package com.writer;

/**
 * ASTM Header Record (H) - First record in ASTM message
 * Format: H|\\^&|||DummyLIS|||||VITEK2||||P|1
 */
public class HeaderRecord extends ASTMRecord {
    private String delimiters;
    private String messageControlId;
    private String senderName;
    private String senderStreetAddress;
    private String reserved1;
    private String senderTelephone;
    private String characteristics;
    private String receiverName;
    private String comments;
    private String processingId;
    private String versionNumber;
    
    public HeaderRecord(String sequenceNumber) {
        super("H", sequenceNumber);
        // Default ASTM delimiters
        this.delimiters = "\\\\^&";
        this.messageControlId = "";
        this.senderName = "DummyLIS";
        this.senderStreetAddress = "";
        this.reserved1 = "";
        this.senderTelephone = "";
        this.characteristics = "";
        this.receiverName = "VITEK2";
        this.comments = "";
        this.processingId = "P";
        this.versionNumber = "1";
    }
    
    // Getters and setters
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    
    public String getProcessingId() { return processingId; }
    public void setProcessingId(String processingId) { this.processingId = processingId; }
    
    public String getVersionNumber() { return versionNumber; }
    public void setVersionNumber(String versionNumber) { this.versionNumber = versionNumber; }
    
    @Override
    public String toASTMString() {
        StringBuilder sb = new StringBuilder();
        sb.append(recordType).append("|")
          .append(delimiters).append("|")
          .append(messageControlId).append("|")
          .append(messageControlId).append("|")  // Add missing field
          .append(escapeASTMValue(senderName)).append("|")
          .append(senderStreetAddress).append("|")
          .append(reserved1).append("|")
          .append(senderTelephone).append("|")
          .append(characteristics).append("|")
          .append(escapeASTMValue(receiverName)).append("|")
          .append(comments).append("|")
          .append(comments).append("|")  // Add missing field
          .append(comments).append("|")  // Add missing field
          .append(processingId).append("|")
          .append(versionNumber);
        
        return sb.toString();
    }
} 