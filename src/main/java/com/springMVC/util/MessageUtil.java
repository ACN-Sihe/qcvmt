package com.springMVC.util;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.i18n.SessionLocaleResolver;

public class MessageUtil {

	public String getMessage(String messageCode, HttpServletRequest request){
	     ResourceBundle messages;  
	     Locale locale=(Locale)request.getSession().getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
	     messages = ResourceBundle.getBundle("messages", locale);   
	     return messages.getString(messageCode);
	}
	
	
	
	public static void main(String[] args){
	  
		 ResourceBundle messages;   
		 messages = ResourceBundle.getBundle("messages", Locale.TRADITIONAL_CHINESE);   
		 System.out.println(messages.getString("username"));   
		 System.out.println(messages.getString("password"));   
		 System.out.println(messages.getString("role"));  
			
	}
}
