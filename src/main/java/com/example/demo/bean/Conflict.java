package com.example.demo.bean;


import java.util.ArrayList;
import java.util.List;

public class Conflict {
	////状态冲突
	private double time;   ///冲突时间
	private List<Integer> conflictValues=new ArrayList<>();   //////冲突的值（对应状态）
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public List<Integer> getConflictValues() {
		return conflictValues;
	}
	public void setConflictValues(List<Integer> conflictValues) {
		this.conflictValues = conflictValues;
	} 
}
