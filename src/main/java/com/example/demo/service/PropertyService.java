package com.example.demo.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.springframework.stereotype.Service;
@Service
public class PropertyService {

	public static Properties getProperties(String filePath,String propertyFileName) throws FileNotFoundException, IOException {
		Properties properties=new Properties();
		properties.load(new FileInputStream(filePath+propertyFileName));
		return properties;
	}
}
