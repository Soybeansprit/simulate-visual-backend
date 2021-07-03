package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class DataTimeValue {
	/////仿真结果数据格式
	public String name="";  ////////数据名
	public List<double[]> timeValues=new ArrayList<double[]>();   ////(time,value)列表，为仿真结果
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<double[]> getTimeValues() {
		return timeValues;
	}
	public void setTimeValues(List<double[]> timeValues) {
		this.timeValues = timeValues;
	}
}
