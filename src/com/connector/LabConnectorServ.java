package com.connector;

import com.reader.LabConnectUtil;

public class LabConnectorServ {


	static LabConnectorThread connectorThread = new LabConnectorThread();

	public static void start(String[] args) {
		try{
			log("start() ... called");

			connectorThread.run();
			
			// Log data writer status
			if (LabConnectorThread.isDataWriterEnabled()) {
				log("ASTM Data Writer Service: ENABLED");
			} else {
				log("ASTM Data Writer Service: DISABLED");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void stop(String[] args) {
		log("stop()... called");
		connectorThread.closePort();
	}
	public static void main(String[] args) {
		start(args);
	}

	public static void log(String message) {
		LabConnectUtil.log(message);
	}

}
