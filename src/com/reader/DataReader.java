package com.reader;

import java.io.*;
import java.util.*;
import javax.ws.rs.core.MediaType;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;

public class DataReader {

	private static SerialPort serialPort;
	private static String readString = "";
	public static String partialResult = "";
	private static String machineID = "VITEK2COMPACT";
	private static String sampleID = "";
	private static String sampleDate = "";
	private static String antibioticCode = "";
	private static String testResult = "";
	private static String testInterpretation = "";
	private static String isolationNumber = "";
	private static String organismName = "";
	private static ArrayList<MachineCodeWithValue> machineCodeWithValueList = new ArrayList<>();

	public static class SerialPortReader implements SerialPortEventListener {

		public SerialPortReader(SerialPort port) {
			  serialPort = port;
		}

		public void serialEvent(SerialPortEvent event) {
				log("Here Serial Event Called ");
	        	if(event.isRXCHAR()){
	                if(event.getEventValue() > 0 ){
	                    try {
	                    	log("in reader..!");
		                    byte bites[] = serialPort.readBytes();
		                    for(int i=0; i < bites.length; i++) {
								readString = readString + (char) bites[i];
								log("what i am getting:: "+readString);
								if (bites[i] == 05) {
									serialPort.writeInt(06);
									log("Inside ENQ BLOCK");
								}
								if (bites[i] == 06) {
									log("Inside ACK BLOCK");
								}
								if (bites[i] == 10) {
									serialPort.writeInt(06);
									log("Inside LF BLOCK");
								}
								if (bites[i] == 29) {
									serialPort.writeInt(06);
									log("Inside GS BLOCK");
								}
								if (bites[i] == 04) {
									log("Inside EOT BLOCK");
								}

								//-------------------------Logic---------------------------//

								if (bites[i] == 02) {
									partialClearData();
								}

								if(bites[i]!=30) {
									partialResult = partialResult + (char) bites[i];
								}

								if (bites[i] == 03) {
									String[] piStrArr = partialResult.split("\\|");

									if(piStrArr.length>0){
										for(int j=0; j<piStrArr.length; j++){
											String strObj = piStrArr[j];

											if(strObj.startsWith("ci")){
												strObj = strObj.substring(2);
												sampleID = strObj;
												log("sampleID " + sampleID);
											}

											if(strObj.startsWith("s1")){
												strObj = strObj.substring(2);
												sampleDate = strObj.replaceAll("/","-");
												log("sampleID " + sampleDate);
											}

											if(strObj.startsWith("pl")){
												strObj = strObj.substring(2);
												log("location " + strObj);
											}

											if(strObj.startsWith("ss")){
												strObj = strObj.substring(2);
												log("sample source code " + strObj);
											}

											if(strObj.startsWith("s5")){
												strObj = strObj.substring(2);
												log("sample source name " + strObj);
											}

											if(strObj.startsWith("a1")){
												antibioticCode = "";
												testResult = "";
												testInterpretation = "";
												strObj = strObj.substring(2);
												System.out.print("drugs code " + strObj);
												antibioticCode = strObj.trim();
											}

											if(strObj.startsWith("a2")){
												strObj = strObj.substring(2);
												System.out.print(" --- "+strObj);
											}


											if(strObj.startsWith("a3")){
												strObj = strObj.substring(2);
												System.out.print(" --- " + strObj);
												testResult = strObj.trim();
											}

											if(strObj.startsWith("a4")){
												strObj = strObj.substring(2);
												log(" --- " + strObj);
												testInterpretation = strObj.trim();

												machineCodeWithValueList.add(DataStructureCreator.createMachineCodeWithValueObj(antibioticCode, testResult, testInterpretation,""));
											}

											if(strObj.startsWith("t1")){
												isolationNumber = "";
												organismName = "";
												strObj = strObj.substring(2);
												log("isolated number " + strObj);
												isolationNumber = strObj.trim();
											}

											if(strObj.startsWith("o2")){
												strObj = strObj.substring(2);
												log("organism " + strObj);
												organismName = strObj.trim();
											}

										}
									}

									MachineReadData machineReadData = null;
									List<MachineCodeForSampleID> machineCodeForSampleIDList = null;

									MachineCodeForSampleID machineCodeForSampleID = DataStructureCreator.createDataStructure(sampleID, sampleDate, isolationNumber, organismName, machineCodeWithValueList);

									if(machineCodeForSampleID!=null){
										machineCodeForSampleIDList = new ArrayList<>();
										machineCodeForSampleIDList.add(machineCodeForSampleID);
									}

									if(machineCodeForSampleIDList!=null && machineCodeForSampleIDList.size()>0) {
										machineReadData = new MachineReadData();
										machineReadData.setMachineID(machineID);
										machineReadData.setMachineCodeForSampleList(machineCodeForSampleIDList);
									}

									if(machineReadData!=null){
										webServiceCall(machineReadData);
										log("webservice Called");
										clearData();
									}
								}
							}
	                  }	catch (SerialPortException spe) {
	                	  log(spe.toString());
	                  }	catch (Exception ex) { 
	                	  log(ex.toString());
	                  }
	            	}
	        	}else if (event.isCTS()) {// If CTS line has changed state
	        		if (event.getEventValue() == 1) {// If line is ON
	        			log("CTS - ON");
	        		}else {
						log("CTS - OFF");
					}
	        	}else if (event.isDSR()) {// /If DSR line has changed state
	        		if (event.getEventValue() == 1) {// If line is ON
						log("DSR - ON");
	        		}else {
						log("DSR - OFF");
	        		}
	        	}
	        }
	}

	public static void webServiceCall(MachineReadData machineReadData) throws Exception {

			String rootDrive = "D://";
			if(System.getProperty("rootDrive")!=null) {
				rootDrive = System.getProperty("rootDrive");
			}
			String propertyFileSuffix = "VITEK2COMPACT";
			if(System.getProperty("propertyFileSuffix")!=null) {
				propertyFileSuffix = System.getProperty("propertyFileSuffix");
			}
			ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(rootDrive+ File.separator+"mednet"+File.separator+"windowsService"+propertyFileSuffix+".properties"));
			String url = bundle.getString("serverIpAddress");
			ObjectMapper result = new ObjectMapper();
			WebClient resultclient = WebClient.create(url);
			WebClient.getConfig(resultclient).getHttpConduit().getClient().setReceiveTimeout(15000);
			WebClient.getConfig(resultclient);
			String jsons = "";
			resultclient.type(MediaType.APPLICATION_JSON);
			resultclient.accept(MediaType.APPLICATION_JSON);
			jsons = result.writeValueAsString(machineReadData);
			log("JSON :" + jsons);
			resultclient.post(jsons, String.class);
			jsons = "";
	}

	public static void clearData(){
		log("In clear data");
		readString = "";
		partialResult = "";
		sampleID = "";
		sampleDate = "";
		antibioticCode = "";
		testResult = "";
		testInterpretation = "";
		isolationNumber = "";
		organismName = "";
		machineCodeWithValueList.clear();
	}

	public static void partialClearData(){
		log("In partial clear data");
		partialResult = "";
		sampleID = "";
		sampleDate = "";
		antibioticCode = "";
		testResult = "";
		testInterpretation = "";
		isolationNumber = "";
		organismName = "";
		machineCodeWithValueList.clear();
	}

	public static void log(String message) {
		LabConnectUtil.log(message);
	}
}
