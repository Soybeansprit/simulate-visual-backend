package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class IFDGraph {
	public static class GraphNode {
		
		private String name="";  ///////节点名 trigger1
		private String shape=""; ///////节点形状 oval
		private String fillcolor=""; ////节点填充色 
		private String label="";  //////节点内容 
		public boolean flag=false;
		private List<GraphNodeArrow> pNodeList=new ArrayList<GraphNodeArrow>();
		private List<GraphNodeArrow> cNodeList=new ArrayList<GraphNodeArrow>();	
			
		
		public void addpNodeList(GraphNodeArrow pNodeList) {
			this.pNodeList.add(pNodeList);
		}
		
		public void addcNodeList(GraphNodeArrow cNodeList) {
			this.cNodeList.add(cNodeList);
		}


		public String getName() {
			return name;
		}


		public void setName(String name) {
			this.name = name;
		}


		public String getShape() {
			return shape;
		}


		public void setShape(String shape) {
			this.shape = shape;
		}


		public String getFillcolor() {
			return fillcolor;
		}


		public void setFillcolor(String fillcolor) {
			this.fillcolor = fillcolor;
		}


		public String getLabel() {
			return label;
		}


		public void setLabel(String label) {
			this.label = label;
		}


		public List<GraphNodeArrow> getpNodeList() {
			return pNodeList;
		}


		public void setpNodeList(List<GraphNodeArrow> pNodeList) {
			this.pNodeList = pNodeList;
		}


		public List<GraphNodeArrow> getcNodeList() {
			return cNodeList;
		}


		public void setcNodeList(List<GraphNodeArrow> cNodeList) {
			this.cNodeList = cNodeList;
		}

	}
	
	public static class GraphNodeArrow {
		/////节点的边
		private GraphNode graphNode=new GraphNode();
		private String label="";
		private String color="";
		private String style="";
		public GraphNode getGraphNode() {
			return graphNode;
		}
		public void setGraphNode(GraphNode graphNode) {
			this.graphNode = graphNode;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getColor() {
			return color;
		}
		public void setColor(String color) {
			this.color = color;
		}
		public String getStyle() {
			return style;
		}
		public void setStyle(String style) {
			this.style = style;
		}
	}


}
