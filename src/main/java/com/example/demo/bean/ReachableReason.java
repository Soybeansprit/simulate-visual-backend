package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class ReachableReason {
	///property可达原因
	private double[] satisfyIntervalTime;  ////满足的区间
	private String scenarioName="";
	private List<CauseRule> causingRules=new ArrayList<CauseRule>();
	public double[] getSatisfyIntervalTime() {
		return satisfyIntervalTime;
	}
	public void setSatisfyIntervalTime(double[] satisfyIntervalTime) {
		this.satisfyIntervalTime = satisfyIntervalTime;
	}
	public List<CauseRule> getCausingRules() {
		return causingRules;
	}
	public void setCausingRules(List<CauseRule> causingRules) {
		this.causingRules = causingRules;
	}
	public String getScenarioName() {
		return scenarioName;
	}
	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}
	
}
