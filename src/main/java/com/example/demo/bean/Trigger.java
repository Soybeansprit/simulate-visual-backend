package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class Trigger {
	public String trigger="";                ///////temperature>=30         //////Bulb_0.bon     ///Person.lobby(人在客厅)
	public String triggerNum="";			 ///////trigger1                //////trigger2       ///trigger3
	public String device="";                 ///////temperature_sensor      //////Bulb_0         ///person_sensor
	public String[] attrVal=new String[3];   ///////temperature >= 30       //////Bulb_0 . bon   ///position = 1
	public List<Rule> rules=new ArrayList<Rule>();
	public String getTrigger() {
		return trigger;
	}
	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}
	public String getTriggerNum() {
		return triggerNum;
	}
	public void setTriggerNum(String triggerNum) {
		this.triggerNum = triggerNum;
	}
	public String[] getAttrVal() {
		return attrVal;
	}
	public void setAttrVal(String[] attrVal) {
		this.attrVal = attrVal;
	}
	public List<Rule> getRules() {
		return rules;
	}
	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.triggerNum+" : "+this.trigger;
	}
}
