package com.example.demo.bean;

public class Attribute {
	private String content="";  ///内容 temperature'==dtemper   更改了模型，添加了一个Attribute模型，用来表示各个属性的变化
	private String attribute="";  ////temperature
	private String delta="";  ////dtemper
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public String getDelta() {
		return delta;
	}
	public void setDelta(String delta) {
		this.delta = delta;
	}
	
}
