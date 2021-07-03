package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class CauseRule {
	/////导致某个状态的原因规则
	private String state="";   ///状态
	private int value=-1;    ////对应的值
	private List<RuleNode> stateCausingRules=new ArrayList<RuleNode>();   ////对应的规则
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public List<RuleNode> getStateCausingRules() {
		return stateCausingRules;
	}
	public void setStateCausingRules(List<RuleNode> stateCausingRules) {
		this.stateCausingRules = stateCausingRules;
	}
	
}
