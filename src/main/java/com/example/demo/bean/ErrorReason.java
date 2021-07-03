package com.example.demo.bean;

public class ErrorReason {
	////用于静态分析
	public Rule rule=new Rule();  ///错误的规则
	public String reason="";  /////规则错误原因
	public Rule getRule() {
		return rule;
	}
	public void setRule(Rule rule) {
		this.rule = rule;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
}
