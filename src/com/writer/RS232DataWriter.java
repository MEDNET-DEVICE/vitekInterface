package com.writer;

import java.util.List;
import jssc.SerialPort;
import jssc.SerialPortException;
import com.reader.LabConnectUtil;

/**
 * RS232 Data Writer class for sending simple RS232 messages to serial port
 * without ASTM protocol overhead
 */
public class RS232DataWriter {

    private boolean isConnected = false;

    // Simple RS232 Control Characters
    private static final byte STX = 0x02;  // Start of Text
    private static final byte ETX = 0x03;  // End of Text
    private static final byte CR = 0x0D;   // Carriage Return
    private static final byte LF = 0x0A;   // Line Feed
    private static final byte ENQ = 0x05;  // Enquiry
    private static final byte ACK = 0x06;  // Acknowledgement
    private static final byte EOT = 0x04;  // End of Transmission

    public RS232DataWriter() {
    }

    /**
     * Send RS232 message to serial port
     * @param rs232Message Complete RS232 message
     * @return true if message sent successfully
     */
    public boolean sendRS232Message(RS232Message rs232Message, SerialPort serialPort) {
        if (serialPort == null) {
            log("RS232DataWriter: Port not found");
            return false;
        }

        if (rs232Message == null) {
            log("RS232DataWriter: RS232 message is null");
            return false;
        }

        try {
            // Get RS232 formatted strings
            List<String> messageLines = rs232Message.getMessageLines();

            if (messageLines == null || messageLines.isEmpty()) {
                log("RS232DataWriter: No message lines to send");
                return false;
            }

            log("RS232DataWriter: Starting RS232 transmission with " + messageLines.size() + " lines");

            // Send each message line
            for (int i = 0; i < messageLines.size(); i++) {
                String messageLine = messageLines.get(i);
                log("RS232DataWriter: Sending line " + (i + 1) + ": " + messageLine);

                if (!sendRS232Line(messageLine, serialPort)) {
                    log("RS232DataWriter: Failed to send line " + (i + 1));
                    return false;
                }

                // Small delay between lines
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // Continue
                }
            }

            log("RS232DataWriter: RS232 transmission completed successfully");
            return true;

        } catch (Exception ex) {
            log("RS232DataWriter: Error during RS232 transmission: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Send individual RS232 line
     * @param messageLine RS232 message line
     */
    private boolean sendRS232Line(String messageLine, SerialPort serialPort) throws SerialPortException {
        // Create simple frame: STX + messageLine + ETX + CR + LF
        StringBuilder frameBuilder = new StringBuilder();
        frameBuilder.append((char) ENQ);
        frameBuilder.append((char) ACK);
        frameBuilder.append((char) STX);
        frameBuilder.append(messageLine);
        frameBuilder.append((char) ACK);
        frameBuilder.append((char) ETX);
        frameBuilder.append((char) EOT);
//                frameBuilder.append((char) CR);
//                frameBuilder.append((char) LF);

        String completeFrame = frameBuilder.toString();
        log("RS232DataWriter: Sending frame: " + completeFrame.replace("\r", "\\r").replace("\n", "\\n"));

        // Send frame
        serialPort.writeBytes(completeFrame.getBytes());

        return true;
    }

    /**
     * Send simple string without protocol framing (for basic RS232 communication)
     * @param message Simple message string
     */
    public boolean sendSimpleMessage(String message, SerialPort serialPort) {
        if (serialPort == null) {
            log("RS232DataWriter: Port not found");
            return false;
        }

        if (message == null || message.trim().isEmpty()) {
            log("RS232DataWriter: Message is null or empty");
            return false;
        }

        try {
            log("RS232DataWriter: Sending simple message: " + message);

            // Send message with just CR+LF termination
            String messageWithTermination = message + (char) CR + (char) LF;
            serialPort.writeBytes(messageWithTermination.getBytes());

            log("RS232DataWriter: Simple message sent successfully");
            return true;

        } catch (Exception ex) {
            log("RS232DataWriter: Error sending simple message: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Send pipe-delimited message with specific segment format
     * Format: mt(3)|pi(16)|pn(40)|pl|si|ci(20)
     * @param labOrderDataList List of lab order data
     */
    public boolean sendPipeDelimitedMessage(List<LabOrderData> labOrderDataList, SerialPort serialPort) {
        return sendPipeDelimitedMessage(labOrderDataList, serialPort, null);
    }

    /**
     * Send pipe-delimited message with specific segment format
     * Format: mt(3)|pi(16)|pn(40)|pl|si|ci(20)
     * @param labOrderDataList List of lab order data
     * @param plValue Patient location value (verify on worklist page of BCI software)
     */
    public boolean sendPipeDelimitedMessage(List<LabOrderData> labOrderDataList, SerialPort serialPort, String plValue) {
        if (serialPort == null) {
            log("RS232DataWriter: Port not found");
            return false;
        }

        if (labOrderDataList == null || labOrderDataList.isEmpty()) {
            log("RS232DataWriter: No lab order data to send");
            return false;
        }

        try {
            log("RS232DataWriter: Starting RS232 segment transmission for " + labOrderDataList.size() + " orders");

            for (LabOrderData labOrder : labOrderDataList) {
                // Create RS232 segment message: mt|pi|pn|pl|si|ci
                String message = createRS232SegmentMessage(labOrder, plValue);

                log("RS232DataWriter: Sending order message: " + message);

                // Send with STX/ETX framing
                StringBuilder frameBuilder = new StringBuilder();
                frameBuilder.append((char) ENQ);
                frameBuilder.append((char) ACK);
                frameBuilder.append((char) STX);
                frameBuilder.append(message);
                frameBuilder.append((char) ACK);
                frameBuilder.append((char) ETX);
                frameBuilder.append((char) EOT);
//                frameBuilder.append((char) CR);
//                frameBuilder.append((char) LF);

                String completeFrame = frameBuilder.toString();
                serialPort.writeBytes(completeFrame.getBytes());

                // Small delay between orders
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Continue
                }
            }

            log("RS232DataWriter: RS232 segment transmission completed successfully");
            return true;

        } catch (Exception ex) {
            log("RS232DataWriter: Error during RS232 segment transmission: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Create RS232 segment message with required fields
     * Format: mt|pi|pn|pl|si|ss|sp|ci|zz|
     */
    private String createRS232SegmentMessage(LabOrderData labOrder, String plValue) {
        StringBuilder lineBuilder = new StringBuilder();

        // mt - static value "mpr" (size=3)
        lineBuilder.append("mt").append(padOrTruncate("mpr", 3)).append("|");

        // pi - patient MRN from order (size=16)
        String mrn = labOrder.getMrn() != null ? labOrder.getMrn() : "";
        lineBuilder.append("pi").append(padOrTruncate(mrn, 16)).append("|");

        // pn - last name, first name from order (size=40)
        String patientName = formatPatientName(labOrder.getPatientName());
        lineBuilder.append("pn").append(padOrTruncate(patientName, 40)).append("|");

        // pl - verify on worklist page of BCI software
        String location = plValue != null ? plValue : "";
        lineBuilder.append("pl").append(location).append("|");

        // si - blank but create the element
        lineBuilder.append("si").append("|");

        // ss - sample type from order (specimenType)
        String sampleType = labOrder.getSpecimenType() != null ? labOrder.getSpecimenType() : "";
        lineBuilder.append("ss").append(sampleType).append("|");

        // sp - sample type from order (same as ss)
        lineBuilder.append("sp").append(sampleType).append("|");

        // ci - sample id from order (size=20)
        String sampleId = labOrder.getSampleId() != null ? labOrder.getSampleId() : "";
        lineBuilder.append("ci").append(padOrTruncate(sampleId, 20)).append("|");

        // zz - end of message marker (empty value)
        lineBuilder.append("zz").append("|");

        return lineBuilder.toString();
    }

    /**
     * Format patient name as "LastName, FirstName"
     * Removes configured prefixes before formatting
     */
    private String formatPatientName(String patientName) {
        if (patientName == null || patientName.trim().isEmpty()) {
            return "";
        }

        // Remove configured prefixes (Dr., Mr., Mrs., Baby boy of, etc.)
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

    /**
     * Truncate string if it exceeds max length (no padding)
     */
    private String padOrTruncate(String value, int maxLength) {
        if (value == null) {
            value = "";
        }

        // Truncate if exceeds max length
        if (value.length() > maxLength) {
            return value.substring(0, maxLength);
        }

        // No padding - return as is
        return value;
    }

    /**
     * Close serial port connection
     */
    public void closeConnection(SerialPort serialPort) {
        if (serialPort != null && isConnected) {
            try {
                serialPort.closePort();
                isConnected = false;
                log("RS232DataWriter: Port closed successfully");
            } catch (SerialPortException ex) {
                log("RS232DataWriter: Error closing port: " + ex.getMessage());
            }
        }
    }

    /**
     * Check if connection is active
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Log messages using the existing logging utility
     */
    private void log(String message) {
        LabConnectUtil.log(message);
    }
} 