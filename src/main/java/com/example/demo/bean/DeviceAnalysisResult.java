package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class DeviceAnalysisResult {
	///////设备分析结果
	private String deviceName="";  ////设备名
	private List<ConflictReason> conflictReasons=new ArrayList<>();   ////是否有冲突及原因
	private List<JitterReason> jitterReasons=new ArrayList<>();    /////是否有抖动及原因
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public List<ConflictReason> getConflictReasons() {
		return conflictReasons;
	}
	public void setConflictReasons(List<ConflictReason> conflictReasons) {
		this.conflictReasons = conflictReasons;
	}
	public List<JitterReason> getJitterReasons() {
		return jitterReasons;
	}
	public void setJitterReasons(List<JitterReason> jitterReasons) {
		this.jitterReasons = jitterReasons;
	}
	
}
