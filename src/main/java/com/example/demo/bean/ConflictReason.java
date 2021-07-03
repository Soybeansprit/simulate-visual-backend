package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class ConflictReason {
	/////冲突即原因
	private Conflict conflict=new Conflict();  /////某个冲突
	private List<CauseRule> causingRules=new ArrayList<>();   //////原因
	public Conflict getConflict() {
		return conflict;
	}
	public void setConflict(Conflict conflict) {
		this.conflict = conflict;
	}
	public List<CauseRule> getCausingRules() {
		return causingRules;
	}
	public void setCausingRules(List<CauseRule> causingRules) {
		this.causingRules = causingRules;
	}

	
}
