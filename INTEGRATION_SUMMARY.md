# ASTM Data Writer Integration Summary

## Overview

The ASTM Data Writer has been successfully integrated into the existing VITEK2 Compact system. The integration allows the same serial port and configuration to handle both data reading (existing functionality) and data writing (new ASTM protocol functionality).

## Integration Points

### 1. LabConnectorThread Integration

**File**: `src/com/connector/LabConnectorThread.java`

**Changes Made**:
- Added data writer service initialization when enabled in configuration
- Added method to check if data writer is enabled: `isDataWriterEnabled()`
- Added method to send lab orders: `addLabOrderForWriting(LabOrderData)`
- Proper cleanup of data writer service on port closure

**Key Features**:
- Data writer is optional and controlled by configuration
- Shares the same serial port as the data reader
- Automatic startup and shutdown with the main service

### 2. Configuration Integration

**File**: `sample_config.properties`

**New Configuration Option**:
```properties
# Data Writer Configuration
# Enable/disable the ASTM data writer functionality
dataWriterEnabled=true
```

**Configuration File Location**: `/opt/mednet/windowsServiceVITEK2COMPACT.properties`

### 3. Service Integration

**File**: `src/com/connector/LabConnectorServ.java`

**Changes Made**:
- Added logging for data writer status on service startup
- Shows whether data writer is enabled or disabled

### 4. Utility Class for External Systems

**File**: `src/com/writer/LabConnectorDataWriter.java`

**Purpose**: Provides a simple interface for external systems to send lab order data through ASTM protocol

**Key Methods**:
```java
// Send lab order with individual parameters
LabConnectorDataWriter.sendLabOrder(patientId, patientName, patientSex, 
                                   birthDate, orderId, testType, testDescription, orderDateTime);

// Send complete lab order object
LabConnectorDataWriter.sendLabOrder(labOrderData);

// Check if data writer is available
LabConnectorDataWriter.isAvailable();

// Get status information
LabConnectorDataWriter.getStatus();
```

## ASTM Protocol Implementation

### Message Format
The implementation generates ASTM messages in the following format:

```
H|\^&|||DummyLIS|||||VITEK2||||P|1
P|1||12345||Doe^John||19800101|M
O|1|ORD1234||BLOOD CULTURE^MICRO|||202509161200||||||
L|1|N
```

### Record Types
- **H (Header)**: System identification and delimiters
- **P (Patient)**: Patient demographics and information
- **O (Order)**: Test order details
- **L (Terminator)**: End of message marker

## Usage Examples

### 1. Basic Integration Usage

```java
// Check if data writer is available
if (LabConnectorDataWriter.isAvailable()) {
    // Send a lab order
    boolean success = LabConnectorDataWriter.sendLabOrder(
        "P001",             // Patient ID
        "John Smith",       // Patient Name
        "M",                // Sex
        "19850315",         // Birth Date (YYYYMMDD)
        "ORD001",           // Order ID
        "BLOOD CULTURE",    // Test Type
        "MICRO",           // Test Description
        "202509161230"     // Order DateTime (YYYYMMDDHHMM)
    );
    
    if (success) {
        System.out.println("Lab order queued for ASTM transmission");
    }
}
```

### 2. Using Complete Lab Order Object

```java
// Create lab order data
LabOrderData labOrder = new LabOrderData();
labOrder.setPatientId("P002");
labOrder.setPatientName("Jane Doe");
labOrder.setPatientSex("F");
labOrder.setPatientBirthDate("19900520");
labOrder.setOrderId("ORD002");
labOrder.setTestType("URINE CULTURE");
labOrder.setTestDescription("MICRO");
labOrder.setOrderDateTime("202509161245");
labOrder.setSpecimenType("URINE");
labOrder.setPriority("STAT");

// Send through integrated system
LabConnectorDataWriter.sendLabOrder(labOrder);
```

## Configuration Steps

1. **Copy Configuration File**:
   ```bash
   cp sample_config.properties /opt/mednet/windowsServiceVITEK2COMPACT.properties
   ```

2. **Edit Configuration**:
   ```properties
   # Enable data writer
   dataWriterEnabled=true
   
   # Configure serial port (shared with reader)
   comPort=COM2
   baudRate=9600
   dataBits=8
   stopBits=1
   parity=0
   ```

3. **Start Service**:
   ```bash
   java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.connector.LabConnectorServ
   ```

## Flow Description

1. **Service Startup**:
   - LabConnectorServ starts LabConnectorThread
   - LabConnectorThread reads configuration
   - If `dataWriterEnabled=true`, initializes DataWriterService
   - Both reader and writer share the same serial port

2. **Data Reading** (Existing functionality):
   - Serial port listener receives data from VITEK2 instrument
   - Data is parsed and sent to configured REST API
   - Original functionality remains unchanged

3. **Data Writing** (New functionality):
   - External systems call `LabConnectorDataWriter.sendLabOrder()`
   - Lab order data is queued in DataWriterService
   - Service polls for queued data and converts to ASTM format
   - ASTM messages are sent through the same serial port
   - Acknowledgments are handled through DataProvider interface

## Key Benefits

1. **Seamless Integration**: Uses existing infrastructure and configuration
2. **Optional Feature**: Can be enabled/disabled without affecting existing functionality
3. **Shared Resources**: Efficiently uses the same serial port for both reading and writing
4. **Simple API**: Easy-to-use interface for external systems
5. **Standard Compliance**: Implements proper ASTM protocol formatting
6. **Logging**: Full integration with existing logging infrastructure

## Testing

```bash
# Compile the project
mvn compile

# Test ASTM message generation
java -cp target/Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.TestDataGenerator

# Test integration (requires serial port)
java -cp target/Vitek2Compact-0.0.1-SNAPSHOT.jar com.connector.LabConnectorServ
```

## Files Modified/Added

### Modified Files:
- `src/com/connector/LabConnectorThread.java` - Added data writer integration
- `src/com/connector/LabConnectorServ.java` - Added status logging
- `sample_config.properties` - Updated configuration
- `pom.xml` - Updated Java version to 1.8

### New Files:
- `src/com/writer/ASTMRecord.java` - Base ASTM record class
- `src/com/writer/HeaderRecord.java` - ASTM Header record
- `src/com/writer/PatientRecord.java` - ASTM Patient record
- `src/com/writer/OrderRecord.java` - ASTM Order record
- `src/com/writer/TerminatorRecord.java` - ASTM Terminator record
- `src/com/writer/ASTMMessage.java` - Complete ASTM message container
- `src/com/writer/LabOrderData.java` - Lab order data transfer object
- `src/com/writer/ASTMMessageBuilder.java` - ASTM message builder
- `src/com/writer/DataWriter.java` - Serial port ASTM writer
- `src/com/writer/DataProvider.java` - Data provider interface
- `src/com/writer/SimpleDataProvider.java` - Simple data provider implementation
- `src/com/writer/DataWriterConnector.java` - Main processing coordinator
- `src/com/writer/DataWriterConfig.java` - Configuration management
- `src/com/writer/DataWriterService.java` - Main service class
- `src/com/writer/TestDataGenerator.java` - Testing utilities
- `src/com/writer/DemoDataWriter.java` - Demonstration examples
- `src/com/writer/LabConnectorDataWriter.java` - Integration utility class

## Future Enhancements

1. **Multiple Serial Ports**: Support for separate read/write ports
2. **Enhanced Error Recovery**: More robust error handling and retry mechanisms
3. **Message Queuing**: Persistent queue for lab orders
4. **Real-time Monitoring**: Web interface for monitoring ASTM transmissions
5. **Additional Record Types**: Support for Result (R) and Comment (C) records

This integration provides a complete ASTM data writing solution that seamlessly works with the existing VITEK2 Compact infrastructure while maintaining all original functionality. 