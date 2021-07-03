package com.example.demo.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Scene {
	/////各个场景分析结果
	private String scenarioName="";   ///场景名	
	private List<DataTimeValue> dataTimeValues=new ArrayList<DataTimeValue>();  ////仿真数据存储格式
	private List<DataTimeValue> triggeredRulesName=new ArrayList<DataTimeValue>();  ////可触发的规则
	private List<String> cannotTriggeredRulesName=new ArrayList<String>();	//////不可触发的规则
	private List<DeviceAnalysisResult> deviceAnalysisResults=new  ArrayList<>();   ////各个设备动态分析结果

	/////key: dataName, value: [time,value]list
	private HashMap<String, List<double[]>> dataTimeValuesHashMap=new HashMap<>();
	////key：dataName, value: [startTime,Value,EndTime,Value]List
	private HashMap<String,List<double[]>> dataStartTimeValueEndTimeValuesHashMap=new HashMap<>();
	public String getScenarioName() {
		return scenarioName;
	}
	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}
	public List<DataTimeValue> getDataTimeValues() {
		return dataTimeValues;
	}
	public void setDataTimeValues(List<DataTimeValue> dataTimeValues) {
		this.dataTimeValues = dataTimeValues;
	}
	public HashMap<String, List<double[]>> getDataTimeValuesHashMap() {
		return dataTimeValuesHashMap;
	}
	public void setDataTimeValuesHashMap(HashMap<String, List<double[]>> dataTimeValuesHashMap) {
		this.dataTimeValuesHashMap = dataTimeValuesHashMap;
	}
	public HashMap<String, List<double[]>> getDataStartTimeValueEndTimeValuesHashMap() {
		return dataStartTimeValueEndTimeValuesHashMap;
	}
	public void setDataStartTimeValueEndTimeValuesHashMap(
			HashMap<String, List<double[]>> dataStartTimeValueEndTimeValuesHashMap) {
		this.dataStartTimeValueEndTimeValuesHashMap = dataStartTimeValueEndTimeValuesHashMap;
	}   
	public List<DataTimeValue> getTriggeredRulesName() {
		return triggeredRulesName;
	}
	public void setTriggeredRulesName(List<DataTimeValue> triggeredRulesName) {
		this.triggeredRulesName = triggeredRulesName;
	}
	public List<String> getCannotTriggeredRulesName() {
		return cannotTriggeredRulesName;
	}
	public void setCannotTriggeredRulesName(List<String> cannotTriggeredRulesName) {
		this.cannotTriggeredRulesName = cannotTriggeredRulesName;
	}
	public List<DeviceAnalysisResult> getDeviceAnalysisResults() {
		return deviceAnalysisResults;
	}
	public void setDeviceAnalysisResults(List<DeviceAnalysisResult> deviceAnalysisResults) {
		this.deviceAnalysisResults = deviceAnalysisResults;
	}
	
}
