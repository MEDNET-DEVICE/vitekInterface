# REST API Integration for ASTM Data Writer

## Overview

The ASTM Data Writer has been enhanced to automatically fetch lab order requests from a REST API endpoint and convert them to ASTM protocol messages for transmission to VITEK2 instruments.

## API Specification

### Endpoint
**URL**: `/mediInterfaceWS/getLabOrderRequests`
**Method**: `POST`
**Content-Type**: `application/json`

### Headers
```
API_KEY: MEDNET_LAB_INTERFACE
```

### Request Format
```json
{
  "machineCode": ["B121"],
  "companyID": "1"
}
```

### Response Formats

#### Case 1: Success with Data
```json
{
  "success": true,
  "data": [
    {
      "sampleID": "PFR660",
      "mSampleID": "PFR660", 
      "investigationCode": "CR392",
      "investigationName": "CREATININE",
      "patientName": "SARTHAK SARAF",
      "patientDOB": "2016-02-05",
      "patientCalcDOB": "2016-02-05",
      "gender": "M",
      "mrn": "SSH4160124",
      "acceptanceDate": "2024-01-16 15:26:00.0"
    }
  ]
}
```

#### Case 2: Success with No Data
```json
{
  "success": true,
  "data": []
}
```

#### Case 3: Error - Analyzer Not Found
```json
{
  "success": false,
  "data": {
    "error_code": 204,
    "error_message": "Analyzer Name is not found"
  }
}
```

#### Case 4: Error - Analyzer Not Matched
```json
{
  "success": false,
  "data": {
    "error_code": 204,
    "error_message": "Analyzer Name is not matched"
  }
}
```

## Acknowledgment API

### Endpoint Details
- **URL**: `/mediInterfaceWS/updateOrderAcknowledgement`
- **Method**: POST
- **Header**: `API_KEY: MEDNET_LAB_INTERFACE`
- **Content-Type**: `application/json`

### Request Format
```json
{
  "requestAckList": [
    {
      "sampleID": "BI10110002",
      "InvID": "INV_HB", 
      "MRN": "MRN-00100202",
      "acceptanceDate": "11-10-2018",
      "machineCode": "COBAS311C-A",
      "companyID": "1",
      "status": "success"
    }
  ]
}
```

### Field Mapping
- **sampleID**: From `sampleID` in the order request response
- **InvID**: From `investigationCode` in the order request response  
- **MRN**: From `mrn` in the order request response
- **acceptanceDate**: From `acceptanceDate` in the order request response (formatted as DD-MM-YYYY)
- **machineCode**: The machine code used in the original request
- **companyID**: The company ID used in the original request
- **status**: `"success"` if ASTM transmission successful, `"failed"` if transmission failed

## Configuration

### Configuration File Location
`/opt/mednet/windowsServiceVITEK2COMPACT.properties`

### Required Properties
```properties
# Data Writer Configuration
dataWriterEnabled=true

# API Configuration for Lab Order Requests
apiBaseUrl=http://localhost:8080
machineCode=B121
companyId=1

# Serial Port Configuration (shared with existing reader)
comPort=COM2
baudRate=9600
dataBits=8
stopBits=1
parity=0

# Polling interval (how often to check for new orders)
pollingInterval=30000
```

## Field Mapping

The API response fields are mapped to ASTM protocol fields as follows:

| API Field | ASTM Field | Description |
|-----------|------------|-------------|
| `sampleID` | Patient ID | Patient identifier |
| `patientName` | Patient Name | Patient full name (converted to Last^First format) |
| `gender` | Patient Sex | Patient gender (M/F) |
| `patientDOB` | Patient Birth Date | Birth date (converted to YYYYMMDD) |
| `mSampleID` | Order ID | Sample/Order identifier |
| `investigationCode` | Test Type | Investigation/Test code |
| `investigationName` | Test Description | Investigation/Test name |
| `acceptanceDate` | Order DateTime | Order acceptance date/time (converted to YYYYMMDDHHMM) |

## Generated ASTM Format

Based on the API response example, the system generates:

```
H|\^&|||DummyLIS|||||VITEK2||||P|1
P|1||PFR660||SARAF^SARTHAK||20160205|M
O|1|PFR660||CR392^CREATININE|||20240116152600||||||
L|1|N
```

## Usage

### 1. Automatic Processing
Once configured and enabled, the system automatically:
1. Polls the API endpoint every 30 seconds (configurable)
2. Fetches available lab orders
3. Converts them to ASTM format
4. Sends them via serial port to the instrument

### 2. Manual Triggering
External systems can trigger processing manually:

```java
// Check if data writer is available
if (LabConnectorDataWriter.isAvailable()) {
    // Test API connection
    boolean apiConnected = LabConnectorDataWriter.testApiConnection();
    
    if (apiConnected) {
        // Process lab orders from API
        boolean processed = LabConnectorDataWriter.processLabOrders();
        System.out.println("Processing result: " + processed);
    }
}
```

### 3. Integration with LabConnectorThread
```java
// Trigger processing through the integrated system
boolean result = LabConnectorThread.triggerLabOrderProcessing();
```

## API Implementation Components

### 1. LabOrderApiService
- Handles REST API communication
- Manages request/response formatting
- Converts API data to internal format

### 2. ApiDataProvider
- Implements DataProvider interface
- Manages polling intervals
- Provides data to the ASTM writer system

### 3. Enhanced Configuration
- Supports API endpoint configuration
- Machine code and company ID settings
- Validation of API-related properties

## Error Handling

### API Response Handling
The system gracefully handles all API response scenarios:

#### Success Responses
- **Case 1 (Data Available)**: Processes lab orders and converts to ASTM format
- **Case 2 (No Data)**: Logs "no orders available" and continues polling

#### Error Responses
- **Case 3 (Analyzer Not Found)**: 
  - Logs error with code 204
  - Provides guidance: "check machine code configuration"
  - Returns empty list to continue operation
- **Case 4 (Analyzer Not Matched)**:
  - Logs error with code 204
  - Provides guidance: "verify machine code mapping"
  - Returns empty list to continue operation

### API Connection Failures
- Service continues running if API is temporarily unavailable
- Logs connection failures for monitoring
- Retries on next polling interval
- Updates fetch time to prevent rapid retry loops

### Data Validation
- Validates API response format and structure
- Handles missing or malformed data gracefully
- Logs processing errors with detailed information
- Continues operation even with invalid individual records

### Serial Port Issues
- Maintains existing serial port error handling
- Continues processing even if individual transmissions fail

## Monitoring and Logging

### Log Messages
All activities are logged using the existing `LabConnectUtil.log()` system:

#### Successful Processing (Case 1)
```
LabOrderApiService: Fetching lab orders from: http://localhost:8080/mediInterfaceWS/getLabOrderRequests
LabOrderApiService: Request payload: {"machineCode":["B121"],"companyID":"1"}
LabOrderApiService: Received response: {"success":true,"data":[...]}
LabOrderApiService: Processing 1 lab orders from API
LabOrderApiService: Successfully converted 1 lab orders
ApiDataProvider: Successfully fetched 1 lab orders
DataWriterConnector: Retrieved 1 lab orders
DataWriterConnector: Built ASTM message with ID: ASTM_1726484400000
DataWriterConnector: ASTM transmission successful
DataWriterConnector: Sending order acknowledgment via API...
LabOrderApiService: Sending acknowledgment to: http://localhost:8080/mediInterfaceWS/updateOrderAcknowledgement
LabOrderApiService: Acknowledgment payload: {"requestAckList":[{"sampleID":"PFR660",...}]}
LabOrderApiService: Acknowledgment sent successfully: {"success":true}
ApiDataProvider: Acknowledgment sent successfully for 1 orders
DataWriterConnector: Order acknowledgment sent successfully
```

#### No Data Available (Case 2)
```
LabOrderApiService: No lab orders available (empty data array)
ApiDataProvider: No lab orders available at this time
```

#### Error Responses (Case 3 & 4)
```
LabOrderApiService: API Error - Code: 204, Message: Analyzer Name is not found
LabOrderApiService: Analyzer not found - check machine code configuration
ApiDataProvider: Successfully fetched 0 lab orders
```

### Status Monitoring
```java
// Get current status
String status = LabConnectorDataWriter.getStatus();
System.out.println(status);

// Get API configuration
String apiConfig = LabConnectorDataWriter.getApiConfiguration();
System.out.println(apiConfig);
```

## Testing

### 1. Configuration Test
```bash
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.DataWriterService test
```

### 2. API Connection Test
```bash
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.LabConnectorDataWriter demonstrateUsage
```

### 3. Manual Processing
```bash
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.DataWriterService process
```

### 4. Response Handling Test
```bash
java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.writer.ApiResponseTest
```

## Deployment Steps

1. **Update Configuration**:
   ```bash
   # Edit the configuration file
   vi /opt/mednet/windowsServiceVITEK2COMPACT.properties
   
   # Add API configuration
   dataWriterEnabled=true
   apiBaseUrl=http://your-api-server:8080
   machineCode=B121
   companyId=1
   ```

2. **Deploy Updated JAR**:
   ```bash
   mvn package
   cp target/Vitek2Compact-0.0.1-SNAPSHOT.jar /your/deployment/path/
   ```

3. **Start Service**:
   ```bash
   java -cp Vitek2Compact-0.0.1-SNAPSHOT.jar com.connector.LabConnectorServ
   ```

## API Server Requirements

The API server should:

1. **Accept POST requests** to `/mediInterfaceWS/getLabOrderRequests`
2. **Validate API_KEY header** with value `MEDNET_LAB_INTERFACE`
3. **Process request payload** with machineCode and companyID
4. **Return JSON response** with success flag and data array
5. **Handle concurrent requests** from multiple machines
6. **Provide appropriate HTTP status codes** (200 for success, 4xx/5xx for errors)

## Troubleshooting

### Common Issues

1. **API Connection Failed**
   - Check network connectivity
   - Verify API endpoint URL
   - Confirm API_KEY header is accepted
   - Review server logs for authentication errors

2. **No Lab Orders Retrieved**
   - Verify machineCode and companyId are correct
   - Check if there are pending orders for the machine
   - Review API server logs for request processing

3. **ASTM Transmission Failed**
   - Check serial port configuration
   - Verify cable connections
   - Review instrument communication settings

4. **Configuration Errors**
   - Validate all required properties are set
   - Check property file syntax
   - Verify file permissions

### Log Analysis
Check log files at `/tmp/mednet/log/` for detailed error messages and processing information.

## Security Considerations

1. **API Authentication**: Uses API_KEY header for authentication
2. **Network Security**: Ensure HTTPS is used for production deployments
3. **Access Control**: Limit network access to authorized systems
4. **Data Validation**: All API responses are validated before processing

## Performance

- **Polling Interval**: Configurable (default 30 seconds)
- **Concurrent Processing**: Handles multiple lab orders in single API call
- **Memory Usage**: Minimal - processes orders as received
- **Network Traffic**: Efficient - only polls when needed

This integration provides seamless connectivity between your LIS and VITEK2 instruments while maintaining all existing functionality. 