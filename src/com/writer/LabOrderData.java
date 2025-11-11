package com.writer;

/**
 * Data transfer object for lab order data received from REST API
 */
public class LabOrderData {
    private String patientId;
    private String patientName;
    private String patientSex;
    private String patientBirthDate;
    private String orderId;
    private String testType;
    private String testDescription;
    private String orderDateTime;
    private String specimenType;
    private String priority;
    
    // Additional fields from API response for acknowledgment
    private String sampleId;
    private String investigationCode;
    private String mrn;
    private String acceptanceDate;
    
    public LabOrderData() {}
    
    // Getters and setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public String getPatientSex() { return patientSex; }
    public void setPatientSex(String patientSex) { this.patientSex = patientSex; }
    
    public String getPatientBirthDate() { return patientBirthDate; }
    public void setPatientBirthDate(String patientBirthDate) { this.patientBirthDate = patientBirthDate; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }
    
    public String getTestDescription() { return testDescription; }
    public void setTestDescription(String testDescription) { this.testDescription = testDescription; }
    
    public String getOrderDateTime() { return orderDateTime; }
    public void setOrderDateTime(String orderDateTime) { this.orderDateTime = orderDateTime; }
    
    public String getSpecimenType() { return specimenType; }
    public void setSpecimenType(String specimenType) { this.specimenType = specimenType; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    // Additional getters and setters for API fields
    public String getSampleId() { return sampleId; }
    public void setSampleId(String sampleId) { this.sampleId = sampleId; }
    
    public String getInvestigationCode() { return investigationCode; }
    public void setInvestigationCode(String investigationCode) { this.investigationCode = investigationCode; }
    
    public String getMrn() { return mrn; }
    public void setMrn(String mrn) { this.mrn = mrn; }
    
    public String getAcceptanceDate() { return acceptanceDate; }
    public void setAcceptanceDate(String acceptanceDate) { this.acceptanceDate = acceptanceDate; }
} 