package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class JitterReason {
	//////抖动及原因
	private List<double[]> jitter=new ArrayList<>();   ///某段抖动  time-value,time-value...
	private List<CauseRule> causingRules=new ArrayList<>();  //////原因
	public List<double[]> getJitter() {
		return jitter;
	}
	public void setJitter(List<double[]> jitter) {
		this.jitter = jitter;
	}
	public List<CauseRule> getCausingRules() {
		return causingRules;
	}
	public void setCausingRules(List<CauseRule> causingRules) {
		this.causingRules = causingRules;
	}

	
}
