package com.writer;

/**
 * Test class to demonstrate patient name prefix removal
 */
public class PatientNamePrefixTest {
    
    public static void main(String[] args) {
        System.out.println("=== Patient Name Prefix Removal Test ===\n");
        
        // Initialize configuration
        PatientNameConfig.initialize();
        System.out.println("Loaded " + PatientNameConfig.getPrefixCount() + " prefixes to remove\n");
        
        // Test cases
        String[] testNames = {
            "Baby John Doe",
            "Baby boy of Mary Smith",
            "Baby girl of Sarah Johnson",
            "Dr. John Smith",
            "Mr. Robert Brown",
            "Mrs. Jennifer Davis",
            "Ms. Emily Wilson",
            "MASTER Thomas Anderson",
            "MISS. Rebecca Miller",
            "MX. Alex Taylor",
            "SOURAV KUMAR, MR.",
            "Dr. SOURAV KUMAR",
            "Baby boy of Krishna Sharma",
            "John Doe",
            "Smith, Jane"
        };
        
        System.out.println("Testing prefix removal and name formatting:\n");
        System.out.println(String.format("%-40s | %-40s", "Input Name", "Output (Formatted)"));
        System.out.println(String.format("%-40s-+-%-40s", 
            "----------------------------------------", 
            "----------------------------------------"));
        
        for (String testName : testNames) {
            String result = formatPatientName(testName);
            System.out.println(String.format("%-40s | %-40s", testName, result));
        }
        
        System.out.println("\n=== Test Complete ===");
    }
    
    /**
     * Format patient name (same logic as RS232Message)
     */
    private static String formatPatientName(String patientName) {
        if (patientName == null || patientName.trim().isEmpty()) {
            return "";
        }
        
        // Remove configured prefixes
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
} 