package com.reader;
import java.util.ArrayList;
import java.util.List;

public class MachineReadData {
	private String machineID;
	private List<MachineCodeForSampleID> machineCodeForSampleList = new ArrayList<MachineCodeForSampleID>();

	public String getMachineID() {
		return machineID;
	}

	public void setMachineID(String machineID) {
		this.machineID = machineID;
	}

	public List<MachineCodeForSampleID> getMachineCodeForSampleList() {
		return machineCodeForSampleList;
	}

	public void setMachineCodeForSampleList(List<MachineCodeForSampleID> machineCodeForSampleList) {
		this.machineCodeForSampleList = machineCodeForSampleList;
	}
}

