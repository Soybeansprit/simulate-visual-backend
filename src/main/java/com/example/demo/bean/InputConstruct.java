package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;


public class InputConstruct {


	public static class EnvironmentRule{
		private EnvironmentModel environmentModel;
		private List<Rule> rules=new ArrayList<Rule>();
		public EnvironmentModel getEnvironmentModel() {
			return environmentModel;
		}
		public void setEnvironmentModel(EnvironmentModel environmentModel) {
			this.environmentModel = environmentModel;
		}
		public List<Rule> getRules() {
			return rules;
		}
		public void setRules(List<Rule> rules) {
			this.rules = rules;
		}
		public EnvironmentRule(EnvironmentModel environmentModel, List<Rule> rules) {
			super();
			this.environmentModel = environmentModel;
			this.rules = rules;
		}
		public EnvironmentRule() {
			super();
		}
		
	}
	

	
	
	public static class SceneEnvironmentProperty{
		private List<Scene> scenes=new ArrayList<>();
		private EnvironmentModel environmentModel;
		private List<String> properties=new ArrayList<>();
		private List<Rule> rules=new ArrayList<>();
				
		public List<Rule> getRules() {
			return rules;
		}
		public void setRules(List<Rule> rules) {
			this.rules = rules;
		}
		public List<String> getProperties() {
			return properties;
		}
		public void setProperties(List<String> properties) {
			this.properties = properties;
		}
		public List<Scene> getScenes() {
			return scenes;
		}
		public void setScenes(List<Scene> scenes) {
			this.scenes = scenes;
		}
		public EnvironmentModel getEnvironmentModel() {
			return environmentModel;
		}
		public void setEnvironmentModel(EnvironmentModel environmentModel) {
			this.environmentModel = environmentModel;
		}
		
	}
	
	
	public static class SceneEnvironmentRule{
		private Scene scene;
		private EnvironmentModel environmentModel;
		private List<Rule> rules=new ArrayList<>();
		public Scene getScene() {
			return scene;
		}
		public void setScene(Scene scene) {
			this.scene = scene;
		}
		public EnvironmentModel getEnvironmentModel() {
			return environmentModel;
		}
		public void setEnvironmentModel(EnvironmentModel environmentModel) {
			this.environmentModel = environmentModel;
		}
		public List<Rule> getRules() {
			return rules;
		}
		public void setRules(List<Rule> rules) {
			this.rules = rules;
		}
		
	}
	
	
}
