package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class OutputConstruct {
	public static class EnvironmentStatic{
		private EnvironmentModel environmentModel;
		private StaticAnalysisResult staticAnalysisResult;
		
		public EnvironmentStatic() {
			super();
		}
		public EnvironmentStatic(EnvironmentModel environmentModel, StaticAnalysisResult staticAnalysisResult) {
			super();
			this.environmentModel = environmentModel;
			this.staticAnalysisResult = staticAnalysisResult;
		}
		public EnvironmentModel getEnvironmentModel() {
			return environmentModel;
		}
		public void setEnvironmentModel(EnvironmentModel environmentModel) {
			this.environmentModel = environmentModel;
		}
		public StaticAnalysisResult getStaticAnalysisResult() {
			return staticAnalysisResult;
		}
		public void setStaticAnalysisResult(StaticAnalysisResult staticAnalysisResult) {
			this.staticAnalysisResult = staticAnalysisResult;
		}
		
	}
	

}
