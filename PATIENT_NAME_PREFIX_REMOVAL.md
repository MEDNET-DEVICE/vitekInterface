# Patient Name Prefix Removal Feature

## Overview
This feature automatically removes configured prefixes from patient names before sending them in RS232 messages. This is useful for removing titles, honorifics, and other prefixes that should not be included in the device communication.

## How It Works

When a patient name is processed for RS232 transmission:
1. The system checks for configured prefixes at the beginning of the name
2. Any matching prefixes are removed (case-insensitive)
3. The remaining name is formatted as "LastName, FirstName"
4. The formatted name is sent in the RS232 message

## Configuration

### Properties File
Add the following property to your configuration file:
```properties
# File: /opt/mednet/windowsServiceVITEK2COMPACT.properties

# Comma-separated list of prefixes to remove from patient names
patientNamePrefixesToRemove=Baby,Baby boy of,Baby girl of,BABY.,Dr.,MASTER,Master.,MISS.,MR.,MRS,Mrs.,MS,Ms.,MX.
```

### Default Prefixes
If the configuration file is not found or the property is not set, the following default prefixes are used:
- Baby
- Baby boy of
- Baby girl of
- BABY.
- Dr.
- MASTER
- Master.
- MISS.
- MR.
- MRS
- Mrs.
- MS
- Ms.
- MX.

## Examples

### Example 1: Dr. Prefix Removal
```
Input:  "Dr. SOURAV KUMAR"
Output: "KUMAR, SOURAV"

RS232 Message:
mtmpr|piMRN-240900018|pnKUMAR, SOURAV|pl|si|ciMICRO-127
```

### Example 2: Baby boy of Prefix Removal
```
Input:  "Baby boy of Krishna Sharma"
Output: "Sharma, Krishna"

RS232 Message:
mtmpr|piMRN-240900019|pnSharma, Krishna|pl|si|ciMICRO-128
```

### Example 3: Mr. Prefix Removal
```
Input:  "Mr. Robert Brown"
Output: "Brown, Robert"

RS232 Message:
mtmpr|piMRN-240900020|pnBrown, Robert|pl|si|ciMICRO-129
```

### Example 4: Multiple Prefixes
```
Input:  "MASTER John Smith"
Output: "Smith, John"

RS232 Message:
mtmpr|piMRN-240900021|pnSmith, John|pl|si|ciMICRO-130
```

### Example 5: No Prefix
```
Input:  "John Doe"
Output: "Doe, John"

RS232 Message:
mtmpr|piMRN-240900022|pnDoe, John|pl|si|ciMICRO-131
```

### Example 6: Already Formatted
```
Input:  "SOURAV KUMAR, MR."
Output: "SOURAV KUMAR, MR."
(No change - prefix is at the end, not beginning)

RS232 Message:
mtmpr|piMRN-240900023|pnSOURAV KUMAR, MR.|pl|si|ciMICRO-132
```

## Prefix Matching Rules

1. **Case-Insensitive**: Prefixes match regardless of case
   - "dr." matches "Dr.", "DR.", "dr"
   
2. **Start of Name Only**: Prefixes are only removed from the beginning
   - "Dr. Smith" → "Smith"
   - "Smith Dr." → "Smith Dr." (no change)

3. **Longest Match First**: Longer prefixes are checked before shorter ones
   - "Baby boy of" is checked before "Baby"
   - Prevents incorrect partial matches

4. **Multiple Prefix Removal**: System can remove multiple prefixes in sequence
   - Keeps removing until no more matches found

5. **Whitespace Handling**: Leading/trailing spaces are automatically trimmed

## Implementation Details

### Class: PatientNameConfig

Located at: `src/com/writer/PatientNameConfig.java`

**Key Methods:**
- `initialize()` - Loads prefixes from properties file
- `removePrefixes(String name)` - Removes matching prefixes from name
- `getPrefixesToRemove()` - Returns list of configured prefixes
- `reload()` - Reloads configuration (useful for runtime updates)

**Features:**
- Thread-safe singleton pattern
- Automatic fallback to defaults if config not found
- Logging of prefix removal operations
- Configurable via properties file

### Integration

The prefix removal is integrated into:
1. **RS232Message.java** - formatPatientName() method
2. **RS232DataWriter.java** - formatPatientName() method
3. **RS232MessageDemo.java** - formatPatientName() method

## Testing

### Run Prefix Removal Test
```bash
java -cp target/classes com.writer.PatientNamePrefixTest
```

### Sample Output
```
=== Patient Name Prefix Removal Test ===

Loaded 14 prefixes to remove

Testing prefix removal and name formatting:

Input Name                               | Output (Formatted)                      
-----------------------------------------+-----------------------------------------
Baby John Doe                            | Doe, John                               
Baby boy of Mary Smith                   | Smith, Mary                             
Baby girl of Sarah Johnson               | Johnson, Sarah                          
Dr. John Smith                           | Smith, John                             
Mr. Robert Brown                         | Brown, Robert                           
Mrs. Jennifer Davis                      | Davis, Jennifer                         
Ms. Emily Wilson                         | Wilson, Emily                           
MASTER Thomas Anderson                   | Anderson, Thomas                        
MISS. Rebecca Miller                     | Miller, Rebecca                         
MX. Alex Taylor                          | Taylor, Alex                            
SOURAV KUMAR, MR.                        | SOURAV KUMAR, MR.                       
Dr. SOURAV KUMAR                         | KUMAR, SOURAV                           
Baby boy of Krishna Sharma               | Sharma, Krishna                         
John Doe                                 | Doe, John                               
Smith, Jane                              | Smith, Jane                             

=== Test Complete ===
```

### Run Demo with Prefix Examples
```bash
java -cp target/classes com.writer.RS232MessageDemo pipe
```

## Adding New Prefixes

### Via Properties File
Edit your properties file and add new prefixes to the comma-separated list:
```properties
patientNamePrefixesToRemove=Baby,Baby boy of,Baby girl of,BABY.,Dr.,MASTER,Master.,MISS.,MR.,MRS,Mrs.,MS,Ms.,MX.,Prof.,Rev.,Sir,Lady
```

### At Runtime
If you need to reload the configuration without restarting:
```java
PatientNameConfig.reload();
```

## Troubleshooting

### Issue: Prefix not being removed
**Possible Causes:**
1. Prefix is not at the beginning of the name
2. Prefix not in configuration list
3. Case sensitivity issue (shouldn't happen - matching is case-insensitive)

**Solution:**
- Check logs for "PatientNameConfig: Removed prefix" messages
- Verify prefix is in properties file
- Ensure prefix is at start of name

### Issue: Configuration not loading
**Possible Causes:**
1. Properties file not found at expected location
2. Property key misspelled
3. File permissions

**Solution:**
- Check logs for "PatientNameConfig: Loaded prefixes" messages
- Verify file path: `/opt/mednet/windowsServiceVITEK2COMPACT.properties`
- Check file permissions
- System will fall back to default prefixes

### Issue: Wrong prefix removed
**Possible Causes:**
1. Overlapping prefix definitions

**Solution:**
- System automatically handles this by checking longest prefixes first
- Review your prefix list for overlaps

## Logging

The system logs all prefix removal operations:

```
PatientNameConfig: Loaded 14 default prefixes
PatientNameConfig: Config file not found, using default prefixes
PatientNameConfig: Removed prefix 'Dr.' from name
PatientNameConfig: Removed prefix 'Baby boy of' from name
```

Logs are written to: `/tmp/mednet/log/dd-MM-yyyy_VITEK2COMPACTLog.txt`

## Performance

- Prefix matching is optimized for performance
- Configuration is loaded once at startup
- Minimal overhead per name processing (< 1ms)
- Thread-safe for concurrent operations

## Best Practices

1. **Keep prefix list current**: Update as new naming patterns are discovered
2. **Order matters for overlaps**: Longer prefixes should come before shorter ones in the config
3. **Test thoroughly**: Use PatientNamePrefixTest to verify behavior
4. **Monitor logs**: Check for unexpected removals
5. **Document custom prefixes**: If you add custom prefixes, document why

## Security Considerations

- Configuration file should have appropriate permissions
- Changes to prefix list affect all messages
- Test changes in non-production environment first
- Keep audit trail of configuration changes

## Future Enhancements

Potential future improvements:
1. Support for suffix removal (e.g., "Jr.", "Sr.", "III")
2. Regular expression support for complex patterns
3. Per-facility prefix configuration
4. Dynamic prefix learning from historical data 