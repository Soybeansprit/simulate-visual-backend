package com.example.demo.controller;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.DocumentException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.bean.Attribute;
import com.example.demo.bean.BiddableType;
import com.example.demo.bean.DeviceDetail;
import com.example.demo.bean.DeviceType;
import com.example.demo.bean.EnvironmentModel;
import com.example.demo.bean.Rule;
import com.example.demo.bean.Scene;
import com.example.demo.bean.SensorType;
import com.example.demo.bean.StaticAnalysisResult;
import com.example.demo.bean.IFDGraph.GraphNode;
import com.example.demo.bean.InputConstruct.EnvironmentRule;
import com.example.demo.bean.OutputConstruct.EnvironmentStatic;
import com.example.demo.service.AddressService;
import com.example.demo.service.DynamicAnalysisService;
import com.example.demo.service.RuleService;
import com.example.demo.service.StaticAnalysisService;
import com.example.demo.service.SystemModelService;
import com.example.demo.service.TemplGraphService;

@CrossOrigin
@RestController
//@Controller
@RequestMapping("/visual")
public class Controller {
	@RequestMapping("/upload")
	@ResponseBody
	public void uploadFile(@RequestParam("file") MultipartFile uploadedFile) throws DocumentException, IOException {
		//////////////上传的环境本体文件，存储在D:\\workspace位置
		if (uploadedFile == null) {
            System.out.println("上传失败，无法找到文件！");
        }
        //上传xml文件和properties文件		
        String fileName = uploadedFile.getOriginalFilename();
        String filePath=AddressService.MODEL_FILE_PATH+fileName;
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));

        outputStream.write(uploadedFile.getBytes());
        outputStream.flush();
        outputStream.close();
        //逻辑处理
        System.out.println(fileName + "上传成功");
	}
	
	
	/////静态分析
	@RequestMapping(value="/getStaticAnalysisResult",method = RequestMethod.POST)
	@ResponseBody
	public static EnvironmentStatic getStaticAnalysisResult(@RequestBody List<String> ruleTextLines,String initModelFileName,String propertyFileName) throws DocumentException, IOException {
		/////生成规则结构
		List<Rule> rules=RuleService.getRuleList(ruleTextLines);
		////设置更改后的模型文件名
		AddressService.setChangedModelFileName(initModelFileName);
		////获得环境模型
		EnvironmentModel environmentModel=TemplGraphService.getEnvironmentModel(initModelFileName, AddressService.changed_model_file_Name, AddressService.MODEL_FILE_PATH, propertyFileName);
		////静态分析
		StaticAnalysisResult staticAnalysisResult=StaticAnalysisService.getStaticAnalaysisResult(rules, AddressService.IFD_FILE_NAME,  AddressService.IFD_FILE_PATH, environmentModel);
		EnvironmentStatic environmentStatic=new EnvironmentStatic(environmentModel, staticAnalysisResult);
		return environmentStatic;
	}
	
	
	/////生成单个场景模型,并仿真获得分析结果
	@RequestMapping(value="/generateBestScenarioModelAndSimulate",method = RequestMethod.POST)
	@ResponseBody
	public static Scene generateBestScenarioModelAndSimulate(@RequestBody List<String> ruleTextLines,String initModelFileName,String propertyFileName,String simulationTime) throws DocumentException, IOException {
		////更改后的模型文件名
		EnvironmentStatic environmentStatic=getStaticAnalysisResult(ruleTextLines,initModelFileName,propertyFileName);
		EnvironmentModel environmentModel=environmentStatic.getEnvironmentModel();
		
		String fileNameWithoutSuffix=initModelFileName.substring(0, initModelFileName.lastIndexOf(".xml"));
		List<Rule> rules=environmentStatic.getStaticAnalysisResult().getUsableRules();
		List<DeviceDetail> devices=environmentModel.getDevices();
		List<DeviceType> deviceTypes=environmentModel.getDeviceTypes();
		List<BiddableType> biddableTypes=environmentModel.getBiddables();
		List<SensorType> sensorTypes=environmentModel.getSensors();
		List<Attribute> attributes=environmentModel.getAttributes();
		////ruleMap
		HashMap<String,Rule> ruleMap=new HashMap<>();
		for(Rule rule:rules) {
			ruleMap.put(rule.getRuleName(), rule);
		}
		/////获得ifd上各节点
		List<GraphNode> graphNodes=StaticAnalysisService.getIFDNode(AddressService.IFD_FILE_NAME, AddressService.IFD_FILE_PATH);
		SystemModelService.generateContrModel(AddressService.MODEL_FILE_PATH+AddressService.changed_model_file_Name, rules, biddableTypes, devices);
		SystemModelService.generateBestScenarioModel(rules, devices, deviceTypes, biddableTypes, sensorTypes, attributes, AddressService.changed_model_file_Name, AddressService.MODEL_FILE_PATH, graphNodes, fileNameWithoutSuffix+"-scenario-best.xml", simulationTime);
		
		///仿真
		Scene scene=DynamicAnalysisService.getSingleSimulationResult(devices, AddressService.UPPAAL_PATH, fileNameWithoutSuffix, "best", AddressService.MODEL_FILE_PATH, AddressService.SIMULATE_RESULT_FILE_PATH);
		///动态分析
		DynamicAnalysisService.getSingleScenarioDynamicAnalysis(scene, devices, graphNodes, ruleMap);
		return scene;
	}
	
	/////仿真结果可视化，返回video存放路径
	@RequestMapping(value="/getVisualizationResult",method = RequestMethod.GET)
	@ResponseBody
	public static List<String> getVisualizationResult(String initModelFileName) throws DocumentException, IOException {
//		String fileNameWithoutSuffix=initModelFileName.substring(0, initModelFileName.lastIndexOf(".xml"));
//		String bestResultFileName=fileNameWithoutSuffix+"-scenario-best.txt";
		
		String path=AddressService.VIDEO_PATH+"\\video.mp4";
		List<String> path0=new ArrayList<String>();
		System.out.println(path);
		path0.add(path);
		return path0;
	}
	
	
}
