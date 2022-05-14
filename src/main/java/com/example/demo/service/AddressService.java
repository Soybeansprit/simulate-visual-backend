package com.example.demo.service;

public class AddressService {
	public final static String MODEL_FILE_PATH= "D:\\postgraduate\\项目\\论文\\DATE2022\\file\\taps_simu_visualization\\case\\";
	public final static String UPPAAL_PATH="D:\\tools\\uppaal-4.1.24\\uppaal-4.1.24\\bin-Windows\\";
	public final static String IFD_FILE_PATH="D:\\workspace\\ifdFile\\";
	public final static String IFD_FILE_NAME="ifd.dot";
	public final static String SIMULATE_RESULT_FILE_PATH="D:\\workspace\\resultFile\\";
	public final static String VIDEO_PATH="D:\\workspace\\videoFile\\";
	public final static String CONVERT_TOOL_PATH="D:\\workspace\\c2t\\c2t\\";
	
//	public final static String MODEL_FILE_PATH= "/root/TAPs-Visual/workspace/modelFile/";
//	public final static String UPPAAL_PATH="/root/TAPs/workspace/uppaal64-4.1.24/bin-Linux/";
//	public final static String IFD_FILE_PATH="/root/TAPs-Visual/workspace/ifdFile/";
//	public final static String IFD_FILE_NAME="ifd.dot";
//	public final static String SIMULATE_RESULT_FILE_PATH="/root/TAPs-Visual/workspace/resultFile/";
//	public final static String VIDEO_PATH="/root/public_html/TAPs-Visual/assets/";
	
	public final static String ONTOLOGY_FILE_NAME="environment_model.xml";
	public final static String DEVICE_POSITION_INFORMATION_FILE_NAME="deviceInstances.properties";

	public static String changed_model_file_Name;
	public static String best_model_file_name;
	public static String getBest_model_file_name() {
		return best_model_file_name;
	}
	public static void setBest_model_file_name(String initModelFileName) {
		AddressService.best_model_file_name = initModelFileName.substring(0, initModelFileName.lastIndexOf(".xml"))+"-scenario-best.xml";
	}
	private AddressService() {
		
	}
	public final static void setChangedModelFileName(String initModelFileName) {
		changed_model_file_Name=initModelFileName.substring(0, initModelFileName.lastIndexOf(".xml"))+"-changed.xml";
	}
}
