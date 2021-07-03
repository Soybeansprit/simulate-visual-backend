package com.example.demo.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;
@Service
public class GetTemplate {
//////该类解析uppaal的xml文件
	public static void main(String[] args) throws DocumentException {
//		// TODO Auto-generated method stub
//		String path1 = "D:\\window16.xml";
//		String path2="D:\\win16.xml";
//		//删除xml文件第二行
//		
//		GetTemplate parse=new GetTemplate();
//		
//		parse.deletLine(path1, path2, 2);
//		List<Template> templates=parse.getTemplate(path2);
//		List<ControlledDevice> controlledDevices=parse.getControlled(templates);
//		List<Sensor> sensors=parse.getSensor(templates);
//		Rain rain=parse.getRain(templates);
		
	}

	
	
	////////////////Template///////////////////////////////
	public class Template{
		public String name=""; 
		public String declaration="";
		public String parameter="";
		public List<Location> locations= new ArrayList<Location>();
		public List<Branchpoint> branchpoints=new ArrayList<Branchpoint>();
		public String init="";
		public List<Transition> transitions=new ArrayList<Transition>();
	}
	
	class Location{
		String id="";
		String name="";
		String invariant="";
		String style="";
	}
	
	class Branchpoint{
		String id="";
	}
	
	class Transition{
		String sourceId="";
		String targetId="";
		List<Label> labels=new ArrayList<Label>();		
	}
	
	class Label{
		String kind="";
		String content="";
	}

	
	public List<Template> getTemplate(File file) throws DocumentException{
		List<Template> templates=new ArrayList<Template>();
		if(file.getName().indexOf(".xml")>=0) {
			SAXReader reader= new SAXReader();
			Document document = reader.read(file);
			Element rootElement=document.getRootElement();
			System.out.println(rootElement.getName());
			
			List<Element> templateElements=rootElement.elements("template");
			for(Element tempElement:templateElements) {
				System.out.println(tempElement.getName());
				//解析获得template
				Template template=new Template();
				//name
				Element nameElement=tempElement.element("name");
				template.name=nameElement.getTextTrim();
				System.out.println(nameElement.getName());
				//declaration
				Element declarElement=tempElement.element("declaration");
				if(declarElement!=null) {
					template.declaration=declarElement.getText();
					System.out.println(declarElement.getName());
				}
				//parameter
				Element paraElement=tempElement.element("parameter");
				if(paraElement!=null) {
					template.parameter=paraElement.getTextTrim();
					System.out.println(paraElement.getName());
				}
				
				//locations
				List<Element> locaElements=tempElement.elements("location");
				
				for(Element locaElement:locaElements) {
					System.out.println(locaElement.getName());
					Location location=new Location();
					Attribute attr=locaElement.attribute("id");
					location.id=attr.getValue();
					System.out.println();
					System.out.println(location.id);
					System.out.println();
					Element locaNameElement=locaElement.element("name");
					if(locaNameElement!=null) {
						location.name=locaNameElement.getTextTrim();
						System.out.println(locaElement.getName());
					}
					Element invaElement=locaElement.element("label");
					if(invaElement!=null) {
						location.invariant=invaElement.getTextTrim();
						System.out.println(invaElement.getName());
					}
					Element urgentElement=locaElement.element("urgent");
					if(urgentElement!=null) {
						location.style="urgent";
					}
					Element committedElement=locaElement.element("committed");
					if(committedElement!=null) {
						location.style="committed";
					}
					if(urgentElement==null&&committedElement==null) {
						location.style="location";
					}
					template.locations.add(location);
				}
				//branchpoint
				List<Element> branElements=tempElement.elements("branchpoint");
				if(branElements!=null) {
					for(Element branElement:branElements) {
						Branchpoint branchpoint=new Branchpoint();
						Attribute attr=branElement.attribute("id");
						branchpoint.id=attr.getValue();
						template.branchpoints.add(branchpoint);
						System.out.println(branElement.getName());
					}
				}
				//init
				Element initElement=tempElement.element("init");
				Attribute initAttr=initElement.attribute("ref");
				template.init=initAttr.getValue();
				System.out.println(initElement.getName());
				//transitions
				List<Element> tranElements=tempElement.elements("transition");
				for(Element tranElement:tranElements) {
					System.out.println(tranElement.getName());
					Transition transition=new Transition();
					Element sourceElement=tranElement.element("source");
					Attribute sourAttr=sourceElement.attribute("ref");
					transition.sourceId=sourAttr.getValue();
					System.out.println(sourceElement.getName());
					Element targetElement=tranElement.element("target");
					Attribute tarAttr=targetElement.attribute("ref");
					transition.targetId=tarAttr.getValue();
					System.out.println(targetElement.getName());
					List<Element> labelElements=tranElement.elements("label");
					if(labelElements!=null) {
						for(Element labelElement:labelElements) {
							System.out.println(labelElement.getName());
							Label label=new Label();
							Attribute kindAttr=labelElement.attribute("kind");
							label.kind=kindAttr.getValue();
							label.content=labelElement.getTextTrim();
							transition.labels.add(label);
						}
					}
					template.transitions.add(transition);
				}
				//添加template
				templates.add(template);
			}

		}else {
			System.out.println("please upload .xml file");
		}
		return templates;
	}
	
	//解析xml文件，获得template
	public List<Template> getTemplate(String path) throws DocumentException{
		SAXReader reader= new SAXReader();
		Document document = reader.read(new File(path));
		Element rootElement=document.getRootElement();
//		System.out.println(rootElement.getName());
		
		List<Element> templateElements=rootElement.elements("template");
		List<Template> templates=new ArrayList<Template>();
		for(Element tempElement:templateElements) {
//			System.out.println(tempElement.getName());
			//解析获得template
			Template template=new Template();
			//name
			Element nameElement=tempElement.element("name");
			template.name=nameElement.getTextTrim();
//			System.out.println(nameElement.getName());
			//declaration
			Element declarElement=tempElement.element("declaration");
			if(declarElement!=null) {
				template.declaration=declarElement.getText();
//				System.out.println(declarElement.getName());
			}
			//parameter
			Element paraElement=tempElement.element("parameter");
			if(paraElement!=null) {
				template.parameter=paraElement.getTextTrim();
//				System.out.println(paraElement.getName());
			}
			
			//locations
			List<Element> locaElements=tempElement.elements("location");
			
			for(Element locaElement:locaElements) {
//				System.out.println(locaElement.getName());
				Location location=new Location();
				Attribute attr=locaElement.attribute("id");
				location.id=attr.getValue();
//				System.out.println();
//				System.out.println(location.id);
//				System.out.println();
				Element locaNameElement=locaElement.element("name");
				if(locaNameElement!=null) {
					location.name=locaNameElement.getTextTrim();
//					System.out.println(locaElement.getName());
				}
				Element invaElement=locaElement.element("label");
				if(invaElement!=null) {
					location.invariant=invaElement.getTextTrim();
//					System.out.println(invaElement.getName());
				}
				Element urgentElement=locaElement.element("urgent");
				if(urgentElement!=null) {
					location.style="urgent";
				}
				Element committedElement=locaElement.element("committed");
				if(committedElement!=null) {
					location.style="committed";
				}
				if(urgentElement==null&&committedElement==null) {
					location.style="location";
				}
				template.locations.add(location);
			}
			//branchpoint
			List<Element> branElements=tempElement.elements("branchpoint");
			if(branElements!=null) {
				for(Element branElement:branElements) {
					Branchpoint branchpoint=new Branchpoint();
					Attribute attr=branElement.attribute("id");
					branchpoint.id=attr.getValue();
					template.branchpoints.add(branchpoint);
//					System.out.println(branElement.getName());
				}
			}
			//init
			Element initElement=tempElement.element("init");
			Attribute initAttr=initElement.attribute("ref");
			template.init=initAttr.getValue();
//			System.out.println(initElement.getName());
			//transitions
			List<Element> tranElements=tempElement.elements("transition");
			for(Element tranElement:tranElements) {
//				System.out.println(tranElement.getName());
				Transition transition=new Transition();
				Element sourceElement=tranElement.element("source");
				Attribute sourAttr=sourceElement.attribute("ref");
				transition.sourceId=sourAttr.getValue();
//				System.out.println(sourceElement.getName());
				Element targetElement=tranElement.element("target");
				Attribute tarAttr=targetElement.attribute("ref");
				transition.targetId=tarAttr.getValue();
//				System.out.println(targetElement.getName());
				List<Element> labelElements=tranElement.elements("label");
				if(labelElements!=null) {
					for(Element labelElement:labelElements) {
//						System.out.println(labelElement.getName());
						Label label=new Label();
						Attribute kindAttr=labelElement.attribute("kind");
						label.kind=kindAttr.getValue();
						label.content=labelElement.getTextTrim();
						transition.labels.add(label);
					}
				}
				template.transitions.add(transition);
			}
			//添加template
			templates.add(template);
		}
		return templates;
	}
	
	
	public static void deleteFileLine(String path1,String path2,int lineNum) {
		try (FileReader fr=new FileReader(path1);
				BufferedReader br=new BufferedReader(fr);
				FileWriter fw=new FileWriter(path2);
				BufferedWriter bw=new BufferedWriter(fw)){
			StringBuilder sb=new StringBuilder();
			String line="";
			int count=0;
			while((line=br.readLine())!=null) {
				count++;
				if(count==lineNum) {
					line="\r\n";
				}
				sb.append(line+"\r\n");
			}
			bw.write(sb.toString());
			
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	
//	//删除文档中的特定行，并写到新文档
//	public void deletLine(String path1,String path2,int lineNum) {
//		try {
//			FileReader fr= new FileReader(path1);
//			BufferedReader br=new BufferedReader(fr);
//			File file = new File(path2);
//			if(!file.exists()) {
//				try {
//					file.createNewFile();
//				}catch(IOException e) {
//					e.printStackTrace();
//				}
//			}
//			
//			String line="";
//			int count=1;
//			write(file,br.readLine(),false);
//			count++;
//			while((line=br.readLine())!=null) {
//				if(count==lineNum) {
//					write(file,"",true);
//					count++;
//					continue;
//				}
//				write(file,line,true);
//				count++;
//			}
//			
//			br.close();
//			fr.close();
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//	}
//	//写文档
//	public void write(File file, String line, boolean conti) throws IOException {
//		Writer writer=new FileWriter(file, conti);
//		StringBuilder stringBuilder=new StringBuilder();
//		stringBuilder.append(line+"\r\n");
//		writer.write(stringBuilder.toString());
//		writer.close();
//	}
//	
//	//split
//	public List<String> splitStr(String str,String splitStr){
//		return Arrays.asList(str.split(splitStr));
//	}
// 	
	
	

}
