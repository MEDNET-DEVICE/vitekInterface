package com.writer;

/**
 * ASTM Order Record (O)
 * Format: O|1|ORD1234||BLOOD CULTURE^MICRO|||202509161200|||||||
 */
public class OrderRecord extends ASTMRecord {
    private String specimenId;
    private String instrumentSpecimenId;
    private String universalTestId;
    private String priority;
    private String requestedOrderedDateTime;
    private String specimeCollectionDateTime;
    private String collectorId;
    private String actionCode;
    private String dangerCode;
    private String relevantClinicalInfo;
    private String dateTimeSpecimenReceived;
    private String specimenDescriptor;
    private String orderingPhysician;
    private String physicianTelephoneNumber;
    private String userField1;
    private String userField2;
    private String laboratoryField1;
    private String laboratoryField2;
    private String dateTimeResultsReportedOrLastModified;
    private String instrumentChargeToComputerSystem;
    private String instrumentSectionId;
    private String reportTypes;
    private String reserved;
    private String locationOfSpecimenCollection;
    private String nosocomialInfectionFlag;
    private String specimenService;
    private String specimenInstitution;
    
    public OrderRecord(String sequenceNumber) {
        super("O", sequenceNumber);
        this.specimenId = "";
        this.instrumentSpecimenId = "";
        this.universalTestId = "";
        this.priority = "";
        this.requestedOrderedDateTime = "";
        this.specimeCollectionDateTime = "";
        this.collectorId = "";
        this.actionCode = "";
        this.dangerCode = "";
        this.relevantClinicalInfo = "";
        this.dateTimeSpecimenReceived = "";
        this.specimenDescriptor = "";
        this.orderingPhysician = "";
        this.physicianTelephoneNumber = "";
        this.userField1 = "";
        this.userField2 = "";
        this.laboratoryField1 = "";
        this.laboratoryField2 = "";
        this.dateTimeResultsReportedOrLastModified = "";
        this.instrumentChargeToComputerSystem = "";
        this.instrumentSectionId = "";
        this.reportTypes = "";
        this.reserved = "";
        this.locationOfSpecimenCollection = "";
        this.nosocomialInfectionFlag = "";
        this.specimenService = "";
        this.specimenInstitution = "";
    }
    
    // Essential getters and setters
    public String getSpecimenId() { return specimenId; }
    public void setSpecimenId(String specimenId) { this.specimenId = specimenId; }
    
    public String getUniversalTestId() { return universalTestId; }
    public void setUniversalTestId(String universalTestId) { this.universalTestId = universalTestId; }
    
    public String getRequestedOrderedDateTime() { return requestedOrderedDateTime; }
    public void setRequestedOrderedDateTime(String requestedOrderedDateTime) { this.requestedOrderedDateTime = requestedOrderedDateTime; }
    
    @Override
    public String toASTMString() {
        // Format: O|1|ORD1234||BLOOD CULTURE^MICRO|||202509161200|||||||
        StringBuilder sb = new StringBuilder();
        sb.append(recordType).append("|")          // O|
          .append(sequenceNumber).append("|")      // 1|
          .append(escapeASTMValue(specimenId)).append("|")  // ORD1234|
          .append(instrumentSpecimenId).append("|") // |
          .append(escapeASTMValue(universalTestId)).append("|") // BLOOD CULTURE^MICRO|
          .append(priority).append("|")             // |
          .append("").append("|")                   // |
          .append(requestedOrderedDateTime).append("|") // 202509161200|
          .append("").append("|")                   // |
          .append("").append("|")                   // |
          .append("").append("|")                   // |
          .append("").append("|")                   // |
          .append("").append("|")                   // |
          .append("").append("|")                   // |
          .append("").append("|")                   // |
          .append("").append("|")                   // |
          .append("");                              // (no final pipe)
        
        return sb.toString();
    }
} 