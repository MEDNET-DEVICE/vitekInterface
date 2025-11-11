package com.reader;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.ws.rs.core.MediaType;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;

public class LabConnectUtil {

	public static void log(String message) {
		try {
			String logRootDrive =  "/tmp";
			if(System.getProperty("logRootDrive")!=null) {
				logRootDrive = System.getProperty("logRootDrive");
			}
			String propertyFileSuffix = "VITEK2COMPACT";
	
			if(System.getProperty("propertyFileSuffix")!=null) {
				propertyFileSuffix = System.getProperty("propertyFileSuffix");
			}

			File folderPath = new File(logRootDrive + File.separator+"mednet"+File.separator+"log");
			if(!folderPath.exists()){
				folderPath.mkdirs();
			}

			PrintWriter out = new PrintWriter(new FileWriter(logRootDrive + File.separator+"mednet"+File.separator+"log"+File.separator+new SimpleDateFormat("dd-MM-yyyy").format(new Date())+"_"+propertyFileSuffix+"Log.txt", true), true);
			out.write("\r\n" +message);
			out.close();

			System.out.println(message);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
}
