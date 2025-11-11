package com.reader;

import java.util.ArrayList;
import java.util.List;

public class IsolationInfoForSampleID {
    private String isolationNumber = "";
    private String organismName = "";
    private String quantity = "";
    private String isolationDate = "";

    private List<MachineCodeWithValue> machineCodeWithValueList = new ArrayList<MachineCodeWithValue>();

    public String getIsolationNumber() {
        return isolationNumber;
    }

    public void setIsolationNumber(String isolationNumber) {
        this.isolationNumber = isolationNumber;
    }

    public String getOrganismName() {
        return organismName;
    }

    public void setOrganismName(String organismName) {
        this.organismName = organismName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getIsolationDate() {
        return isolationDate;
    }

    public void setIsolationDate(String isolationDate) {
        this.isolationDate = isolationDate;
    }

    public List<MachineCodeWithValue> getMachineCodeWithValueList() {
        return machineCodeWithValueList;
    }

    public void setMachineCodeWithValueList(List<MachineCodeWithValue> machineCodeWithValueList) {
        this.machineCodeWithValueList = machineCodeWithValueList;
    }
}
