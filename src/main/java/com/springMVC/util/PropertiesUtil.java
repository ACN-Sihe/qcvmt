package com.springMVC.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
	private static Properties properties;
	
	public PropertiesUtil(){		
	}
	
	static{		
		InputStream in =null;
		try{
			in = PropertiesUtil.class.getResourceAsStream("/system.properties");
			properties = new Properties();
			properties.load(in);
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(in !=null){
				try{
					in.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
	}
		
	public static String getPropertiesValue(String key){
		String value = (String) properties.get(key);
		if(value ==null){
			return "";
		}
		return value;
	}

	public static void main(String[] args){
		
		int company  = Integer.parseInt(PropertiesUtil.getPropertiesValue("cookieMaxAge"));
		System.out.println("\u7528\u6237\u540d :"+company);
	}
}
