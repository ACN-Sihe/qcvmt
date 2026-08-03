package com.springMVC.filter;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.springMVC.util.Constants;

public class SecurityInterceptor implements HandlerInterceptor{
	 private List<String> excludedUrls; 
	 
	@Override
	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {
	    String requestUri = request.getRequestURI();  
	    for (String url : excludedUrls) {       
	    	if (requestUri.indexOf(url) !=-1) {
	    		return true;       
	    		}     
	    	}
	    
	    HttpSession session = request.getSession();    
	    if (session.getAttribute(Constants.USER_LOGIN) == null) { 
	    		
		    session.setAttribute("error", "Please login first!"); 
		    	
		    response.sendRedirect(request.getContextPath()+"/index.jsp");	    
	    }else{
	    	return true;
	    }
		return false;
	}

	public List<String> getExcludedUrls() {
		return excludedUrls;
	}

	public void setExcludedUrls(List<String> excludedUrls) {
		this.excludedUrls = excludedUrls;
	}

	
}
