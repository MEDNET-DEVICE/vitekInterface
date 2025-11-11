package com.reader;

import java.util.ArrayList;
import java.util.List;

public class MachineCodeForSampleID {
	private String sampleID;
	private String date;
	private List<MachineCodeWithValue> machineCodeWithValueList = new ArrayList<MachineCodeWithValue>();
	private List<IsolationInfoForSampleID> isolationInfoForSampleIDList = new ArrayList<IsolationInfoForSampleID>();

	public String getSampleID() {
		return sampleID;
	}

	public void setSampleID(String sampleID) {
		this.sampleID = sampleID;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public List<MachineCodeWithValue> getMachineCodeWithValueList() {
		return machineCodeWithValueList;
	}

	public void setMachineCodeWithValueList(List<MachineCodeWithValue> machineCodeWithValueList) {
		this.machineCodeWithValueList = machineCodeWithValueList;
	}

	public List<IsolationInfoForSampleID> getIsolationInfoForSampleIDList() {
		return isolationInfoForSampleIDList;
	}

	public void setIsolationInfoForSampleIDList(List<IsolationInfoForSampleID> isolationInfoForSampleIDList) {
		this.isolationInfoForSampleIDList = isolationInfoForSampleIDList;
	}
}
