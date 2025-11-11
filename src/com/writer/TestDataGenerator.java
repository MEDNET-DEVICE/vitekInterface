package com.writer;

import java.util.*;

/**
 * Test data generator for ASTM Data Writer testing
 */
public class TestDataGenerator {
    
    /**
     * Generate sample lab order data for testing
     */
    public static List<LabOrderData> generateSampleLabOrders() {
        List<LabOrderData> labOrders = new ArrayList<LabOrderData>();
        
        // Sample 1: Basic blood culture
        LabOrderData order1 = new LabOrderData();
        order1.setPatientId("12345");
        order1.setPatientName("John Doe");
        order1.setPatientSex("M");
        order1.setPatientBirthDate("19800101");
        order1.setOrderId("ORD1234");
        order1.setTestType("BLOOD CULTURE");
        order1.setTestDescription("MICRO");
        order1.setOrderDateTime("202509161200");
        order1.setSpecimenType("BLOOD");
        order1.setPriority("ROUTINE");
        labOrders.add(order1);
        
        // Sample 2: Urine culture
        LabOrderData order2 = new LabOrderData();
        order2.setPatientId("67890");
        order2.setPatientName("Jane Smith");
        order2.setPatientSex("F");
        order2.setPatientBirthDate("19750315");
        order2.setOrderId("ORD5678");
        order2.setTestType("URINE CULTURE");
        order2.setTestDescription("MICRO");
        order2.setOrderDateTime("202509161300");
        order2.setSpecimenType("URINE");
        order2.setPriority("STAT");
        labOrders.add(order2);
        
        return labOrders;
    }
    
    /**
     * Generate a single lab order for testing
     */
    public static LabOrderData generateSingleLabOrder() {
        LabOrderData order = new LabOrderData();
        order.setPatientId("TEST001");
        order.setPatientName("Test Patient");
        order.setPatientSex("M");
        order.setPatientBirthDate("19900101");
        order.setOrderId("TESTORD001");
        order.setTestType("BLOOD CULTURE");
        order.setTestDescription("MICRO");
        order.setOrderDateTime("202509161400");
        order.setSpecimenType("BLOOD");
        order.setPriority("ROUTINE");
        return order;
    }
    
    /**
     * Test ASTM message generation
     */
    public static void testASTMMessageGeneration() {
        System.out.println("=== Testing ASTM Message Generation ===");
        
        List<LabOrderData> labOrders = generateSampleLabOrders();
        System.out.println("Generated " + labOrders.size() + " sample lab orders");
        
        // Build ASTM message
        ASTMMessage astmMessage = ASTMMessageBuilder.buildASTMMessage(labOrders);
        
        if (astmMessage != null) {
            System.out.println("ASTM message built successfully");
            
            // Get ASTM strings
            List<String> astmStrings = astmMessage.toASTMStrings();
            System.out.println("Generated " + astmStrings.size() + " ASTM records:");
            
            for (int i = 0; i < astmStrings.size(); i++) {
                System.out.println("Record " + (i + 1) + ": " + astmStrings.get(i));
            }
        } else {
            System.out.println("Failed to build ASTM message");
        }
        
        System.out.println("=== End ASTM Message Generation Test ===");
    }
    
    /**
     * Test the expected format against the provided examples
     */
    public static void testExpectedFormat() {
        System.out.println("=== Testing Expected ASTM Format ===");
        
        // Create a lab order that should match the expected format
        LabOrderData order = new LabOrderData();
        order.setPatientId("12345");
        order.setPatientName("John Doe");
        order.setPatientSex("M");
        order.setPatientBirthDate("19800101");
        order.setOrderId("ORD1234");
        order.setTestType("BLOOD CULTURE");
        order.setTestDescription("MICRO");
        order.setOrderDateTime("202509161200");
        
        List<LabOrderData> orders = Arrays.asList(order);
        ASTMMessage astmMessage = ASTMMessageBuilder.buildASTMMessage(orders);
        
        if (astmMessage != null) {
            List<String> astmStrings = astmMessage.toASTMStrings();
            
            System.out.println("Expected formats:");
            System.out.println("H|\\\\^&|||DummyLIS|||||VITEK2||||P|1");
            System.out.println("P|1||12345||Doe^John||19800101|M");
            System.out.println("O|1|ORD1234||BLOOD CULTURE^MICRO|||202509161200|||||||");
            System.out.println("L|1|N");
            
            System.out.println("\nGenerated formats:");
            for (String astmString : astmStrings) {
                System.out.println(astmString);
            }
            
            // Compare with expected
            String[] expected = {
                "H|\\\\^&|||DummyLIS|||||VITEK2||||P|1",
                "P|1||12345||Doe^John||19800101|M",
                "O|1|ORD1234||BLOOD CULTURE^MICRO|||202509161200|||||||",
                "L|1|N"
            };
            
            boolean matches = true;
            if (astmStrings.size() == expected.length) {
                for (int i = 0; i < expected.length; i++) {
                    if (!astmStrings.get(i).equals(expected[i])) {
                        System.out.println("Mismatch at record " + (i + 1));
                        System.out.println("Expected: " + expected[i]);
                        System.out.println("Generated: " + astmStrings.get(i));
                        matches = false;
                    }
                }
            } else {
                System.out.println("Record count mismatch. Expected: " + expected.length + ", Generated: " + astmStrings.size());
                matches = false;
            }
            
            System.out.println("Format validation: " + (matches ? "PASSED" : "FAILED"));
        }
        
        System.out.println("=== End Expected Format Test ===");
    }
    
    /**
     * Main method for running tests
     */
    public static void main(String[] args) {
        System.out.println("ASTM Data Writer Test Suite");
        System.out.println("============================");
        
        if (args.length > 0) {
            String testType = args[0].toLowerCase();
            
            switch (testType) {
                case "generate":
                    testASTMMessageGeneration();
                    break;
                case "format":
                    testExpectedFormat();
                    break;
                case "all":
                    testASTMMessageGeneration();
                    System.out.println();
                    testExpectedFormat();
                    break;
                default:
                    System.out.println("Unknown test type: " + testType);
                    System.out.println("Available tests: generate, format, all");
                    break;
            }
        } else {
            // Run all tests by default
            testASTMMessageGeneration();
            System.out.println();
            testExpectedFormat();
        }
    }
} 