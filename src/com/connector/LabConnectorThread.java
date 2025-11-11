package com.connector;

import java.io.File;
import java.io.FileInputStream;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.cxf.resource.PropertiesResolver;

import jssc.SerialPort;
import jssc.SerialPortException;

import com.reader.DataReader;
import com.writer.DataWriterService;

public class LabConnectorThread  {

	private static SerialPort serialPort;
	private static boolean dataWriterEnabled = false;


	public void run() throws Exception {
		String rootDrive = "D://";
		if(System.getProperty("rootDrive")!=null) {
			rootDrive = System.getProperty("rootDrive");
		}
		String propertyFileSuffix = "VITEK2COMPACT";
		if(System.getProperty("propertyFileSuffix")!=null) {
			propertyFileSuffix = System.getProperty("propertyFileSuffix");
		}
		ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(rootDrive+ File.separator+"mednet"+File.separator+"windowsService"+propertyFileSuffix+".properties"));
		String comPortAddress = bundle.getString("comPort");
		
		// Check if data writer is enabled in configuration
		try {
			String dataWriterEnabledStr = bundle.getString("dataWriterEnabled");
			dataWriterEnabled = "true".equalsIgnoreCase(dataWriterEnabledStr);
		} catch (Exception e) {
			// Default to false if property not found
			dataWriterEnabled = false;
		}
		
		serialPort = new SerialPort(comPortAddress);
		try {
			serialPort.openPort();//Open port
			System.out.println("is port open :::: "+serialPort.getPortName());
			serialPort.setParams(9600, 8, 1, 0);//Set params
			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
			serialPort.setEventsMask(mask);//Set mask
			serialPort.addEventListener(new DataReader.SerialPortReader(serialPort));
			// Initialize data writer service if enabled
			if (dataWriterEnabled) {
				initializeDataWriter(serialPort);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex);
		}
	}

	public void closePort() {
		try{
			// Stop data writer service if it was enabled
			if (dataWriterEnabled) {
				DataWriterService.stop(serialPort);
				System.out.println("Data writer service stopped");
			}
			
			serialPort.closePort();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialize the data writer service
	 */
	private void initializeDataWriter(SerialPort serialPort) {
		try {
			System.out.println("Initializing ASTM Data Writer Service...");
			boolean initialized = DataWriterService.initialize();
			
			if (initialized) {
				// Start the data writer service
				boolean started = DataWriterService.start(serialPort);
				if (started) {
					System.out.println("ASTM Data Writer Service started successfully");
				} else {
					System.out.println("Failed to start ASTM Data Writer Service");
				}
			} else {
				System.out.println("Failed to initialize ASTM Data Writer Service");
			}
		} catch (Exception ex) {
			System.out.println("Error initializing data writer: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	/**
	 * Get the data writer service for external access
	 * This allows other components to add lab order data for writing
	 */
	public static boolean isDataWriterEnabled() {
		return dataWriterEnabled;
	}


}

