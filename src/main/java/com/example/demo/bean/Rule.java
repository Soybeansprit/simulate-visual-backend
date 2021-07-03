package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class Rule {

	private String ruleName="";   ///规则名序号  rulei
	private String ruleContent="";  ///规则内容
	private List<String> trigger=new ArrayList<String>();  ////所有triggers
	private List<String> action=new ArrayList<String>();   ////所有actions
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public String getRuleContent() {
		return ruleContent;
	}
	public void setRuleContent(String ruleContent) {
		this.ruleContent = ruleContent;
	}
	public List<String> getTrigger() {
		return trigger;
	}
	public void setTrigger(List<String> trigger) {
		this.trigger = trigger;
	}
	public List<String> getAction() {
		return action;
	}
	public void setAction(List<String> action) {
		this.action = action;
	}
	
	public boolean contentEquals(Rule rule) {
		if(ruleName.equals(rule.getRuleName())) {
			return true;
		}
		if(ruleContent.equals(rule.getRuleContent())) {
			return true;
		}
		if(trigger.size()==rule.getTrigger().size()&&action.size()==rule.getAction().size()) {
			boolean equal=true;
			for(String tri:trigger) {
				boolean exist=false;
				for(String tri2:rule.getTrigger()) {
					if(tri.equals(tri2)) {
						exist=true;
						break;
					}
				}
				if(!exist) {
					equal=false;
					break;
				}
			}
			if(equal) {
				for(String act:action) {
					boolean exist=false;
					for(String act2:rule.getAction()) {
						if(act.equals(act2)) {
							exist=true;
							break;
						}
					}
					if(!exist) {
						equal=false;
						break;
					}
				}
			}
			if(equal) {
				return true;
			}
		}
		return false;
	}
}
