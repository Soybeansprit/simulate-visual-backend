package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class RuleNode {
	/////用于locate error ，当前规则和会引发这个规则的其他规则
	private Rule rule=new Rule();   //////当前规则
	private List<RuleNode> preRules=new ArrayList<RuleNode>();    ////会触发该规则的规则
	public Rule getRule() {
		return rule;
	}
	public void setRule(Rule rule) {
		this.rule = rule;
	}
	public List<RuleNode> getPreRules() {
		return preRules;
	}
	public void setPreRules(List<RuleNode> preRules) {
		this.preRules = preRules;
	}
	public void setPreRules(int i,RuleNode preRule) {
		this.preRules.set(i, preRule);
	}
	
	@Override
	public String toString() {
		StringBuilder preRules=new StringBuilder();
		for(RuleNode ruleNode:this.preRules) {
			preRules.append(ruleNode.getRule().getRuleName()+" ");
		}
		return this.rule.getRuleName()+"<---"+preRules.toString();
	}
	
	////删除某个pre
	public void removePreRule(String ruleName) {
		for(RuleNode ruleNode:this.preRules) {
			if(ruleNode.getRule().getRuleName().equals(ruleName)) {
				this.preRules.remove(ruleNode);
				break;
			}
		}
	}
	
//	@Override
//	public boolean equals(Object obj) {
//		if(obj instanceof RuleNode) {
//			RuleNode otherRuleNode=(RuleNode) obj;
//			if(otherRuleNode.getRule().getRuleName().equals(otherRuleNode.getRule().getRuleName())) {
//				return true;
//			}
//		}
//		return false;
//	}
}
