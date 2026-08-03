package com.springMVC.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookiesUtil {
	
//	static PropertiesUtil s = new PropertiesUtil();
	//设置cookie有效期是两个星期，根据需要自定义         
	private final static int cookieMaxAge = Integer.parseInt(PropertiesUtil.getPropertiesValue("cookieMaxAge"));
	
	public void createCookie(HttpServletResponse response, String key, String value){

        Cookie c = new Cookie(key,value) ;
        //设定有效时间  以s为单位
        c.setMaxAge(cookieMaxAge) ;
        //设置Cookie路径和域名
        c.setPath("/") ;
      //  c.setDomain(".zl.org") ;  //域名要以“.”开头
        //发送Cookie文件
        response.addCookie(c) ;


	}
	
	public boolean findCookieFile(HttpServletRequest request, String key){
		
		Cookie cookies[] = request.getCookies() ;
	    Cookie c = null ;
	    if(cookies != null && cookies.length>0){
	    	for(int i=0;i<cookies.length;i++){
	    		c = cookies[i] ;
		        if(c.getName().equals(key)){
		             return true;
		           }
		       }
	    }else{
	    	return false;
	    }
	    return false;
	}
	
	public String getCookieValueByKey(HttpServletRequest request, String key){
		
		Cookie cookies[] = request.getCookies() ;
        Cookie c = null ;
        if(cookies != null && cookies.length>0){
            for(int i=0;i<cookies.length;i++){
               c = cookies[i] ;
               if(c.getName().equals(key)){
            	   return c.getValue();
               }
            }
        }
        return "";
	}
	
	public void updateCookie(HttpServletRequest request,HttpServletResponse response, String key, String value){
		//修改Cookie
		Cookie cookies[] = request.getCookies() ;
		Cookie c = null ;
		if(cookies != null && cookies.length>0){
			for(int i=0;i<cookies.length;i++){
				c = cookies[i] ;
				if(c.getName().equals(key)){
					c.setValue(value) ;
					c.setMaxAge(cookieMaxAge) ;
					response.addCookie(c) ;     //修改后，要更新到浏览器中     

				}
			}
		}
	}
	
	public void deleteCookie(){
		
	}
	
}
