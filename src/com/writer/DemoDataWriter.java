package com.writer;

import java.util.*;
import com.reader.LabConnectUtil;

/**
 * Demonstration class showing how to use the ASTM Data Writer
 * This shows the complete flow without REST API dependencies
 */
public class DemoDataWriter {
    
    /**
     * Main demonstration method
     */

    /**
     * Demonstrate ASTM message building
     */
    public static void demonstrateASTMBuilding() {
        System.out.println("=== ASTM Message Building Demo ===");
        
        // Create sample data
        List<LabOrderData> labOrders = new ArrayList<LabOrderData>();
        
        LabOrderData order1 = SimpleDataProvider.createSampleLabOrder(
            "12345", "John Doe", "ORD1234", "BLOOD CULTURE", "MICRO"
        );
        labOrders.add(order1);
        
        LabOrderData order2 = SimpleDataProvider.createSampleLabOrder(
            "67890", "Jane Smith", "ORD5678", "URINE CULTURE", "MICRO"
        );
        labOrders.add(order2);
        
        // Build ASTM message
        ASTMMessage astmMessage = ASTMMessageBuilder.buildASTMMessage(labOrders);
        
        if (astmMessage != null) {
            List<String> astmStrings = astmMessage.toASTMStrings();
            System.out.println("Generated ASTM message with " + astmStrings.size() + " records:");
            
            for (int i = 0; i < astmStrings.size(); i++) {
                System.out.println("Record " + (i + 1) + ": " + astmStrings.get(i));
            }
        } else {
            System.out.println("Failed to build ASTM message");
        }
        
        System.out.println("=== End ASTM Building Demo ===");
    }


} 