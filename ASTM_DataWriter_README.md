# ASTM Data Writer for VITEK2 Compact

This implementation provides a complete ASTM protocol data writer that fetches lab order data from REST APIs, converts it to ASTM format, and sends it to serial ports. It follows the same architectural patterns as the existing VITEK2 Compact data reader.

## Architecture Overview

The ASTM Data Writer consists of several key components:

### Core Components

1. **ASTM Protocol Models** (`com.writer.*Record.java`)
   - `ASTMRecord`: Base class for all ASTM records
   - `HeaderRecord`: H record (Header)
   - `PatientRecord`: P record (Patient information)
   - `OrderRecord`: O record (Order information)
   - `TerminatorRecord`: L record (Termination)

2. **Message Builder** (`ASTMMessageBuilder.java`)
   - Converts lab order data to ASTM format
   - Handles data formatting and validation
   - Supports the exact format specified:
     ```
     H|\^&|||DummyLIS|||||VITEK2||||P|1
     P|1||12345||Doe^John||19800101|M
     O|1|ORD1234||BLOOD CULTURE^MICRO|||202509161200|||||||
     L|1|N
     ```

3. **Data Writer** (`DataWriter.java`)
   - Handles serial port communication
   - Implements ASTM protocol control characters
   - Manages transmission flow (ENQ, STX, ETX, EOT, ACK)

4. **REST API Service** (`RestApiService.java`)
   - Fetches lab order data from configured API endpoint
   - Sends acknowledgments back to API
   - Flexible JSON parsing for various API formats

5. **Main Connector** (`DataWriterConnector.java`)
   - Orchestrates the complete flow
   - Manages polling and processing
   - Handles error recovery

6. **Service Layer** (`DataWriterService.java`)
   - Entry point for the application
   - Command-line interface
   - Service lifecycle management

## Flow Description

1. **Data Input**: External systems provide lab order data through DataProvider interface
2. **ASTM Conversion**: Lab order data is converted to ASTM format using the builder
3. **Serial Transmission**: ASTM message is sent to serial port with proper protocol
4. **Acknowledgment**: Success/failure acknowledgment is sent back to DataProvider

## Configuration

### Configuration File Location
- Default: `/opt/mednet/windowsServiceVITEK2COMPACT_WRITER.properties`
- Can be overridden with system property `writerPropertyFileSuffix`

### Sample Configuration
```properties
# Serial Port Configuration
comPort=COM2
baudRate=9600
dataBits=8
stopBits=1
parity=0

# Service Configuration
enabled=true
pollingInterval=30000

# ASTM Protocol Configuration (Optional)
senderName=DummyLIS
receiverName=VITEK2
processingId=P
versionNumber=1
```

## Data Integration

The ASTM Data Writer uses a `DataProvider` interface for integration with external systems. This design separates the ASTM protocol implementation from data source specifics.

### DataProvider Interface
```java
public interface DataProvider {
    List<LabOrderData> getLabOrderData();
    void acknowledgeProcessing(String messageId, boolean success, String details);
    boolean hasDataAvailable();
}
```

### Simple Integration Example
```java
// Create and configure data provider
SimpleDataProvider dataProvider = new SimpleDataProvider();

// Add lab order data
LabOrderData labOrder = new LabOrderData();
labOrder.setPatientId("12345");
labOrder.setPatientName("John Doe");
labOrder.setPatientSex("M");
// ... set other fields

dataProvider.addLabOrderData(labOrder);

// Initialize and configure service
DataWriterService.initialize();
DataWriterService.addLabOrderData(labOrder);
DataWriterService.processOnce();
```

## Usage

### Standalone Execution
```bash
# Start the service
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.DataWriterService start

# Test configuration
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.DataWriterService test

# Process once (manual trigger)
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.DataWriterService process

# Get status
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.DataWriterService status
```

### Integration with Existing Service
The DataWriterService can be integrated into the existing LabConnectorServ:

```java
// In LabConnectorServ.start()
DataWriterService.start();

// In LabConnectorServ.stop()
DataWriterService.stop();
```

### Testing
```bash
# Run ASTM format tests
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.TestDataGenerator

# Test specific components
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.TestDataGenerator format

# Run demonstrations
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.DemoDataWriter
```

## ASTM Protocol Implementation

### Message Structure
Each ASTM message contains:
1. **Header (H)**: System identification and delimiters
2. **Patient (P)**: Patient demographics
3. **Order (O)**: Test order information
4. **Terminator (L)**: End of message marker

### Protocol Control
- **ENQ (0x05)**: Establish communication
- **STX (0x02)**: Start of text block
- **ETX (0x03)**: End of text block
- **EOT (0x04)**: End of transmission
- **ACK (0x06)**: Acknowledgment
- **Checksum**: Calculated for each frame

### Frame Format
```
STX + Frame# + Data + ETX + Checksum + CR + LF
```

## Error Handling

- Serial port connection failures are logged and retried
- REST API timeouts trigger acknowledgment failures
- Invalid data formats are logged and skipped
- Service continues running despite individual processing failures

## Logging

Uses the existing `LabConnectUtil.log()` mechanism:
- Logs are written to `/tmp/mednet/log/` (or configured location)
- Daily log files with format: `dd-MM-yyyy_VITEK2COMPACTLog.txt`
- All operations are logged with timestamps

## Dependencies

No additional dependencies required beyond existing project:
- JSSC for serial communication
- Apache CXF for REST clients
- Jackson for JSON processing

## Deployment

1. Copy the sample configuration file to the appropriate location
2. Modify configuration for your environment
3. Ensure serial port permissions are properly set
4. Start the service using the command-line interface

## Monitoring

The service provides several monitoring capabilities:
- Status endpoint shows current state
- Continuous logging of all operations
- Acknowledgment tracking for API integration
- Serial port connection status

## Troubleshooting

### Common Issues

1. **Serial Port Access**: Ensure the user has permission to access the serial port
2. **API Connectivity**: Verify fetch and acknowledge URLs are accessible
3. **Configuration**: Check that all required properties are set
4. **Port Conflicts**: Ensure the serial port isn't in use by other applications

### Log Analysis
Check log files for:
- Configuration validation errors
- Serial port connection issues
- REST API communication problems
- ASTM formatting errors

## Future Enhancements

- Support for additional ASTM record types (R for Results)
- Enhanced error recovery mechanisms
- Multiple serial port support
- Real-time monitoring dashboard
- Database persistence for message tracking 