package com.writer;

/**
 * ASTM Patient Record (P) 
 * Format: P|1||12345||Doe^John||19800101|M
 */
public class PatientRecord extends ASTMRecord {
    private String practiceAssignedPatientId;
    private String laboratoryAssignedPatientId;
    private String patientId;
    private String patientName;
    private String mothersMaidenName;
    private String birthdate;
    private String patientSex;
    private String patientRaceEthnicity;
    private String patientAddress;
    private String reserved;
    private String patientTelephoneNumber;
    private String attendingPhysicianId;
    private String specialField1;
    private String specialField2;
    private String patientHeight;
    private String patientWeight;
    private String patientsKnownDiagnosis;
    private String patientActiveMedications;
    private String patientsDiet;
    private String practiceField1;
    private String practiceField2;
    private String admissionDischargeDate;
    private String admissionStatus;
    private String location;
    private String natureOfAlternativeDiagnosticCodeAndClassification;
    private String alternativeDiagnosticCodeAndClassification;
    private String patientReligion;
    private String maritalStatus;
    private String isolationStatus;
    private String languageSpoken;
    private String hospitalService;
    private String hospitalInstitution;
    private String dosageCategory;
    
    public PatientRecord(String sequenceNumber) {
        super("P", sequenceNumber);
        this.practiceAssignedPatientId = "";
        this.laboratoryAssignedPatientId = "";
        this.patientId = "";
        this.patientName = "";
        this.mothersMaidenName = "";
        this.birthdate = "";
        this.patientSex = "";
        // Initialize other fields as empty
        this.patientRaceEthnicity = "";
        this.patientAddress = "";
        this.reserved = "";
        this.patientTelephoneNumber = "";
        this.attendingPhysicianId = "";
        this.specialField1 = "";
        this.specialField2 = "";
        this.patientHeight = "";
        this.patientWeight = "";
        this.patientsKnownDiagnosis = "";
        this.patientActiveMedications = "";
        this.patientsDiet = "";
        this.practiceField1 = "";
        this.practiceField2 = "";
        this.admissionDischargeDate = "";
        this.admissionStatus = "";
        this.location = "";
        this.natureOfAlternativeDiagnosticCodeAndClassification = "";
        this.alternativeDiagnosticCodeAndClassification = "";
        this.patientReligion = "";
        this.maritalStatus = "";
        this.isolationStatus = "";
        this.languageSpoken = "";
        this.hospitalService = "";
        this.hospitalInstitution = "";
        this.dosageCategory = "";
    }
    
    // Essential getters and setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }
    
    public String getPatientSex() { return patientSex; }
    public void setPatientSex(String patientSex) { this.patientSex = patientSex; }
    
    @Override
    public String toASTMString() {
        StringBuilder sb = new StringBuilder();
        sb.append(recordType).append("|")
          .append(sequenceNumber).append("|")
          .append(practiceAssignedPatientId).append("|")
          .append(escapeASTMValue(patientId)).append("|")
          .append(laboratoryAssignedPatientId).append("|")
          .append(escapeASTMValue(patientName)).append("|")
          .append(mothersMaidenName).append("|")
          .append(birthdate).append("|")
          .append(patientSex);
        
        return sb.toString();
    }
} 