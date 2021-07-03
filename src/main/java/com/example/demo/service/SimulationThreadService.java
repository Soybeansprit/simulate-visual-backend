package com.example.demo.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.bean.DataTimeValue;
import com.example.demo.bean.DeviceDetail;
import com.example.demo.bean.Scene;
import com.example.demo.service.DynamicAnalysisService;

public class SimulationThreadService extends Thread{
	//////仿真线程，每个场景为一个线程
	private List<Scene> scenes;
	private List<DeviceDetail> devices;
	private String uppaalPath;
	private String fileNameWithoutSuffix;   ////xml文件去掉后缀名
	private String scenarioNum;   /////场景号
	private String simulateResultFilePath;   /////文件存放位置
	private String modelFilePath;
	
	
	public SimulationThreadService(List<Scene> scenes, List<DeviceDetail> devices, String uppaalPath,
			String fileNameWithoutSuffix, String scenarioNum, String modelFilePath,String simulateResultFilePath) {
		super();
		this.scenes = scenes;
		this.devices = devices;
		this.uppaalPath = uppaalPath;
		this.fileNameWithoutSuffix = fileNameWithoutSuffix;
		this.scenarioNum = scenarioNum;
		this.modelFilePath = modelFilePath;
		this.simulateResultFilePath=simulateResultFilePath;
	}



	@Override
	public void run() {
		String newModelFileName=fileNameWithoutSuffix+"-scenario-"+scenarioNum+".xml";
		String resultFileName=fileNameWithoutSuffix+"-scenario-"+scenarioNum+".txt";
		String simulationResult=DynamicAnalysisService.getSimulationResult(uppaalPath, newModelFileName, modelFilePath);
		simulationResult=simulationResult.replace("\n[0]:", "");  ///删掉“\n[0]”
		//生成仿真结果数据文件
		for(DeviceDetail device:devices) {
			//获得设备标识符表示，identifier，转为设备名，并给出位置信息和设备类型
			//如 bulb[0] => deviceName=Bulb_0,deviceType=Bulb,location=Lobby
			String identifier=device.getDeviceType().getName().substring(0, 1).toLowerCase()+device.getDeviceType().getName().substring(1)+"["+device.getConstructionNum()+"]";
			simulationResult=simulationResult.replace("\n"+identifier, "\ndeviceName="+device.getDeviceName()+",deviceType="+device.getDeviceType().getName()+",location="+device.getLocation());
		}
		try (FileWriter fr=new FileWriter(simulateResultFilePath+resultFileName);
				PrintWriter pw=new PrintWriter(fr)){
			pw.write(simulationResult);
		}catch(IOException e) {
			e.printStackTrace();
		}
		/////将仿真结果解析成 （数据名，（时间，值）） 的格式
		List<DataTimeValue> dataTimeValues=DynamicAnalysisService.getAllDataTimeValues(simulationResult);
		Scene scene=new Scene();
		scene.setScenarioName("scenario-"+scenarioNum);
		scene.setDataTimeValues(dataTimeValues);
		List<DataTimeValue> triggeredRuleDataTimeValues=new ArrayList<DataTimeValue>();
		List<String> cannotTriggeredRules=new ArrayList<>();
				
		for(DataTimeValue dataTimeValue:dataTimeValues) {
			if(dataTimeValue.getName().contains("rule")) {
				////找到trigger的和cannot triggered rules
				boolean canTriggered=false;
				for(double[] dataTime:dataTimeValue.getTimeValues()) {
					if(dataTime[1]>0.5) {
						///能triggered
						canTriggered=true;
						triggeredRuleDataTimeValues.add(dataTimeValue);
						break;
					}
				}
				if(!canTriggered) {
					cannotTriggeredRules.add(dataTimeValue.getName());
				}
			}
		}
		scene.setCannotTriggeredRulesName(cannotTriggeredRules);
		scene.setTriggeredRulesName(triggeredRuleDataTimeValues);
		////加锁避免出问题
		synchronized (scenes){
			scenes.add(scene);
		}
		System.out.println(Thread.currentThread().getName()+" end.");
		
	}
}
