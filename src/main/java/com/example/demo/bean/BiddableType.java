package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class BiddableType extends Entity{
	
	//////lobby position 1
	public List<String[]> stateAttributeValues =new ArrayList<String[]>();

	public List<String[]> getStateAttributeValues() {
		return stateAttributeValues;
	}

	public void setStateAttributeValues(List<String[]> stateAttributeValues) {
		this.stateAttributeValues = stateAttributeValues;
	}
	
	
}
