package com.writer;

import java.util.List;
import jssc.SerialPort;
import jssc.SerialPortException;
import com.reader.LabConnectUtil;

/**
 * Data Writer class for sending ASTM protocol messages to serial port
 */
public class DataWriter {
    
    private boolean isConnected = false;
    
    // ASTM Protocol Control Characters
    private static final byte STX = 0x02;  // Start of Text
    private static final byte ETX = 0x03;  // End of Text
    private static final byte EOT = 0x04;  // End of Transmission
    private static final byte ENQ = 0x05;  // Enquiry
    private static final byte ACK = 0x06;  // Acknowledge
    private static final byte NAK = 0x15;  // Negative Acknowledge
    private static final byte ETB = 0x17;  // End of Transmission Block
    private static final byte LF = 0x0A;   // Line Feed
    private static final byte CR = 0x0D;   // Carriage Return
    
    public DataWriter() {
    }

    
    /**
     * Send ASTM message to serial port
     * @param astmMessage Complete ASTM message
     * @return true if message sent successfully
     */
    public boolean sendASTMMessage(ASTMMessage astmMessage, SerialPort serialPort) {
        if (serialPort == null) {
            log("DataWriter: Port not found");
            return false;
        }

        if (astmMessage == null) {
            log("DataWriter: ASTM message is null");
            return false;
        }
        
        try {
            // Get ASTM formatted strings
            List<String> astmStrings = astmMessage.toASTMStrings();
            
            if (astmStrings.isEmpty()) {
                log("DataWriter: No ASTM strings to send");
                return false;
            }
            
            log("DataWriter: Starting ASTM transmission with " + astmStrings.size() + " records");
            
            // Start transmission with ENQ
            if (!sendENQ(serialPort)) {
                log("DataWriter: Failed to establish communication with ENQ");
                return false;
            }
            
            // Send each ASTM record
            for (int i = 0; i < astmStrings.size(); i++) {
                String astmString = astmStrings.get(i);
                log("DataWriter: Sending record " + (i + 1) + ": " + astmString);
                
                if (!sendASTMRecord(astmString, i + 1, serialPort)) {
                    log("DataWriter: Failed to send record " + (i + 1));
                    return false;
                }
            }
            
            // End transmission with EOT
            if (!sendEOT(serialPort)) {
                log("DataWriter: Failed to end transmission with EOT");
                return false;
            }
            
            log("DataWriter: ASTM transmission completed successfully");
            return true;
            
        } catch (Exception ex) {
            log("DataWriter: Error during ASTM transmission: " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Send ENQ (Enquiry) to establish communication
     */
    private boolean sendENQ(SerialPort serialPort) throws SerialPortException {
        serialPort.writeByte(ENQ);
        log("DataWriter: Sent ENQ");
        
        // Wait for ACK response (simplified - in real implementation, should have timeout)
        try {
            Thread.sleep(100); // Give time for response
        } catch (InterruptedException e) {
            // Continue
        }
        
        return true; // Simplified - should check for ACK response
    }
    
    /**
     * Send individual ASTM record
     * @param astmRecord ASTM formatted record string
     * @param frameNumber Frame number for the record
     */
    private boolean sendASTMRecord(String astmRecord, int frameNumber, SerialPort serialPort) throws SerialPortException {
        // Calculate frame number (0-7, cycling)
        int frame = frameNumber % 8;
        
        // Create complete frame: STX + frame + astmRecord + ETX + checksum + CR + LF
        StringBuilder frameBuilder = new StringBuilder();
        frameBuilder.append((char) STX);
        frameBuilder.append(frame);
        frameBuilder.append(astmRecord);
        frameBuilder.append((char) ETX);
        
        // Calculate checksum
        String frameContent = frame + astmRecord + (char) ETX;
        int checksum = calculateChecksum(frameContent);
        frameBuilder.append(String.format("%02X", checksum));
        frameBuilder.append((char) CR);
        frameBuilder.append((char) LF);
        
        String completeFrame = frameBuilder.toString();
        log("DataWriter: Sending frame: " + completeFrame.replace("\r", "\\r").replace("\n", "\\n"));
        
        // Send frame
        serialPort.writeBytes(completeFrame.getBytes());
        
        // Wait for ACK (simplified)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Continue
        }
        
        return true; // Simplified - should check for ACK response
    }
    
    /**
     * Send EOT (End of Transmission)
     */
    private boolean sendEOT(SerialPort serialPort) throws SerialPortException {
        serialPort.writeByte(EOT);
        log("DataWriter: Sent EOT");
        return true;
    }
    
    /**
     * Calculate ASTM checksum
     * @param data Data to calculate checksum for
     * @return Checksum value
     */
    private int calculateChecksum(String data) {
        int checksum = 0;
        for (char c : data.toCharArray()) {
            checksum += (int) c;
        }
        return checksum & 0xFF; // Keep only the lower 8 bits
    }
    
    /**
     * Close serial port connection
     */
    public void closeConnection(SerialPort serialPort) {
        if (serialPort != null && isConnected) {
            try {
                serialPort.closePort();
                isConnected = false;
                log("DataWriter: Port closed successfully");
            } catch (SerialPortException ex) {
                log("DataWriter: Error closing port: " + ex.getMessage());
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