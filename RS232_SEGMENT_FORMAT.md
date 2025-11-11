# RS232 Segment Message Format

## Overview
This document describes the specific RS232 message format used for VITEK2 Compact device communication. The format uses pipe-delimited segments with fixed field sizes for critical data elements.

## Message Structure

Each RS232 message consists of 6 segments separated by pipe (`|`) characters:

```
mt|pi|pn|pl|si|ci
```

### Segment Definitions

| Segment | Description | Source | Size | Required | Notes |
|---------|-------------|--------|------|----------|-------|
| `mt` | Message Type | Static value "mpr" | 3 | Yes | Always "mpr" |
| `pi` | Patient Identifier | Patient MRN from order | 16 | Yes | Right-padded with spaces |
| `pn` | Patient Name | Last name, First name | 40 | Yes | Format: "LastName, FirstName" |
| `pl` | Patient Location | Verify on worklist page | Variable | No | May be empty |
| `si` | Specimen Info | Reserved for future use | 0 | No | Always empty |
| `ci` | Case/Sample ID | Sample ID from order | 20 | Yes | Right-padded with spaces |

## Message Format Examples

### Example 1: Complete Message
```
mtmpr|piMRN1234567890   |pnDoe, John                               |pl|si|ciSAMPLE001           
```

**Breakdown:**
- `mt` = `mpr` (3 chars)
- `pi` = `MRN1234567890   ` (16 chars with trailing spaces)
- `pn` = `Doe, John                               ` (40 chars with trailing spaces)
- `pl` = `` (empty)
- `si` = `` (empty)
- `ci` = `SAMPLE001           ` (20 chars with trailing spaces)

### Example 2: With Patient Location
```
mtmpr|piMRN9876543210   |pnSmith, Jane                             |plWARD-A|si|ciSAMPLE002           
```

**Breakdown:**
- `mt` = `mpr` (3 chars)
- `pi` = `MRN9876543210   ` (16 chars)
- `pn` = `Smith, Jane                             ` (40 chars)
- `pl` = `WARD-A` (variable length)
- `si` = `` (empty)
- `ci` = `SAMPLE002           ` (20 chars)

## RS232 Framing

The complete message is transmitted with RS232 control characters:

```
<STX>message_content<ETX><CR><LF>
```

Where:
- `<STX>` = Start of Text (0x02)
- `message_content` = The pipe-delimited segment message
- `<ETX>` = End of Text (0x03)
- `<CR>` = Carriage Return (0x0D)
- `<LF>` = Line Feed (0x0A)

### Complete Transmission Example
```
\x02mtmpr|piMRN1234567890   |pnDoe, John                               |pl|si|ciSAMPLE001           \x03\r\n
```

## Field Specifications

### mt (Message Type)
- **Size**: Fixed 3 characters
- **Value**: Always "mpr"
- **Format**: Right-padded with spaces if needed (though "mpr" is already 3 chars)
- **Example**: `mpr`

### pi (Patient Identifier - MRN)
- **Size**: Fixed 16 characters
- **Source**: `LabOrderData.getMrn()`
- **Format**: Right-padded with spaces
- **Truncation**: If MRN exceeds 16 characters, truncated to 16
- **Example**: `MRN1234567890   ` (16 chars total)

### pn (Patient Name)
- **Size**: Fixed 40 characters
- **Source**: `LabOrderData.getPatientName()`
- **Format**: "LastName, FirstName" - Right-padded with spaces
- **Truncation**: If name exceeds 40 characters, truncated to 40
- **Name Conversion**: 
  - If name is "John Doe" → converts to "Doe, John"
  - If name is already "Doe, John" → keeps as is
  - If single name → keeps as is
- **Example**: `Doe, John                               ` (40 chars total)

### pl (Patient Location)
- **Size**: Variable length
- **Source**: Configuration or API (verify on BCI software worklist page)
- **Format**: No padding
- **Optional**: May be empty
- **Example**: `WARD-A` or `` (empty)

### si (Specimen Info)
- **Size**: 0 characters (always empty)
- **Source**: Reserved for future use
- **Format**: Always empty but segment separator is present
- **Example**: `` (empty)

### ci (Case/Sample ID)
- **Size**: Fixed 20 characters
- **Source**: `LabOrderData.getSampleId()`
- **Format**: Right-padded with spaces
- **Truncation**: If sample ID exceeds 20 characters, truncated to 20
- **Example**: `SAMPLE001           ` (20 chars total)

## Implementation Details

### Padding Rules
- All fixed-size fields are right-padded with spaces
- Padding is applied using `String.format("%-<length>s", value)`
- Empty fields are padded entirely with spaces

### Truncation Rules
- If a field value exceeds its maximum size, it is truncated from the right
- Truncation is applied using `value.substring(0, length)`

### Name Formatting
The patient name field requires special formatting:

```java
// Input: "John Doe"
// Output: "Doe, John"

// Input: "Smith, Jane"
// Output: "Smith, Jane" (no change)

// Input: "Madonna"
// Output: "Madonna" (no change)
```

## LabOrderData Mapping

The RS232 segments map to `LabOrderData` fields as follows:

| Segment | LabOrderData Field | Getter Method |
|---------|-------------------|---------------|
| `mt` | Static "mpr" | N/A |
| `pi` | `mrn` | `getMrn()` |
| `pn` | `patientName` | `getPatientName()` |
| `pl` | External config | N/A |
| `si` | Reserved | N/A |
| `ci` | `sampleId` | `getSampleId()` |

## Usage in Code

### Creating Messages

```java
// Using RS232Message class
List<LabOrderData> orders = getLabOrders();
RS232Message message = RS232Message.fromLabOrderData(orders);

// With patient location
RS232Message message = RS232Message.fromLabOrderData(orders, "WARD-A");

// Using RS232DataWriter
RS232DataWriter writer = new RS232DataWriter();
writer.sendPipeDelimitedMessage(orders, serialPort);

// With patient location
writer.sendPipeDelimitedMessage(orders, serialPort, "WARD-A");
```

### Sample Code Output

```java
LabOrderData order = new LabOrderData();
order.setSampleId("SAMPLE001");
order.setMrn("MRN1234567890");
order.setPatientName("John Doe");

// Results in:
// mtmpr|piMRN1234567890   |pnDoe, John                               |pl|si|ciSAMPLE001           
```

## Validation Rules

1. **Required Fields**: mt, pi, pn, ci must have values
2. **Size Compliance**: Fixed-size fields must be exactly the specified length (with padding)
3. **Format Compliance**: Patient name must be in "LastName, FirstName" format
4. **Character Encoding**: ASCII encoding (standard RS232)

## Testing

Use the provided demo to test message formatting:

```bash
# Run the demo
java -cp target/classes com.writer.RS232MessageDemo

# Test specific modes
java -cp target/classes com.writer.RS232MessageDemo pipe
```

## BCI Software Integration

The `pl` (Patient Location) field should be configured based on the worklist page of the BCI software:

1. Check the BCI software worklist
2. Identify the patient location field
3. Configure the location value in the data provider
4. Pass the location value when creating messages

If the location is not available or not required, leave the field empty (just the segment separator will be present).

## Serial Port Settings

- **Baud Rate**: 9600
- **Data Bits**: 8
- **Stop Bits**: 1
- **Parity**: None
- **Flow Control**: None

## Error Handling

- **Missing MRN**: Empty string, padded to 16 spaces
- **Missing Patient Name**: Empty string, padded to 40 spaces
- **Missing Sample ID**: Empty string, padded to 20 spaces
- **Oversized Values**: Truncated to maximum field size
- **Null Values**: Treated as empty strings

## Notes

1. The `si` segment is reserved for future use and should always be empty
2. The `pl` segment is variable length and should be verified against BCI software requirements
3. All fixed-size fields include their padding in transmission
4. Field separators (`|`) are always present, even between empty fields 