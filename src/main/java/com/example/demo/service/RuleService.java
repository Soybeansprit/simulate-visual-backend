package com.example.demo.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.bean.Action;
import com.example.demo.bean.BiddableType;
import com.example.demo.bean.DeviceDetail;
import com.example.demo.bean.Rule;
import com.example.demo.bean.SensorType;
import com.example.demo.bean.Trigger;

@Service
public class RuleService {
	
	public static void main(String[] args) throws IOException {
		RuleService ruleService=new RuleService();
		String ruleStrList="1. IF SmartHomeSecurity.homeMode AND temperature<=15 THEN Heater.turn_heat_on\r\n" + 
				"\r\n" + 
				"2. IF SmartHomeSecurity.homeMode AND temperature>=30 THEN AirConditioner.turn_ac_cool\r\n" + 
				"\r\n" + 
				"3. IF SmartHomeSecurity.homeMode AND humidity<20 THEN Humidifier.turn_hum_on\r\n" + 
				"\r\n" + 
				"4. IF SmartHomeSecurity.homeMode AND humidity>=45 THEN Humidifier.turn_hum_off\r\n" + 
				"\r\n" + 
				"5. IF SmartHomeSecurity.homeMode AND humidity>65 THEN Fan.turn_fan_on\r\n" + 
				"\r\n" + 
				"6. IF SmartHomeSecurity.homeMode AND temperature>28 THEN Fan.turn_fan_on\r\n" + 
				"\r\n" + 
				"7. IF SmartHomeSecurity.homeMode AND temperature<20 THEN Fan.turn_fan_off\r\n" + 
				"\r\n" + 
				"8. IF SmartHomeSecurity.homeMode AND rain=1 THEN PhilipsHueLight.turn_phl_white\r\n" + 
				"\r\n" + 
				"9. IF SmartHomeSecurity.homeMode AND temperature<=10 THEN PhilipsHueLight.turn_phl_blue\r\n" + 
				"\r\n" + 
				"10. IF SmartHomeSecurity.homeMode AND leak=1 THEN PhilipsHueLight.turn_phl_blue\r\n" + 
				"\r\n" + 
				"11. IF SmartHomeSecurity.awayMode THEN PhilipsHueLight.turn_phl_off\r\n" + 
				"\r\n" + 
				"12. IF SmartHomeSecurity.homeMode THEN PhilipsHueLight.turn_phl_white\r\n" + 
				"\r\n" + 
				"13. IF Door.dopen THEN PhilipsHueLight.turn_phl_white\r\n" + 
				"\r\n" + 
				"14. IF SmartHomeSecurity.homeMode AND co2ppm>=1000 THEN PhilipsHueLight.turn_phl_red\r\n" + 
				"\r\n" + 
				"15. IF SmartHomeSecurity.awayMode THEN Fan.turn_fan_on\r\n" + 
				"\r\n" + 
				"16. IF Door.dopen THEN Heater.turn_heat_off\r\n" + 
				"\r\n" + 
				"17. IF Window.wopen THEN Heater.turn_heat_off\r\n" + 
				"\r\n" + 
				"18. IF SmartHomeSecurity.awayMode THEN Heater.turn_heat_off,AirConditioner.turn_ac_off,Fan.turn_fan_off,Blind.close_blind,Bulb.turn_bulb_off\r\n" + 
				"\r\n" + 
				"19. IF SmartHomeSecurity.homeMode AND temperature<18 THEN AirConditioner.turn_ac_heat\r\n" + 
				"\r\n" + 
				"20. IF SmartHomeSecurity.homeMode AND temperature>30 THEN AirConditioner.turn_ac_cool\r\n" + 
				"\r\n" + 
				"21. IF SmartHomeSecurity.homeMode THEN Robot.dock_robot\r\n" + 
				"\r\n" + 
				"22. IF SmartHomeSecurity.awayMode THEN Robot.start_robot\r\n" + 
				"\r\n" + 
				"23. IF SmartHomeSecurity.awayMode THEN Window.close_window,Door.close_door\r\n" + 
				"\r\n" + 
				"24. IF number>0 THEN SmartHomeSecurity.turn_sms_home\r\n" + 
				"\r\n" + 
				"25. IF number=0 THEN SmartHomeSecurity.turn_sms_away\r\n" + 
				"\r\n" + 
				"26. IF SmartHomeSecurity.homeMode AND temperature>28 THEN Blind.open_blind\r\n" + 
				"\r\n" + 
				"27. IF SmartHomeSecurity.homeMode THEN Bulb.turn_on_bulb\r\n" + 
				"\r\n" + 
				"28. IF SmartHomeSecurity.homeMode AND co2ppm>=800 THEN Fan.turn_on_fan,Window.open_window\r\n" + 
				"\r\n" + 
				"29. IF AirConditioner.cool THEN Window.close_window\r\n" + 
				"\r\n" + 
				"30. IF AirConditioner.heat THEN Window.close_window";
		List<Rule> rules=getRuleList(ruleStrList);
		System.out.println(rules);
		
		String rulePath="D:\\rules0105new.txt";
		List<Rule> txtRules=ruleService.getRuleListFromTxt(rulePath);
		System.out.println(txtRules);
	}

	//////////////////////////////解析rule//////////////////////////////
	public static Rule getRule(String ruleStr,int num) {
		Rule rule=null;
		if(ruleStr.endsWith("\r\n")) {
			ruleStr=ruleStr.substring(0, ruleStr.indexOf("\r\n"));
		}
		if(ruleStr.endsWith("\n")) {
			ruleStr=ruleStr.substring(0, ruleStr.indexOf("\n"));
		}
		ruleStr=ruleStr.trim();
		if(ruleStr!=null && ruleStr.toUpperCase().indexOf("IF ")>=0 &&
				ruleStr.toUpperCase().indexOf(" THEN ")>0) {
			rule=new Rule();
			int ifIndex=ruleStr.toUpperCase().indexOf("IF ");    //////IF 的下标
			int thenIndex=ruleStr.toUpperCase().indexOf(" THEN ");     //////THEN 的下标
			
			if(ifIndex>=thenIndex) {
				System.out.println(ruleStr+"\n    not rule!");
				rule=null;
			}else {				
				rule.setRuleName("rule"+num);   ///////规则的序号 rule1
				rule.setRuleContent(ruleStr.substring(ruleStr.toUpperCase().indexOf("IF ")));   /////规则内容  IF...THEN...
				String triggerStr=ruleStr.substring(ifIndex, thenIndex).substring("IF ".length()).trim();  ////trigger字符串串
				String actionStr=ruleStr.substring(thenIndex).substring(" THEN ".length()).trim();    /////action字符串
				List<String> triggers=Arrays.asList(triggerStr.split("AND"));   ////trigger列表
				List<String> actions=Arrays.asList(actionStr.split(","));       /////action列表
				for(int i=0;i<triggers.size();i++) {
					String trigger=triggers.get(i).trim();
					triggers.set(i, trigger);					
				}
				rule.setTrigger(triggers);
				for(int i=0;i<actions.size();i++) {
					String action=actions.get(i).trim();
					actions.set(i, action);
				}
				rule.setAction(actions);				
			}			
		}
		return rule;
	}
	

	/////////////////解析所有规则
	public static List<Rule> getRuleList(List<String> ruleTextLines){
		List<Rule> rules=new ArrayList<Rule>();
		List<String> ruleStrList=new ArrayList<String>();;
		for(String ruleTextLine:ruleTextLines) {
			////////找到含IF...THEN...的行
			if(ruleTextLine!=null && ruleTextLine.toUpperCase().indexOf("IF ")>=0 &&
					ruleTextLine.toUpperCase().indexOf(" THEN ")>0) {
				ruleTextLine=ruleTextLine.substring(ruleTextLine.toUpperCase().indexOf("IF "));
				ruleStrList.add(ruleTextLine);
			}
		}
		for(int i=0;i<ruleStrList.size();i++) {
			String ruleStr=ruleStrList.get(i);
			////////解析IF...THEN... 获得Rule结构的rule
			Rule rule=getRule(ruleStr, i+1);
			if(rule!=null) {
				rules.add(rule);
			}			
		}
		return rules;
	}
	
	public static List<Action> getAllActions(List<Rule> rules,List<DeviceDetail> devices){
		List<Action> actions=new ArrayList<Action>();  ///存所有actions
		for(Rule rule:rules) {
			for(String action:rule.getAction()) {  //////遍历一条规则的所有actions
				boolean exist=false;
				for(Action act:actions) {
					if(act.action.equals(action)) {  ////看是否已经存在于 actions 中
						exist=true;
						break;
					}
				}
				if(!exist) {          ////不存在就添加
					Action act=new Action();
					act.action=action;       /////action内容  Bulb_0.turn_bulb_on
					act.actionNum="action"+actions.size();     ///action序号（用于ifd中）action0
					if(action.indexOf(".")>0) {
						act.actionPulse=action.substring(action.indexOf(".")).substring(1).trim(); ///actionPulse: turn_bulb)on
						act.device=action.substring(0, action.indexOf("."));     /////device: Bulb_0
					}else {
						act.actionPulse=action;
					}
					for(DeviceDetail device:devices) {
						if(device.getDeviceName().equals(act.device)) {
							for(String[] stateActionValue:device.getDeviceType().stateActionValues) {
								if(stateActionValue[1].equals(act.actionPulse)) {
									act.toState=stateActionValue[0];    //////找到这个action对应的状态
									act.value=stateActionValue[2];      //////和标识符的取值
									break;
								}
							}
							break;
						}
					}
					for(Rule otherRule:rules) {
						for(String oAction:otherRule.getAction()) {
							////////获得有这个action的所有规则
							if(oAction.equals(act.action)) {
								act.rules.add(otherRule);
								break;
							}
						}
					}
					actions.add(act);
				}
			
			}
		}
		return actions;
	}
	
	//////////获得一组规则的所有triggers
	public static List<Trigger> getAllTriggers(List<Rule> rules,List<SensorType> sensors,List<BiddableType> biddables){
		List<Trigger> triggers=new ArrayList<Trigger>();  ///存所有triggers
		for(Rule rule:rules) {
			for(String trigger:rule.getTrigger()) {  //////遍历一条规则的所有triggers
				boolean exist=false;
				for(Trigger tri:triggers) {
					if(tri.trigger.equals(trigger)) {  ////看是否已经存在于 triggers 中
						exist=true;
						break;
					}
				}
				if(!exist) {            ////不存在就添加
					Trigger tri=new Trigger();
					tri.trigger=trigger;     /////trigger内容
					tri.triggerNum="trigger"+triggers.size();    ///trigger序号（用于ifd中）
					tri.attrVal=getTriAttrVal(trigger,biddables);      /////分析trigger
					if(!tri.attrVal[1].equals(".")){
						for(SensorType sensor:sensors) {
							if(sensor.attribute.equals(tri.attrVal[0])) {
								tri.device=sensor.getName();
								break;
							}
						}
					}else {
						tri.device=tri.attrVal[0];
					}
					for(Rule otherRule:rules) {
						////////获得有这个trigger的所有规则
						for(String oTrigger:otherRule.getTrigger()) {
							if(oTrigger.equals(tri.trigger)) {
								tri.rules.add(otherRule);
								break;
							}
						}
					}
					triggers.add(tri);
				}
			}
		}		
		return triggers;
	}
	
	public static String[] getTriAttrVal(String trigger,List<BiddableType> biddables) {
		String[] attrVal=new String[3];
		if(trigger.contains(">=")) {
			attrVal[1]=">=";
			attrVal[0]=trigger.substring(0,trigger.indexOf(">=")).trim();
			attrVal[2]=trigger.substring(trigger.indexOf(">=")).substring(2).trim();
		}else if(trigger.contains(">")) {
			attrVal[1]=">";
			attrVal[0]=trigger.substring(0, trigger.indexOf(">")).trim();
			attrVal[2]=trigger.substring(trigger.indexOf(">")).substring(1).trim();
		}else if(trigger.contains("<=")) {
			attrVal[1]="<=";
			attrVal[0]=trigger.substring(0,trigger.indexOf("<=")).trim();
			attrVal[2]=trigger.substring(trigger.indexOf("<=")).substring(2).trim();
		}else if(trigger.contains("<")) {
			attrVal[1]="<";
			attrVal[0]=trigger.substring(0, trigger.indexOf("<")).trim();
			attrVal[2]=trigger.substring(trigger.indexOf("<")).substring(1).trim();
		}else if(trigger.contains("=")) {
			attrVal[1]="=";
			attrVal[0]=trigger.substring(0,trigger.indexOf("=")).trim();
			attrVal[2]=trigger.substring(trigger.indexOf("=")).substring(1).trim();
		}else if(trigger.contains(".")) {
			//////表示实体状态  Bulb_0.bon   Person.lobby
			attrVal[1]=".";
			attrVal[0]=trigger.substring(0, trigger.indexOf(".")).trim();
			attrVal[2]=trigger.substring(trigger.indexOf(".")).substring(1).trim();	
			boolean isNot=false;
			////非
			if(attrVal[0].toUpperCase().startsWith("NOT_")) {
				attrVal[0]=attrVal[0].substring("NOT_".length());
				isNot=true;
			}
			for(BiddableType biddable:biddables) {
				if(biddable.getName().equals(attrVal[0])) {     /////不是设备而是biddable实体的状态，如Person
					for(String[] stateAttributeValue:biddable.stateAttributeValues) {
						if(stateAttributeValue[0].equals(attrVal[2])) {
							/////////找到state对应的属性值
							attrVal[0]=stateAttributeValue[1];
							if(isNot) {
								attrVal[1]="!=";	
							}else {
								attrVal[1]="=";
							}							
							attrVal[2]=stateAttributeValue[2];
							break;
						}
						
					}
				}
			}
								
		}
		return attrVal;
	}
	
	
	
	///////////////////////////从字符串获得所有规则//////////////////////////////////
	public static List<Rule> getRuleList(String ruleTxt){
		List<String> ruleStrList=new ArrayList<String>();
		int changeLine=0;
		if(ruleTxt.indexOf("\r\n")>0) {
			changeLine=1;
		}else if(ruleTxt.indexOf("\n")>0) {
			changeLine=2;
		}
		//////////////////////换行了才能算///////////////////////
		List<String> strList=new ArrayList<String>();
		if(changeLine==1) {
			strList=Arrays.asList(ruleTxt.split("\r\n"));
		}else if(changeLine==2) {
			strList=Arrays.asList(ruleTxt.split("\n"));
		}
		for(String str:strList) {
			if(str!=null && str.toUpperCase().indexOf("IF ")>=0 &&
					str.toUpperCase().indexOf(" THEN ")>0) {
				ruleStrList.add(str);
			}
		}
		List<Rule> rules=new ArrayList<Rule>();
		for(int i=0;i<ruleStrList.size();i++) {
			String ruleStr=ruleStrList.get(i);
			Rule rule=getRule(ruleStr, i+1);
			if(rule!=null) {
				rules.add(rule);
			}			
		}
		return rules;
	}
	
	public List<Rule> getRuleListFromTxt(String path){
		List<String> ruleStrList=new ArrayList<String>();
		List<Rule> rules=new ArrayList<Rule>();
		try {
			FileReader fr=new FileReader(path);
			BufferedReader br=new BufferedReader(fr);
			
			String str="";
			while((str=br.readLine())!=null) {
				if(str!=null && str.toUpperCase().indexOf("IF ")>=0 &&
						str.toUpperCase().indexOf(" THEN ")>0) {
					ruleStrList.add(str);
				}
			}
			
			br.close();
			fr.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		for(int i=0;i<ruleStrList.size();i++) {
			String ruleStr=ruleStrList.get(i);
			Rule rule=getRule(ruleStr, i+1);
			if(rule!=null) {
				rules.add(rule);
			}			
		}		
		return rules;
	}
	
	public static String getAudioToRules(String audioFileName,String audioPath,String convertToolPath) {
		  InputStream error = null;
		  	try {

//		  		System.out.println(command.toString());
		  		Process process = Runtime.getRuntime().exec(getCMDCommand(convertToolPath, audioFileName, audioPath));
		  		
		  		error = process.getErrorStream();
//		  		long startTime0=System.currentTimeMillis();
		  		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(),Charset.forName("UTF-8")));
		  		BufferedReader errorReader=new BufferedReader(new InputStreamReader(error,Charset.forName("GBK")));
//		  		long endTime0=System.currentTimeMillis();
//	  			System.out.println("simulationData time: "+(endTime0-startTime0));
		  		StringBuffer resultBuffer = new StringBuffer();
		  		String s = "";
		  		s = bufferedReader.readLine();
		  		if(s==null) {
		  			System.out.println("null");
		  		}
		  		while (s != null) {
		  			System.out.println(s);
//		  			System.out.println(s);
		  			s=s.substring(1, s.length()-1);
		  			String[] ss=s.split(",");
		  			for(int i=0;i<ss.length;i++) {
		  				ss[i]=ss[i].trim();
		  				if(ss[i].startsWith("'")&&ss[i].endsWith("'")) {
		  					ss[i]=ss[i].substring(1, ss[i].length()-1);
		  					System.out.println(ss[i]);
				  			resultBuffer.append(ss[i]+"\n");
		  				}

		  			}

//		  			long startTime=System.currentTimeMillis();
		  			s = bufferedReader.readLine();
//		  			long endTime=System.currentTimeMillis();
//		  			System.out.println("readline time: "+(endTime-startTime));
//		  			if(s!=null) {
//		  				System.out.println(s.toString());	
//		  			}
		  			
		  		}
		  		s=errorReader.readLine();
		  		while(s!=null) {
		  			System.out.println(s);
		  			s=errorReader.readLine();
		  		}
		  		bufferedReader.close();
		  		errorReader.close();
		  		process.waitFor();
		  		String result=resultBuffer.toString();
//		  		int formulaIsSatisfiedIndex=result.indexOf("Formula is satisfied.");
//		  		if(formulaIsSatisfiedIndex>=0) {
//		  			result=result.substring(formulaIsSatisfiedIndex).substring("Formula is satisfied.".length());
//		  		}
		  		return result;
		  	} catch (Exception ex) {
		  		if (error != null) {
		  			try {
		  				error.close();
		  			} catch (IOException e) {
		  				e.printStackTrace();
		  			}
		  		}
		  		return ex.getMessage();
		  	}

	}
	
	public static String getCMDCommand(String convertToolPath,String audioFileName,String audioFilePath) {
  		StringBuffer command = new StringBuffer();
  		command.append("cmd /c d:");
  		//这里的&&在多条语句的情况下使用，表示等上一条语句执行成功后在执行下一条命令，
  		//也可以使用&表示执行上一条后台就立刻执行下一条语句
  		command.append(String.format("&& cd %s",convertToolPath));
  		command.append(String.format(" && python %s %s","c2t.py",audioFilePath+audioFileName));
  		System.out.println(command.toString());
  		
  		return command.toString();
	}
	
	public static String getLinuxCommand(String convertToolPath,String audioFileName,String audioFilePath) {
  		StringBuffer command = new StringBuffer();
  		//这里的&&在多条语句的情况下使用，表示等上一条语句执行成功后在执行下一条命令，
  		//也可以使用&表示执行上一条后台就立刻执行下一条语句
  		command.append(String.format("cd %s", convertToolPath));
  		command.append(String.format("&& python  %s %s", "c2t.py",audioFilePath+audioFileName));
  		
  		return command.toString();
	}
	
	
	

	
	
}
