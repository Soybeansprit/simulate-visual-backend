package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class ModelGraph {
	//////表示uppaal模型的类
	public static class TemplGraph {
		private String name="";   /////模型名
		private String declaration="";  
		private String parameter="";
		private String init="";   
		private List<TemplGraphNode> templGraphNodes=new ArrayList<TemplGraphNode>();
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDeclaration() {
			return declaration;
		}
		public void setDeclaration(String declaration) {
			this.declaration = declaration;
		}
		public String getParameter() {
			return parameter;
		}
		public void setParameter(String parameter) {
			this.parameter = parameter;
		}
		public String getInit() {
			return init;
		}
		public void setInit(String init) {
			this.init = init;
		}
		public List<TemplGraphNode> getTemplGraphNodes() {
			return templGraphNodes;
		}
		public void setTemplGraphNodes(List<TemplGraphNode> templGraphNodes) {
			this.templGraphNodes = templGraphNodes;
		}
		
		public void addTemplGraphNode(TemplGraphNode node) {
			this.templGraphNodes.add(node);
		}

	}
	
	/////节点类
	public static class TemplGraphNode {
		public String name="";
		public String id="";
		public String invariant="";
		public String style="";       //location or branchpoint or committed  urgent?
		public boolean flag=false;
		public List<TemplTransition> inTransitions=new ArrayList<TemplTransition>();
		public List<TemplTransition> outTransitions=new ArrayList<TemplTransition>();
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getInvariant() {
			return invariant;
		}
		public void setInvariant(String invariant) {
			this.invariant = invariant;
		}
		public String getStyle() {
			return style;
		}
		public void setStyle(String style) {
			this.style = style;
		}
		public boolean isFlag() {
			return flag;
		}
		public void setFlag(boolean flag) {
			this.flag = flag;
		}
		public List<TemplTransition> getInTransitions() {
			return inTransitions;
		}
		public void setInTransitions(List<TemplTransition> inTransitions) {
			this.inTransitions = inTransitions;
		}
		public List<TemplTransition> getOutTransitions() {
			return outTransitions;
		}
		public void setOutTransitions(List<TemplTransition> outTransitions) {
			this.outTransitions = outTransitions;
		}
	}
	
	/////边类
	public static class TemplTransition {
		public String source="";
		public String target="";
		public TemplGraphNode node=new TemplGraphNode();
		public String assignment="";
		public String synchronisation="";
		public String guard="";
		public String probability="";     //source or target中有branchpoint则有这一项
		public String getSource() {
			return source;
		}
		public void setSource(String source) {
			this.source = source;
		}
		public String getTarget() {
			return target;
		}
		public void setTarget(String target) {
			this.target = target;
		}
		public TemplGraphNode getNode() {
			return node;
		}
		public void setNode(TemplGraphNode node) {
			this.node = node;
		}
		public String getAssignment() {
			return assignment;
		}
		public void setAssignment(String assignment) {
			this.assignment = assignment;
		}
		public String getSynchronisation() {
			return synchronisation;
		}
		public void setSynchronisation(String synchronisation) {
			this.synchronisation = synchronisation;
		}
		public String getGuard() {
			return guard;
		}
		public void setGuard(String guard) {
			this.guard = guard;
		}
		public String getProbability() {
			return probability;
		}
		public void setProbability(String probability) {
			this.probability = probability;
		}
	}



}
