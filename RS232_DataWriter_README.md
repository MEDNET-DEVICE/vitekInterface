# RS232 Data Writer for VITEK2 Compact

This implementation provides a simple RS232 protocol data writer that replaces the ASTM protocol implementation. It fetches lab order data from REST APIs, converts it to simple RS232 format, and sends it to serial ports using basic framing.

## Architecture Overview

The RS232 Data Writer consists of several key components:

### Core Components

1. **RS232 Protocol Models**
   - `RS232Message`: Simple message container for multiple lines
   - `RS232DataWriter`: Main writer class for RS232 communication

2. **Communication Methods**
   - **Simple Message**: Plain text with CR+LF termination
   - **Pipe-Delimited**: Structured format similar to DataReader input
   - **Framed Message**: STX + data + ETX + CR + LF format

3. **Updated Connector** (`DataWriterConnector.java`)
   - Uses `RS232DataWriter` instead of `DataWriter`
   - Sends pipe-delimited messages directly
   - Maintains same API integration for acknowledgments

## RS232 Message Formats

### 1. Simple Message Format
```
Message Text<CR><LF>
```

### 2. Pipe-Delimited Format (compatible with DataReader)
```
<STX>ci<SampleID>|pi<PatientID>|oi<OrderID>|tc<TestType>|td<TestDescription><ETX><CR><LF>
```

Example:
```
<STX>ciSAMPLE001|piPAT001|oiORD001|tcBLOOD_CULTURE|tdBlood Culture Identification<ETX><CR><LF>
```

### 3. Framed Message Format
```
<STX>MessageLine<ETX><CR><LF>
```

## Key Differences from ASTM Implementation

| Aspect | ASTM Protocol | RS232 Implementation |
|--------|---------------|---------------------|
| **Protocol Overhead** | ENQ/ACK handshaking, checksums, frame numbers | Simple STX/ETX framing |
| **Message Structure** | H/P/O/L records with complex formatting | Simple pipe-delimited lines |
| **Control Characters** | Full ASTM control flow | Basic STX, ETX, CR, LF |
| **Error Handling** | NAK/retransmission | Basic transmission only |
| **Compatibility** | ASTM standard compliant | Compatible with existing DataReader |

## Usage Examples

### Basic Integration
```java
// Replace DataWriter with RS232DataWriter
RS232DataWriter writer = new RS232DataWriter();

// Send simple message
writer.sendSimpleMessage("Test Message", serialPort);

// Send lab order data
List<LabOrderData> orders = getLabOrders();
writer.sendPipeDelimitedMessage(orders, serialPort);

// Send structured message
RS232Message message = RS232Message.fromLabOrderData(orders);
writer.sendRS232Message(message, serialPort);
```

### Configuration Changes
No configuration changes required - the RS232 implementation uses the same serial port settings as the ASTM version:
- Baud Rate: 9600
- Data Bits: 8
- Stop Bits: 1
- Parity: None

## Field Mappings

The RS232 implementation maps LabOrderData fields to pipe-delimited format:

| LabOrderData Field | RS232 Code | Description |
|-------------------|------------|-------------|
| `sampleId` | `ci` | Sample identifier |
| `patientId` | `pi` | Patient identifier |
| `orderId` | `oi` | Order identifier |
| `testType` | `tc` | Test code/type |
| `testDescription` | `td` | Test description |
| `orderDateTime` | `s1` | Sample/order date |

## Migration from ASTM

### Updated Classes
1. **DataWriterConnector.java** - Now uses `RS232DataWriter`
2. **New RS232DataWriter.java** - Replaces `DataWriter.java`
3. **New RS232Message.java** - Simple message container
4. **New RS232DemoWriter.java** - Testing and demonstration

### Removed Dependencies
- `ASTMMessage.java` - No longer needed
- `ASTMMessageBuilder.java` - No longer needed
- `ASTMRecord.java` and subclasses - No longer needed
- ASTM protocol control flow logic

### Maintained Compatibility
- Same REST API integration
- Same DataProvider interface
- Same acknowledgment flow
- Same logging mechanism
- Same configuration system

## Testing

### Demo Application
```bash
# Run all tests
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.RS232DemoWriter

# Test specific communication modes
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.RS232DemoWriter simple
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.RS232DemoWriter pipe
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.RS232DemoWriter structured
```

### Integration Testing
The RS232 implementation can be tested with the existing DataReader:
1. Start the DataReader service
2. Use RS232DemoWriter to send test messages
3. Verify DataReader receives and processes messages correctly

## Deployment

### 1. Rebuild the Project
```bash
mvn clean compile package
```

### 2. No Configuration Changes Required
The existing `sample_config.properties` works unchanged.

### 3. Service Integration
The RS232 implementation integrates seamlessly with:
- `LabConnectorServ` - Main service
- `DataWriterService` - Service management
- REST API endpoints - No changes needed

## Error Handling

- **Serial Port Errors**: Logged and operation continues
- **Message Formatting**: Invalid data is logged and skipped
- **Transmission Failures**: Logged with acknowledgment sent to API
- **Connection Issues**: Automatic retry on next polling cycle

## Logging

Uses the existing `LabConnectUtil.log()` mechanism:
- Logs written to `/tmp/mednet/log/` (or configured location)
- Daily log files with format: `dd-MM-yyyy_VITEK2COMPACTLog.txt`
- All RS232 operations logged with "RS232DataWriter:" prefix

## Performance Benefits

1. **Reduced Overhead**: No ASTM protocol complexity
2. **Faster Transmission**: Direct message sending without handshaking
3. **Simpler Debugging**: Plain text format easier to troubleshoot
4. **Better Compatibility**: Matches existing DataReader expectations

## Backward Compatibility

The RS232 implementation maintains full backward compatibility with:
- Existing configuration files
- REST API endpoints and responses
- Service management commands
- Log file formats and locations
- Integration with LabConnectorServ

## Support

For issues or questions regarding the RS232 implementation:
1. Check log files for transmission details
2. Use RS232DemoWriter for testing communication
3. Verify serial port configuration matches DataReader settings
4. Ensure lab order data contains required fields (sampleId, patientId, orderId) 