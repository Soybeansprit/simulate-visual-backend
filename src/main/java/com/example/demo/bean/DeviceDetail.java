package com.example.demo.bean;


/** ///////////设备细节信息
 *  包括设备名、设备位置信息、设备类型（灯？空调。。。）、
 *  在xml中对应的构造函数（如Bulb_0=Bulb(0)，存Bulb(0),constructionNum=0）
*/
public class DeviceDetail {
	private String deviceName="";   //////设备名  Bulb_0
	private String location="";     //////位置      lobby
	private DeviceType deviceType=new DeviceType();   ///////哪类设备   Bulb
	private int constructionNum=-1;   ///////0  对应uppaal的Bulb_0=Bulb(0);默认未设置-1
	
	public String getDeviceName() {
		return deviceName;
	}
	public DeviceDetail() {
		
	}
	public DeviceDetail(String deviceName, String location, String deviceTypeName) {
		super();
		this.deviceName = deviceName;
		this.location = location;
		this.deviceType.name=deviceTypeName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public DeviceType getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	public int getConstructionNum() {
		return constructionNum;
	}
	public void setConstructionNum(int constructionNum) {
		this.constructionNum = constructionNum;
	}
	@Override
	public String toString() {
		return "DeviceDetail [deviceName=" + deviceName + ", location=" + location + ", deviceType=" + deviceType.name + "]";
	}
	public void setDeviceTypeName(String deviceTypeName) {
		DeviceType deviceType=new DeviceType();
		deviceType.setName(deviceTypeName);
		this.deviceType=deviceType;
	}

	

}
