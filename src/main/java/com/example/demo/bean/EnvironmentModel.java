package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentModel {
	
	private List<DeviceDetail> devices=new ArrayList<DeviceDetail>();
	private List<SensorType> sensors=new ArrayList<SensorType>();
	private List<BiddableType> biddables=new ArrayList<BiddableType>();
	private List<DeviceType> deviceTypes=new ArrayList<>();
	/////添加Attribute
	private List<Attribute> attributes=new ArrayList<>();

	public List<Attribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}
	public List<DeviceType> getDeviceTypes() {
		return deviceTypes;
	}
	public void setDeviceTypes(List<DeviceType> deviceTypes) {
		this.deviceTypes = deviceTypes;
	}
	public List<DeviceDetail> getDevices() {
		return devices;
	}
	public void setDevices(List<DeviceDetail> devices) {
		this.devices = devices;
	}
	public List<SensorType> getSensors() {
		return sensors;
	}
	public void setSensors(List<SensorType> sensors) {
		this.sensors = sensors;
	}
	public List<BiddableType> getBiddables() {
		return biddables;
	}
	public void setBiddables(List<BiddableType> biddables) {
		this.biddables = biddables;
	}
	public EnvironmentModel() {
		super();
	}
	
	
}
