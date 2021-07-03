package com.example.demo.bean;

public class SensorType extends Entity{
	public String attribute="";  ///传感器检测什么属性
	public String style="";   ///该属性的类型
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
}
