package com.example.demo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.example.demo.bean.Action;
import com.example.demo.bean.Attribute;
import com.example.demo.bean.BiddableType;
import com.example.demo.bean.DeviceDetail;
import com.example.demo.bean.DeviceType;
import com.example.demo.bean.IFDGraph.GraphNode;
import com.example.demo.bean.ModelGraph.TemplGraph;
import com.example.demo.bean.ModelGraph.TemplGraphNode;
import com.example.demo.bean.ModelGraph.TemplTransition;
import com.example.demo.bean.Rule;
import com.example.demo.bean.Trigger;
import com.example.demo.bean.SensorType;
public class SystemModelService {
	
	///////////////生成一个最佳场景
	public static void generateBestScenarioModel(List<Rule> rules,List<DeviceDetail> devices,List<DeviceType> deviceTypes,
			List<BiddableType> biddableTypes,List<SensorType> sensorTypes,List<Attribute> attributes,String modelFileName,String modelFilePath,
			List<GraphNode> graphNodes,String bestScenarioFileName,String simulationTime) throws DocumentException, IOException {
		/////获得所有模型
		List<TemplGraph> templGraphs=TemplGraphService.getTemplGraphs(modelFileName, modelFilePath);
		/////获得控制器模型
		List<TemplGraph> controllers=new ArrayList<TemplGraph>();
		List<TemplGraph> controlledDevices=new ArrayList<>();
		for(TemplGraph templGraph:templGraphs) {
			if(templGraph.getName().indexOf("Rule")>=0) {
				controllers.add(templGraph);
			}else if(templGraph.getDeclaration().contains("controlled_device")) {
				controlledDevices.add(templGraph);
			}
		}
		////ruleMap
		HashMap<String,Rule> ruleMap=new HashMap<>();
		for(Rule rule:rules) {
			ruleMap.put(rule.getRuleName(), rule);
		}
		/////声明的参数
		List<String[]> declarations=generateDeclaration(rules, biddableTypes, deviceTypes, sensorTypes, attributes, controlledDevices);
		List<Action> actions=RuleService.getAllActions(rules, devices);
		List<Trigger> triggers=RuleService.getAllTriggers(rules, sensorTypes, biddableTypes);

		//先获得最佳场景的各属性取值
		List<String[]> attributesValue=getBestScenarioAttributesValue(graphNodes, devices, biddableTypes, attributes, ruleMap);
		////根据attributeValue给declaration赋值
		for(String[] attributeValue:attributesValue) {
			for(String[] declaration:declarations) {
				if(attributeValue[1].equals(declaration[1])) {
					declaration[2]=attributeValue[2];
					break;
				}
			}
		}

		/////模型声明各个场景相同
		String modelDeclaration=getModelDeclaration(actions, triggers, devices, biddableTypes, controllers, attributes, simulationTime);
		/////query仿真公式
		String queryFormula=getQueryFormula(declarations, simulationTime);

		generateScenario(declarations, modelDeclaration, queryFormula, modelFileName, bestScenarioFileName, modelFilePath);
		

	}
	
	////生成单个最佳场景	
	public static List<String[]> getBestScenarioAttributesValue(List<GraphNode> graphNodes,List<DeviceDetail> devices,List<BiddableType> biddables,List<Attribute> attributes,HashMap<String,Rule> ruleMap) {

		HashMap<String,DeviceDetail> deviceMap=new HashMap<>();
		for(DeviceDetail device:devices) {
			deviceMap.put(device.getDeviceName(), device);
		}
		
		////先获得每条规则能触发的所有规则
		List<List<Rule>> rulesTriggeredRules=StaticAnalysisService.getRulesTriggeredRules(ruleMap, graphNodes);
		
		List<String[]> attributesValue=new ArrayList<>();
		
		////分类获得初始条件规则，获得各attribute的初始值
		for(Attribute attribute:attributes) {
			String[] attributeValue=new String[3];
			attributeValue[0]="clock";
			attributeValue[1]=attribute.getAttribute();
			///相同属性的规则
			List<List<Rule>> rulesWithSameAttribute=new ArrayList<>();
			for(List<Rule> ruleTriggeredRules:rulesTriggeredRules) {
				//判断trigger能否为初始值，如果是设备状态而且是开的设备状态，则不是初始值
				Rule rule=ruleTriggeredRules.get(0);
				///有trigger涉及该attribute
				boolean sameAttribute=false;
				///没有trigger是设备的非初始状态
				boolean notInitialState=false;
				for(String trigger:rule.getTrigger()) {
					String[] attrVal=RuleService.getTriAttrVal(trigger, biddables);
					if(attrVal[1].equals(".")) {
						///设备状态 Device.State
						DeviceDetail device=deviceMap.get(attrVal[0]);
						for(String[] stateActionValue:device.getDeviceType().getStateActionValues()) {
							if(stateActionValue[0].equals(attrVal[2])){
								if(!stateActionValue[2].equals("0")) {
									///非初始状态
									notInitialState=true;
								}
								break;
							}
						}
					}else {
						if(attrVal[0].equals(attribute.getAttribute())) {
							sameAttribute=true;
						}
					}
				}
				if(sameAttribute&&!notInitialState) {
					rulesWithSameAttribute.add(ruleTriggeredRules);
				}
			}
			///获得触发规则数最多的规则，并根据这个规则给该属性赋值
			Rule triggeredMaxRule=new Rule();
			int max=0;
			for(List<Rule> ruleTriggeredRules:rulesWithSameAttribute) {
				if(ruleTriggeredRules.size()>max) {
					max=ruleTriggeredRules.size();
					triggeredMaxRule=ruleTriggeredRules.get(0);
				}
			}
			///获得该rule中所有与该attribute相关的tigger
			List<String[]> attrVals=new ArrayList<>();
			for(String trigger:triggeredMaxRule.getTrigger()) {
				String[] attrVal=RuleService.getTriAttrVal(trigger, biddables);
				if(attrVal[0].equals(attribute.getAttribute())) {
					///找到对应trigger
					attrVals.add(attrVal);
				}
			}
			////获得边界值，对attribute赋值，找到所有<的选值最小的，找到所有>的选值最大的
			String[] smallerAttrVal=null;
			String[] greaterAttrVal=null;
			for(String[] attrVal:attrVals) {
				if(attrVal[1].indexOf(">")>=0) {
					if(greaterAttrVal==null) {
						greaterAttrVal=new String[3];
						greaterAttrVal[0]=attrVal[0];
						greaterAttrVal[1]=attrVal[1];
						greaterAttrVal[2]=attrVal[2];
					}else {
						double val1=Double.parseDouble(attrVal[2]);
						double val2=Double.parseDouble(greaterAttrVal[2]);
						if(val1>val2) {
							greaterAttrVal[2]=attrVal[2];
						}
					}
				}else if(attrVal[1].indexOf("<")>=0) {
					if(smallerAttrVal==null) {
						smallerAttrVal=new String[3];
						smallerAttrVal[0]=attrVal[0];
						smallerAttrVal[1]=attrVal[1];
						smallerAttrVal[2]=attrVal[2];
					}else {
						double val1=Double.parseDouble(attrVal[2]);
						double val2=Double.parseDouble(smallerAttrVal[2]);
						if(val1<val2) {
							greaterAttrVal[2]=attrVal[2];
						}
					}
				}
			}
			if(smallerAttrVal!=null && greaterAttrVal!=null) {
				double val1=Double.parseDouble(greaterAttrVal[2]);
				double val2=Double.parseDouble(smallerAttrVal[2]);
				if(val1<val2) {
					attributeValue[2]=String.format("%.1f", (val1+val2)/2);
				}
			}else if(smallerAttrVal!=null) {
				double val=Double.parseDouble(smallerAttrVal[2]);
				attributeValue[2]=(val-1)+"";
			}else if(greaterAttrVal!=null) {
				double val=Double.parseDouble(greaterAttrVal[2]);
				attributeValue[2]=(val+1)+"";
			}
			
			attributesValue.add(attributeValue);
		}
		
		return attributesValue;
		
	}
	
	///////////////生成多个场景，根据temperature等biddable类型的的trigger取值分段获得
//	public static ScenesTree generateAllScenarios(List<Rule> rules,List<DeviceDetail> devices,List<DeviceType> deviceTypes,
//			List<BiddableType> biddableTypes,List<SensorType> sensorTypes,List<Attribute> attributes,String fileName,String filePath,String simulationTime) throws DocumentException, IOException {
//		/////生成场景树
//		ScenesTree scenesTree=new ScenesTree();
//		scenesTree.setName("smart home");
//		
//		/////获得所有模型
//		List<TemplGraph> templGraphs=TemplGraphService.getTemplGraphs(fileName, filePath);
//		List<TemplGraph> controllers=new ArrayList<TemplGraph>();
//		List<TemplGraph> controlledDevices=new ArrayList<>();
//		for(TemplGraph templGraph:templGraphs) {
//			if(templGraph.getName().indexOf("Rule")>=0) {
//				controllers.add(templGraph);
//			}else if(templGraph.getDeclaration().contains("controlled_device")) {
//				controlledDevices.add(templGraph);
//			}
//		}
//		/////声明的参数
//		List<String[]> declarations=generateDeclaration(rules, biddableTypes, deviceTypes, sensorTypes, attributes, controlledDevices);
//		List<Action> actions=RuleService.getAllActions(rules, devices);
//		List<Trigger> triggers=RuleService.getAllTriggers(rules, sensorTypes, biddableTypes);
//		////////找到涉及相同causal类型的属性的triggers，分别获得分段点，用于多个场景的初始值分段赋值
//		List<List<Trigger>> attributesSameTriggers=new ArrayList<List<Trigger>>();
//		for(String[] declaration:declarations) {
//			/////////declaration: [0]clock/double [1]temperature
//			if(declaration[0].equals("double")||declaration[0].equals("clock")) {
//				for(SensorType sensor:sensorTypes) {
//					////找到检测该属性的sensor
//					if(sensor.attribute.equals(declaration[1]) &&
//							sensor.style.equals("causal")) {
//						/////causal类型的属性用于区分场景
//						/////找到涉及该属性的所有triggers
//						List<Trigger> triggersWithSameAttribute=getTriggersWithSameAttribute(declaration, triggers);
//						if(triggersWithSameAttribute.size()>0) {
//							/////如果有涉及该属性的triggers
//							attributesSameTriggers.add(triggersWithSameAttribute);
//						}
//						break;
//					}
//				}
//			}
//		}
//		/////计算上述分段情况下能生成多少个场景
//		////如temperature属性的断点为18、30 && humidity属性的断点为40
//		////则能生成3*2个场景
//		int scenarioNum=getScenarioNum(attributesSameTriggers);
//		/////获得控制器模型
//
//		/////模型声明各个场景相同
//		String modelDeclaration=getModelDeclaration(actions, triggers, devices, biddableTypes, controllers, attributes, simulationTime);
//		/////query仿真公式
//		String queryFormula=getQueryFormula(declarations, simulationTime);
//		////文件名
//		String fileNameWithoutSuffix=fileName.substring(0, fileName.lastIndexOf(".xml"));
//		/////各场景分别赋予不同的值
//		for(int i=0;i<scenarioNum;i++) {
//			/////生成场景树子节点
//			SceneChild sceneChild=new SceneChild();
//			sceneChild.setName("scenario-"+i);
//			////场景模型文件名
//			String newFileName=fileNameWithoutSuffix+"-scenario-"+i+".xml";
//			/////给causal的参数赋值并返回
//			List<AttributeValue> attributeValues=setCausalInitialValue(declarations, attributesSameTriggers, i);
//			/////作为展示细节按钮的子节点
//			AttributeValue attributeValue=new AttributeValue();
//			attributeValue.setName("scenario-"+i +" details");
//			sceneChild.addChildrens(attributeValues);
//			sceneChild.addChildren(attributeValue);
//			scenesTree.addChildren(sceneChild);
//			/////生成模型
//			generateScenario(declarations, modelDeclaration, queryFormula, fileName, newFileName, filePath);
//		}
//		
//		return scenesTree;
//	}
	
	///////////////生成多个场景，根据temperature等biddable类型的的trigger取值分段获得
//	public static void generateAllScenarios(List<Action> actions,List<Trigger> triggers,List<String[]> declarations,List<DeviceDetail> devices,List<DeviceType> deviceTypes,
//			List<BiddableType> biddables,List<SensorType> sensors,List<Attribute> attributes, String fileName,String filePath,String simulationTime) throws DocumentException, IOException {
//		
//		////////找到涉及相同causal类型的属性的triggers，分别获得分段点，用于多个场景的初始值分段赋值
//		List<List<Trigger>> attributesSameTriggers=new ArrayList<List<Trigger>>();
//		for(String[] declaration:declarations) {
//			/////////declaration: [0]clock/double [1]temperature
//			if(declaration[0].equals("double")||declaration[0].equals("clock")) {
//				for(SensorType sensor:sensors) {
//					////找到检测该属性的sensor
//					if(sensor.attribute.equals(declaration[1]) &&
//							sensor.style.equals("causal")) {
//						/////causal类型的属性用于区分场景
//						/////找到涉及该属性的所有triggers
//						List<Trigger> triggersWithSameAttribute=getTriggersWithSameAttribute(declaration, triggers);
//						if(triggersWithSameAttribute.size()>0) {
//							/////如果有涉及该属性的triggers
//							attributesSameTriggers.add(triggersWithSameAttribute);
//						}
//						break;
//					}
//				}
//			}
//		}
//		/////计算上述分段情况下能生成多少个场景
//		////如temperature属性的断点为18、30 && humidity属性的断点为40
//		////则能生成3*2个场景
//		int scenarioNum=getScenarioNum(attributesSameTriggers);
//		/////获得控制器模型
//		List<TemplGraph> templGraphs=TemplGraphService.getTemplGraphs(fileName, filePath);
//		List<TemplGraph> controllers=new ArrayList<TemplGraph>();
//		for(TemplGraph templGraph:templGraphs) {
//			if(templGraph.getName().indexOf("Rule")>=0) {
//				controllers.add(templGraph);
//			}
//		}
//		/////模型声明各个场景相同
//		String modelDeclaration=getModelDeclaration(actions, triggers, devices, biddables, controllers, attributes, simulationTime);
//		/////query仿真公式
//		String queryFormula=getQueryFormula(declarations, simulationTime);
//		////文件名
//		String newFileNameWithoutSuffix=fileName.substring(0, fileName.lastIndexOf(".xml"));
//		/////分别赋予不同的值
//		for(int i=0;i<scenarioNum;i++) {
//			String newFileName=newFileNameWithoutSuffix+"scenario-"+i+".xml";
//			/////给causal的参数赋值
//			setCausalInitialValue(declarations, attributesSameTriggers, i);
//			/////生成模型
//			generateScenario(declarations, modelDeclaration, queryFormula, fileName, newFileName, filePath);
//		}
//	}

	public static void generateScenario(List<String[]> declarations, String modelDeclaration,String query,String fileName,String newFileName,String filePath) throws DocumentException, IOException {
		StringBuilder globalDeclaration=new StringBuilder();
		for(String[] declaration:declarations) {
			if(declaration[1].contains("[0]")) {
				globalDeclaration.append(declaration[0]+" "+declaration[1].replace("[0]", "[1]"));
			}else {
				globalDeclaration.append(declaration[0]+" "+declaration[1]);
			}
			
			if(declaration[2]!=null&&!declaration[2].equals("")) {
				globalDeclaration.append("="+declaration[2]);
			}
			globalDeclaration.append(";\r\n");
		}
		SAXReader reader= new SAXReader();
		Document document = reader.read(new File(filePath+fileName));
		Element rootElement=document.getRootElement();
		////全局声明
		Element declarationElement=rootElement.element("declaration");
		declarationElement.setText(globalDeclaration.toString());
		////模型声明
		Element modelDeclarationElement=rootElement.element("system");
		modelDeclarationElement.setText(modelDeclaration);
		////仿真验证公式
		Element queriesElement=rootElement.element("queries");
		Element queryElement=queriesElement.element("query");
		if(queryElement!=null) {
			Element formulaElement=queryElement.element("formula");
			if(formulaElement!=null) {
				formulaElement.setText(query);
			}else {
				formulaElement=queryElement.addElement("formula");
				formulaElement.setText(query);
			}
		}else {
			queryElement=queriesElement.addElement("query");
			Element formulaElement=queryElement.addElement("formula");
			formulaElement.setText(query);
		}
		OutputStream os=new FileOutputStream(filePath+newFileName);
		OutputFormat format=OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		format.setTrimText(false); //保留换行，但是出现空行
		format.setNewlines(false);
		XMLWriter writer=new XMLWriter(os,format);
		writer.write(document);
		writer.close();
		os.close();
	}
	
	public static String getQueryFormula(List<String[]> declarations,String simulationTime) {
		StringBuilder queryFormula=new StringBuilder();
		queryFormula.append("simulate[<="+simulationTime+"] {");
		
		for(int i=0;i<declarations.size();i++) {
			if(!declarations.get(i)[0].contains("chan")) {				
				///只考虑非通道参数
				String dataName=declarations.get(i)[1];
				if(dataName.contains("[")) {
					////设备标识符  如 bulb[2] => bulb[0],bulb[1]
					int num=Integer.parseInt(dataName.substring(dataName.indexOf("["), dataName.indexOf("]")).substring(1));
					///如果设备没有实例化，则不考虑

					dataName="";
					String identifierName=declarations.get(i)[1].substring(0, declarations.get(i)[1].indexOf("["));
					for(int k=0;k<num;k++) {
						if(k<num-1) {
							dataName=dataName+identifierName+"["+k+"],";
						}else {
							dataName=dataName+identifierName+"["+k+"]";
						}
					}
				}
				if(!dataName.equals("")) {
					if(i<declarations.size()-1) {
						queryFormula.append(dataName+",");
					}else {
						queryFormula.append(dataName);
					}
				}
				
			}
			
		}
		queryFormula.append("}");
		
		return queryFormula.toString();
	}
	
//	public static List<AttributeValue> setCausalInitialValue(List<String[]> declarations,List<List<Trigger>> attributesSameTriggers,int scenarioIndex) {
//		/////返回场景树中各场景下的各属性子节点的取值
//		List<AttributeValue> attributeValues=new ArrayList<>();
//		for(List<Trigger> triggersWithSameAttribute:attributesSameTriggers) {
//			////给每个属性赋值
//			int k=scenarioIndex % (triggersWithSameAttribute.size()+1);
//			scenarioIndex=scenarioIndex/(triggersWithSameAttribute.size()+1);
//			for(String[] declaration:declarations) {
//				if(declaration[1].equals(triggersWithSameAttribute.get(0).attrVal[0])) {
//					AttributeValue attributeValue=new AttributeValue();
//					attributeValue.setName(triggersWithSameAttribute.get(0).attrVal[0]);
//					if(k==0) {
//						///第一段,value-1
//						Double value=Double.parseDouble(triggersWithSameAttribute.get(k).attrVal[2]);
//						declaration[2]=String.format("%.1f", value-1);
//					}else if(k==triggersWithSameAttribute.size()) {
//						///最后一段,value+1
//						Double value=Double.parseDouble(triggersWithSameAttribute.get(k-1).attrVal[2]);
//						declaration[2]=String.format("%.1f", value+1);
//					}else {
//						///中间段,中间值
//						Double value1=Double.parseDouble(triggersWithSameAttribute.get(k-1).attrVal[2]);
//						Double value2=Double.parseDouble(triggersWithSameAttribute.get(k).attrVal[2]);
//						declaration[2]=String.format("%.1f", (value1+value2)/2);
//					}
//					attributeValue.setValue(Double.parseDouble(declaration[2]));
//					attributeValues.add(attributeValue);
//				}
//			}
//		}
//		return attributeValues;
//	}
	
	///////获得模型声明
	public static String getModelDeclaration(List<Action> actions,List<Trigger> triggers,List<DeviceDetail> devices,List<BiddableType> biddables,List<TemplGraph> controllers,List<Attribute> attributes , String simulationTime) {
		StringBuilder sb=new StringBuilder();
		////先实例化
		////Person的实例化需要知道仿真时间，先等等
		////PersonInstance=Person(...)
		for(BiddableType biddable:biddables) {
			if(biddable.getName().equals("Person")) {
				/////每个状态持续时间平均
				sb.append("PersonInstance=Person(");
				///Person(t1,t2,t3);
				double lastTime=Integer.parseInt(simulationTime)/biddable.stateAttributeValues.size();
				if(biddable.stateAttributeValues.size()>=2) {
					for(int i=0;i<biddable.stateAttributeValues.size()-1;i++) {
						if(i<biddable.stateAttributeValues.size()-2) {
							sb.append(lastTime*(i+1)+",");
						}else {
							sb.append(lastTime*(i+1));
						}
					}
				}

				sb.append(");\r\n");
				break;
			}
		}
		////先对设备实例化
		for(DeviceDetail device:devices) {
			////Bulb_1=Bulb(1);
			String deviceInstance=device.getDeviceName()+"="+device.getDeviceType().getName()+"("+device.getConstructionNum()+")";
			sb.append(deviceInstance+";\r\n");
		}
		////system声明中设备只对规则中涉及的设备进行声明
		////还有控制器，biddable实体，sensor不声明
		sb.append("system ");
		////看需要声明哪些模型
		List<String> models=new ArrayList<String>();
		////控制器模型声明
		for(TemplGraph controller:controllers) {
			models.add(controller.getName());
		}
		////biddable声明
		for(BiddableType biddable:biddables) {
			if(biddable.getName().equals("Person")) {
				models.add("PersonInstance");
			}else {
				models.add(biddable.getName());
			}
		}
		////设备声明,只对规则中涉及的设备进行声明
		for(DeviceDetail device:devices) {
			///看在action中是否涉及
			boolean existAction=false;
			for(Action action:actions) {
				if(action.device.equals(device.getDeviceName())) {
					models.add(device.getDeviceName());
					existAction=true;
					break;
				}
			}
			///如果action中不存在，看在trigger中是否涉及
			if(!existAction) {
				for(Trigger trigger:triggers) {
					if(trigger.device.equals(device.getDeviceName())) {
						models.add(device.getDeviceName());
						break;
					}
				}
			}

		}
		////对models用","拼接
		sb.append(String.join(",", models));
		if(attributes.size()>0&&attributes.get(0).isHasAttributeModel()) {
			sb.append(",Attribute");
		}
		sb.append(";");
		return sb.toString();
	}
	
	//////////计算场景数量
	public static int getScenarioNum(List<List<Trigger>> attributesSameTriggers) {
		////如temperature属性的断点为18、30 && humidity属性的断点为40
		////则能生成3*2个场景
		int scenarioNum=1;
		for(List<Trigger> triggersWithSameAttribute:attributesSameTriggers) {
			scenarioNum=scenarioNum*(triggersWithSameAttribute.size()+1);
		}
		return scenarioNum;
	}
	
	/////////找到涉及相同属性的triggers, 并按照分段点取值大小排序（分段点取值不重复）
	public static List<Trigger> getTriggersWithSameAttribute(String[] declaration,List<Trigger> triggers) {
		///如temperature>30和temperature<18,temperature<=30
		////temperature属性的断点为18、30
		List<Trigger> triggersWithSameAttribute=new ArrayList<>();
		for(Trigger trigger:triggers) {			
			if(trigger.attrVal[0].equals(declaration[1])) {
				////////找到涉及该属性的triggers
				////////保证断点值不重复
				boolean pointValueExist=false;
				for(Trigger sameTrigger:triggersWithSameAttribute) {
					if(trigger.attrVal[2].equals(sameTrigger.attrVal[2])) {
						////////保证断点值不重复
						pointValueExist=true;
					}
				}
				if(!pointValueExist) {
					triggersWithSameAttribute.add(trigger);
				}
			}
		}
		Comparator<Trigger> comparator=new Comparator<Trigger>() {
			/////按分段点取值排序
			/////其中分段点的取值为attrVal[2]
			@Override
			public int compare(Trigger t1, Trigger t2) {
				Double v1=Double.parseDouble(t1.attrVal[2]);
				Double v2=Double.parseDouble(t2.attrVal[2]);
				if(v1<v2) {
					return -1;
				}else {
					return 1;
				}
			}
		};
		//////////对triggers进行排序
		Collections.sort(triggersWithSameAttribute, comparator);		
		return triggersWithSameAttribute;
	}
	
	
	
	/////////////获得各参数的声明
	public static List<String[]> generateDeclaration(List<Rule> rules,List<BiddableType> biddableTypes,List<DeviceType> deviceTypes,List<SensorType> sensorTypes,List<Attribute> attributes,List<TemplGraph> templGraphs) {
		List<String[]> declarations=new ArrayList<String[]>();  ////clock temperature 10.0
		if(attributes!=null) {
			for(Attribute attribute:attributes) {
				////先根据attribute获得对应参数
				String[] declaration=new String[3];
				declaration[0]="clock";
				declaration[1]=attribute.getAttribute();
				declarations.add(declaration);
				///如果delta不为0，则需要对其声明
				if(!attribute.getDelta().equals("0")) {
					String[] deltaDeclaration=new String[3];
					deltaDeclaration[0]="double";
					deltaDeclaration[1]=attribute.getDelta();
					deltaDeclaration[2]="0.0";
					declarations.add(deltaDeclaration);
					///delta的初始值都为0
				}
			}
		}

		for(SensorType sensor:sensorTypes) {
			///如果之前声明了就不加入
			boolean exist=false;
			for(String[] declaration:declarations) {
				if(declaration[1].equals(sensor.getAttribute())) {
					exist=true;
					break;
				}
			}
			if(!exist) {
				String[] declaration=new String[3];
				declaration[1]=sensor.attribute;
				declaration[0]="double";
				/////如果是biddable的状态对应的属性，则为int类型
				biddable:
				for(BiddableType biddable:biddableTypes) {
					for(String[] stateAttributeValue:biddable.stateAttributeValues) {
						if(stateAttributeValue[1].equals(declaration[1])) {
							//////如果该属性是biddable状态标识符
							declaration[0]="int[0,"+(biddable.stateAttributeValues.size()-1)+"]";
							break biddable;
						}
					}
				}
				declarations.add(declaration);
			}


		}
		for(DeviceType device:deviceTypes) {
			String[] declaration=new String[3];
			/////声明设备标识符   如Bulb： int[0,1] bulb[9];  有九个实例
			declaration[0]="int[0,"+(device.stateActionValues.size()-1)+"]";
			declaration[1]=device.getName().substring(0,1).toLowerCase()+device.getName().substring(1)+"["+device.deviceNumber+"]";
			
			declarations.add(declaration);
			/////声明信号   如Bulb：   urgent broadcast chan bonPulse[9];  
			/////urgent broadcast chan bonoff[9]; 
			for(String[] stateActionValue:device.stateActionValues) {
				String[] chanDecalration=new String[3];
				chanDecalration[0]="urgent broadcast chan";
				chanDecalration[1]=stateActionValue[1]+"["+device.deviceNumber+"]";				
				declarations.add(chanDecalration);
			}
		}
		for(Rule rule:rules) {
			/////规则的标识符声明  rule1[0,1]=0;
			String[] declaration=new String[3];
			declaration[0]="int[0,1]";
			declaration[1]=rule.getRuleName();
			declaration[2]="0";
			declarations.add(declaration);
		}
		
		////如果没有Attribute模型,就通过这种方式找clock
		if(attributes==null||attributes.size()==0) {
			for(String[] declaration:declarations) {
				if(declaration[0].equals("double")) {
					//////////找到clock类型的参数
					templ:
					for(TemplGraph templGraph:templGraphs) {
						for(TemplGraphNode node:templGraph.getTemplGraphNodes()) {
							String[] invariants=node.getInvariant().split("&&");
							for(String invariant:invariants) {
								invariant=invariant.trim();
								if(invariant.startsWith(declaration[1]+"'")) {
									declaration[0]="clock";
									Attribute attribute=new Attribute();
									attribute.setAttribute(declaration[1]);
									if (attributes==null) attributes=new ArrayList<>();
									attributes.add(attribute);
									break templ;
								}
							}
						}
					}
				}
			}
		}

		

		
		///仿真时间
		String[] declaration=new String[3];
		declaration[0]="clock";
		declaration[1]="time";
		declaration[2]="0.0";
		declarations.add(declaration);
		return declarations;
	}
	
	public static void generateContrModel(String modelFilePath,List<Rule> rules,List<BiddableType> biddables,List<DeviceDetail> devices) throws DocumentException, IOException {
		SAXReader reader= new SAXReader();
		Document document = reader.read(new File(modelFilePath));
		Element rootElement=document.getRootElement();
		List<Element> templateElements=rootElement.elements("template");
		for(Rule rule:rules) {
			//创建rule模型
			//xml中排版顺序为
			//<template>
			//	<name><name/>
			//	<declaration><declaration/>
			//	<location>
			//		<label><label/>
			//	<location/>
			//	<init/>
			//	<transition>
			//		<source/>
			//		<target/>
			//		<label><label/>
			//	<transition/>
			//<template/>
			Element ruleElement=DocumentHelper.createElement("template");
			templateElements.add(0,ruleElement);
			Element nameElement=ruleElement.addElement("name");
			//模型名为：Rule num
			nameElement.setText("Rule"+rule.getRuleName().substring("rule".length()));
			//模型的声明declaration
			Element declarationElement=ruleElement.addElement("declaration");
			declarationElement.setText("clock t;");
			//初始节点
			Element startElement=ruleElement.addElement("location");
			startElement.addAttribute("id", "id0");
			//初始节点位置
			startElement.addAttribute("x", "-300");
			startElement.addAttribute("y", "0");
			//初始节点具有不变式t<=3
			Element labelElement0=startElement.addElement("label");
			labelElement0.addAttribute("kind", "invariant");
			labelElement0.setText("t<=1");
			//第一个节点为初始节点
			Element initElement=ruleElement.addElement("init");
			initElement.addAttribute("ref", "id0");
			List<Element> locationElements=ruleElement.elements("location");

			List<Element> transitionElements=ruleElement.elements("transition");
			//中间节点
			int count=1; //节点计数
			//判断条件的节点
			for(int i=0;i<rule.getTrigger().size();i++) {
				String trigger=rule.getTrigger().get(i);
				//条件判断节点
				Element locationElement=DocumentHelper.createElement("location");
				locationElement.addAttribute("id", "id"+(1+count));
				locationElements.add(0,locationElement);
				//location位置
				locationElement.addAttribute("x", ""+(-300+count*150));
				locationElement.addAttribute("y", "0");
				//满足条件的transition
				Element satTransitionElement=DocumentHelper.createElement("transition");
				Element sourceElement=satTransitionElement.addElement("source");
				Element targetElement=satTransitionElement.addElement("target");
				sourceElement.addAttribute("ref", "id"+(1+count));
				targetElement.addAttribute("ref", "id"+(2+count));
				//不满足条件的transition，从该节点指向初始节点
				Element unsatTransitionElement=DocumentHelper.createElement("transition");
				Element unsatSourceElement=unsatTransitionElement.addElement("source");
				Element unsatTargetElement=unsatTransitionElement.addElement("target");
				unsatSourceElement.addAttribute("ref", "id"+(1+count));
				unsatTargetElement.addAttribute("ref", "id0");
				Element committedElement=DocumentHelper.createElement("committed");
				locationElement.add(committedElement);
				Element unsatGuardElement=unsatTransitionElement.addElement("label");
				unsatGuardElement.addAttribute("kind", "guard");
				Element satGuardElement=satTransitionElement.addElement("label");
				satGuardElement.addAttribute("kind", "guard");
				String[] conAndReverseCon=new String[2];
				if(trigger.indexOf("=")<0&&
						trigger.indexOf("<")<0&&
						trigger.indexOf(">")<0) {
					//如果节点为设备相关，则该节点类型为committed
					//不满足条件的guard为条件的反转
					conAndReverseCon=getEntityStateCondAndReverseCon(trigger, devices, biddables);
				}else {
					//如果节点为属性相关，则该节点类型为committed
					//不满足条件的guard为条件的反转
					conAndReverseCon=getAttrConAndReverseCon(trigger);
				}
				unsatGuardElement.setText(conAndReverseCon[1]);
				satGuardElement.setText(conAndReverseCon[0]);
				//label位置
				unsatGuardElement.addAttribute("x", ""+(-300+10));
				unsatGuardElement.addAttribute("y", ""+(count*90+10));
				//不满足条件则将rule.num赋值为0，表示规则不触发
				//同时将t重新赋值为0
				Element unsatAssElement=unsatTransitionElement.addElement("label");
				unsatAssElement.addAttribute("kind", "assignment");
				//assignment label的位置
				unsatAssElement.addAttribute("x", ""+(-300+10));
				unsatAssElement.addAttribute("y", ""+(count*90-20));
				unsatAssElement.setText(rule.getRuleName()+"=0");
				transitionElements.add(0,unsatTransitionElement);
				//增加两个nail
				Element unsatNailElement0=unsatTransitionElement.addElement("nail");
				unsatNailElement0.addAttribute("x", ""+(-300+(i+1)*150));
				unsatNailElement0.addAttribute("y", ""+count*90);
				Element unsatNailElement1=unsatTransitionElement.addElement("nail");
				unsatNailElement1.addAttribute("x", "-300");
				unsatNailElement1.addAttribute("y", ""+count*90);
				if(i==0) {
					//如果为第一个trigger判断节点，
					//则从初始节点到该节点有个transition
					//transition guard为t>=3
					Element firstTransitionElement=DocumentHelper.createElement("transition");
					Element firstSourceElement=firstTransitionElement.addElement("source");
					Element firstTargetElement=firstTransitionElement.addElement("target");
					firstSourceElement.addAttribute("ref", "id0");
					firstTargetElement.addAttribute("ref", "id"+(1+count));
					Element guardElement=firstTransitionElement.addElement("label");					
					guardElement.addAttribute("kind", "guard");
					guardElement.setText("t>=1");
					Element assignmentElement=firstTargetElement.addElement("label");
					assignmentElement.addAttribute("kind", "assignment");
					assignmentElement.setText("t=0");
					transitionElements.add(0,firstTransitionElement);
				}
				if(i==rule.getTrigger().size()-1) {
					//最后一个trigger满足
					Element assignmentElement=satTransitionElement.addElement("label");
					assignmentElement.addAttribute("kind", "assignment");
					assignmentElement.setText(rule.getRuleName()+"=1");
				}				
				transitionElements.add(0,satTransitionElement);				
				count++;
			}
			
			//action的节点  
			for(int i=0;i<rule.getAction().size();i++) {
				//action节点
				String action=rule.getAction().get(i);
				Element locationElement=DocumentHelper.createElement("location");
				locationElement.addAttribute("id", "id"+(1+count));
				locationElements.add(0,locationElement);
				//位置
				locationElement.addAttribute("x", ""+(-300+count*150));
				locationElement.addAttribute("y", "0");
				//节点类型均为committed
				Element committedElement=DocumentHelper.createElement("committed");
				locationElement.add(committedElement);
				//该节点到下一节点的transition
				Element transitionElement=DocumentHelper.createElement("transition");
				Element sourceElement=transitionElement.addElement("source");
				Element targetElement=transitionElement.addElement("target");
				
				//================================不考虑 for=======================
//				String[] actionTime=action.split("for");
//				if(actionTime[0].indexOf(".")>0)
//				actionTime[0]=actionTime[0].substring(actionTime[0].indexOf(".")).substring(1).trim();
//				if(action.indexOf("for")>0) {
//					if(actionTime[0].indexOf(".")>0)
//					actionTime[1]=actionTime[1].substring(actionTime[0].indexOf(".")).substring(1).trim();
//				}
				//================================不考虑 for=======================
				
				String controllerAction=getControllerAction(action, devices);
				
				if(i<rule.getAction().size()-1) {
					//action 可能包含for表示经过多长时间后进行下一个
					//without for
					sourceElement.addAttribute("ref", "id"+(1+count));
					targetElement.addAttribute("ref", "id"+(2+count));
					Element actionSynElement=transitionElement.addElement("label");
					actionSynElement.addAttribute("kind", "synchronisation");
					
					actionSynElement.setText(controllerAction+"!");
					
					//================================不考虑 for=======================
//					if(action.indexOf("for")>0) {
//						//for time 节点
//						Element nextLocationElement=DocumentHelper.createElement("location");
//						nextLocationElement.addAttribute("id", "id"+(2+count));
//						locationElements.add(0,nextLocationElement);
//						//位置
//						nextLocationElement.addAttribute("x", ""+(-300+(count+1)*150));
//						nextLocationElement.addAttribute("y", "0");
//						//该节点到下一节点的transition
//						Element nextTransitionElement=DocumentHelper.createElement("transition");
//						Element nextSourceElement=nextTransitionElement.addElement("source");
//						Element nextTargetElement=nextTransitionElement.addElement("target");
//						nextSourceElement.addAttribute("ref", "id"+(2+count));
//						nextTargetElement.addAttribute("ref", "id"+(3+count));
//						String time=actionTime[1].substring(0, actionTime[1].indexOf("s"));
//						Element invariantElement=nextLocationElement.addElement("label");
//						invariantElement.addAttribute("kind", "invariant");
//						invariantElement.setText("t<="+time);
//						Element assignmentElement =nextTransitionElement.addElement("label");
//						assignmentElement.addAttribute("kind", "assignment");
//						assignmentElement.setText("t=0");
//						Element guardElement=nextTransitionElement.addElement("label");
//						guardElement.addAttribute("kind", "guard");
//						guardElement.setText("t>="+time);
//						transitionElements.add(0,nextTransitionElement);
//						count++;
//					}
					//================================不考虑 for=======================
				}
				if(i==rule.getAction().size()-1) {
					//最后一个action节点与end连接
					sourceElement.addAttribute("ref", "id"+(1+count));
					targetElement.addAttribute("ref", "id0");
					Element actionSynElement=transitionElement.addElement("label");
					actionSynElement.addAttribute("kind", "synchronisation");
					actionSynElement.addAttribute("x", ""+(-150+50));
					actionSynElement.addAttribute("y", "-90");
					actionSynElement.setText(controllerAction+"!");
					Element finalNailElement=transitionElement.addElement("nail");
					finalNailElement.addAttribute("x", ""+(-300+count*150));
					finalNailElement.addAttribute("y", "-100");
					
					Element nailElement0=transitionElement.addElement("nail");
					nailElement0.addAttribute("x", "-300");
					nailElement0.addAttribute("y", "-100");
				}
				
				
				transitionElements.add(0,transitionElement);
				count++;
			}
			
			
		}
			
		
		OutputStream os=new FileOutputStream(modelFilePath);
		OutputFormat format=OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		format.setTrimText(false);
		format.setNewlines(true);
		XMLWriter writer=new XMLWriter(os,format);
		writer.write(document);
		writer.close();
		os.close();
	}
	
	
	public static void generatePersonModel(TemplGraph person,String modelFilePath) throws DocumentException, IOException {
		SAXReader reader= new SAXReader();
		Document document = reader.read(new File(modelFilePath));
		Element rootElement=document.getRootElement();
		List<Element> templateElements=rootElement.elements("template");
		for(Element templateElement:templateElements) {
			Element nameElement=templateElement.element("name");
			if(nameElement.getTextTrim().equals(person.getName())) {
				///是人的模型
				//删除原来的模板
				rootElement.remove(templateElement);				
				break;
			}
		}
		templateElements=rootElement.elements("template");
		///新建新的Person模板
		Element personElement=DocumentHelper.createElement("template");
		templateElements.add(0, personElement);
		Element nameElement=personElement.addElement("name");
		nameElement.setText(person.getName());
		Element parameterElement=personElement.addElement("parameter");
		parameterElement.setText(person.getParameter());
		Element declarationElement=personElement.addElement("declaration");
		declarationElement.setText(person.getDeclaration());
		
		List<Element> transitionElements=new ArrayList<Element>();
		int count=1;
		for(TemplGraphNode node:person.getTemplGraphNodes()) {
			/////location
			Element locationElement=personElement.addElement("location");
			locationElement.addAttribute("id", node.id);
			locationElement.addAttribute("x", 150*count+"");
			locationElement.addAttribute("y", "100");
			if(node.style.equals("committed")) {
				locationElement.addElement("committed");
			}else if(!node.name.equals("")) {
				Element locationNameElement=locationElement.addElement("name");
				locationNameElement.setText(node.name);
				if(!node.getInvariant().equals("")) {
					Element labelElement=locationElement.addElement("label");
					labelElement.addAttribute("kind", "invariant");
					labelElement.setText(node.getInvariant());
				}
			}
			//////// transition
			for(TemplTransition outTransition:node.outTransitions) {
				Element transitionElement=DocumentHelper.createElement("transition");
				Element sourceElement=transitionElement.addElement("source");
				sourceElement.addAttribute("ref", outTransition.source);
				Element targetElement=transitionElement.addElement("target");
				targetElement.addAttribute("ref", outTransition.target);
				if(!outTransition.guard.equals("")) {
					Element guardElement=transitionElement.addElement("label");
					guardElement.addAttribute("kind", "guard");
					guardElement.setText(outTransition.guard);
				}
				if(!outTransition.assignment.equals("")) {
					Element assignmentElement=transitionElement.addElement("label");
					assignmentElement.addAttribute("kind", "assignment");
					assignmentElement.setText(outTransition.assignment);
				}
				transitionElements.add(transitionElement);
			}			
			count++;
		}
		Element initElement=personElement.addElement("init");
		initElement.addAttribute("ref", person.getTemplGraphNodes().get(0).id);
		
		for(Element transitionElement:transitionElements) {
			personElement.add(transitionElement);
		}
		
		OutputStream os=new FileOutputStream(modelFilePath);
		OutputFormat format=OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		format.setTrimText(false);
		format.setNewlines(true);
		XMLWriter writer=new XMLWriter(os,format);
		writer.write(document);
		writer.close();
		os.close();
	}
	
	
	/////////////////////////////////////2020.12.29///////////////////////////////
	//获得属性条件和反转条件
	//////String[0]-----condition String[1]-----reverseCondition
	public static String[] getAttrConAndReverseCon(String trigger){
		String[] conAndReverseCon=new String[2];
		String reverseCondition="";
		String condition=trigger;
		if(trigger.indexOf("FOR")>=0) {
			condition=trigger.substring(0, trigger.indexOf("FOR")).trim();
		}
		if(trigger.indexOf(".")>0) {
			condition=condition.substring(condition.indexOf(".")).substring(".".length());
		}
		if(condition.indexOf(">=")>0) {
			reverseCondition=condition.replace(">=", "<");
		}else if(condition.indexOf(">")>0) {
			reverseCondition=condition.replace(">", "<=");
		}else if(condition.indexOf("<=")>0) {
			reverseCondition=condition.replace("<=", ">");
		}else if(condition.indexOf("<")>0) {
			reverseCondition=condition.replace("<", ">=");
		}else if(condition.indexOf("=")>0) {
			reverseCondition=condition.replace("=", "!=");
			condition=condition.replace("=", "==");
		}
		//距离感应  这块暂时不考虑了
//		if(reverseCondition.indexOf("distanceFrom")>=0) {
//			for(TemplGraph templGraph:templGraphs) {
//				if(templGraph.getDeclaration().indexOf("sensor")>=0 && templGraph.getDeclaration().indexOf("distance")>0) {
//					for(TemplGraphNode templGraphNode:templGraph.getTemplGraphNodes()) {
//						for(TemplTransition inTransition:templGraphNode.inTransitions) {
//							if(inTransition.assignment!=null) {
//								String[] assignments=inTransition.assignment.split(",");
//								for(String assignment:assignments) {
//									if(assignment.indexOf("get()")>0) {
//										assignment=assignment.trim();
//										String attribute=assignment.substring(0, assignment.indexOf("=")).trim();
//										if(reverseCondition.indexOf(">")>0) {
//											String originAttribute=reverseCondition.substring(0, reverseCondition.indexOf(">")).trim();
//											reverseCondition=reverseCondition.replace(originAttribute, attribute);
//											condition=condition.replace(originAttribute, attribute);
//										}
//										if(reverseCondition.indexOf("<")>0) {
//											String originAttribute=reverseCondition.substring(0, reverseCondition.indexOf("<")).trim();
//											reverseCondition=reverseCondition.replace(originAttribute, attribute);
//											condition=condition.replace(originAttribute, attribute);
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//			
//		}

		conAndReverseCon[0]=condition;
		conAndReverseCon[1]=reverseCondition;
		return conAndReverseCon;
	}
	
	
	///////////对于实体状态 condition和reverseCondition
	public static String[] getEntityStateCondAndReverseCon(String trigger,List<DeviceDetail> devices,List<BiddableType> biddables){
		/////trigger= Entity.state
		String[] conAndReverseCon=new String[2];
		String reverseCondition="";
		String condition="";


		
		String attrVal[]=RuleService.getTriAttrVal(trigger, biddables);
		//找到该设备状态对应的值/condition
		
		for(DeviceDetail device:devices) {
			if(device.getDeviceName().equals(attrVal[0])) {
				//找到哪类设备
				//找到状态对应值
				String value="";
				for(String[] stateActionValue:device.getDeviceType().stateActionValues) {
					if(stateActionValue[0].equals(attrVal[2])) {
						value=stateActionValue[2];
					}
				}
				if(!value.equals("")) {
					String deviceTypeName=device.getDeviceType().getName();
					condition=deviceTypeName.substring(0, 1).toLowerCase()+deviceTypeName.substring(1)+"["+device.getConstructionNum()+"]=="+value;
					reverseCondition=condition.replace("==", "!=");
				}
				
			}
		}
		if(!attrVal[1].equals(".")) {
			if(attrVal[1].equals("!=")) {
				condition=attrVal[0]+"!="+attrVal[2];
				reverseCondition=condition.replace("!=", "==");
			}else {
				condition=attrVal[0]+"=="+attrVal[2];
				reverseCondition=condition.replace("==", "!=");
			}
	
		}
//		if(!isDevice) {
//			String[] attrVal=RuleService.getTriAttrVal(trigger, biddables);   /////如Person.Lobby , Person position 1  ==> position=1
//
////			for(TemplGraph templGraph:templGraphs) {
////				if(templGraph.getName().equals(entityName)) {
////					if(templGraph.getDeclaration().indexOf("biddable")>=0) {
////						////对应biddable实体状态
////						for(TemplGraphNode stateNode:templGraph.getTemplGraphNodes()) {
////							if(stateNode.name.equals(state)) {
////								for(TemplTransition inTransition:stateNode.inTransitions) {
////									String[] assignments=inTransition.assignment.split(",");
////									////如果不是t=开头的，则是状态对应的属性
////									if(assignments.length<=2) {
////										for(String assignment:assignments) {
////											assignment=assignment.trim();
////											if(!assignment.startsWith("t=")) {
////												condition=assignment.replace("=", "==");;
////												reverseCondition=condition.replace("==", "!=");
////											}
////											break;
////										}
////									}
////									break;
////								}
////							}
////						}
////					}
////					break;
////				}
////			}
//		}
		conAndReverseCon[0]=condition;
		conAndReverseCon[1]=reverseCondition;
		return conAndReverseCon;
	}
	
	
	
	
	//获得设备状态条件和反转条件
	//////String[0]-----condition String[1]-----reverseCondition
//	public static List<String> getConAndReverseCon(String trigger,List<TemplGraph> templGraphs){
//		List<String> conAndReverseCon=new ArrayList<String>();
//		String reverseCondition=null;
//		String condition=null;
//		String device=null;
//		String state=null;
//		if(trigger.indexOf(".")>0) {
//			state=trigger.substring(trigger.indexOf(".")).substring(".".length());
//			device=trigger.substring(0, trigger.indexOf("."));
//		}
//		//找到该设备状态对应的值/condition
//		for(TemplGraph templGraph:templGraphs) {
//			if(templGraph.getName().equals(device)) {
//				for(TemplGraphNode templGraphNode:templGraph.getTemplGraphNodes()) {
//					if(templGraphNode.name.equals(state)) {
//						if(templGraphNode.inTransitions!=null) {
//							if(templGraphNode.inTransitions.get(0).assignment!=null) {
//								String[] assignments=templGraphNode.inTransitions.get(0).assignment.split(",");
//								//设备参数，首字母小写
//								String deviceParameter=device.substring(0, 1).toLowerCase()+device.substring(1);
//								for(String assignment:assignments) {
//									if(assignment.indexOf(deviceParameter)>=0) {
//										condition=assignment;
//										break;
//									}
//								}
//							}
//						}						
//						break;
//					}
//				}				
//				break;
//			}
//		}
//		if(condition!=null && condition.indexOf("=")>0) {
//			reverseCondition=condition.replace("=", "!=");
//			condition=condition.replace("=", "==");
//		}
//		conAndReverseCon.add(condition);
//		conAndReverseCon.add(reverseCondition);
//		return conAndReverseCon;
//	}
	
	/////////action 的transition生成
	public static String getControllerAction(String ruleAction,List<DeviceDetail> devices) {
		String actionStr="";
		String deviceName="";
		if(ruleAction.indexOf(".")>0) {
			deviceName=ruleAction.substring(0, ruleAction.indexOf(".")).trim();
			actionStr=ruleAction.substring(ruleAction.indexOf(".")).substring(1).trim();
		}
		if(!deviceName.equals("")) {
			for(DeviceDetail device:devices) {
				if(deviceName.equals(device.getDeviceName())) {
					actionStr=actionStr+"["+device.getConstructionNum()+"]";
					break;
				}
			}
		}
		return actionStr;
	}
	
	

}
