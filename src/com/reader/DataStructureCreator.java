package com.reader;
import com.reader.MachineCodeForSampleID;
import com.reader.MachineCodeWithValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataStructureCreator {
	
	public static MachineCodeForSampleID createDataStructure(String sampleID, String sampleDate, String isolationNumber, String organismName, List machineCodeWithValueList) {
		MachineCodeForSampleID machineCodeForSampleID = null;
		if (sampleID != null && sampleID != "") {
			machineCodeForSampleID = new MachineCodeForSampleID();
			machineCodeForSampleID.setSampleID(sampleID);
			machineCodeForSampleID.setDate(sampleDate);

			List<IsolationInfoForSampleID> isolationInfoForSampleIDList = new ArrayList<>();
			IsolationInfoForSampleID isolationInfoForSampleID = new IsolationInfoForSampleID();
			isolationInfoForSampleID.setIsolationNumber(isolationNumber);
			isolationInfoForSampleID.setOrganismName(organismName);
			isolationInfoForSampleID.setIsolationDate("");
			isolationInfoForSampleID.setQuantity("");
			isolationInfoForSampleIDList.add(isolationInfoForSampleID);

			isolationInfoForSampleID.setMachineCodeWithValueList(machineCodeWithValueList);

			machineCodeForSampleID.setIsolationInfoForSampleIDList(isolationInfoForSampleIDList);
		}
		return machineCodeForSampleID;
	}

	public static  MachineCodeWithValue createMachineCodeWithValueObj(String code, String value, String interpretation, String unit){
		MachineCodeWithValue machineCodeWithValue = new MachineCodeWithValue();
		machineCodeWithValue.setMachineCode(code);
		machineCodeWithValue.setValue(value);
		machineCodeWithValue.setInterpretation(interpretation);
		machineCodeWithValue.setUnit(unit);
		return machineCodeWithValue;
	}

	public static  IsolationInfoForSampleID isolationInfoObj(String isolationNumber, String organismName, String quantity, String isolationDate){
		IsolationInfoForSampleID isolationInfo= new IsolationInfoForSampleID();
		isolationInfo.setIsolationNumber(isolationNumber);
		isolationInfo.setOrganismName(organismName);
		isolationInfo.setQuantity(quantity);
		isolationInfo.setIsolationDate(isolationDate);
		return isolationInfo;
	}
}
