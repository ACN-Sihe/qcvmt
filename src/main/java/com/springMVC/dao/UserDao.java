package com.springMVC.dao;

import java.text.ParseException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.springMVC.entity.PageManage;
import com.springMVC.entity.ShowLog;
import com.springMVC.entity.User;

public interface UserDao {

	public User login(User user);
	
	public void save(User user);
	
	public PageManage getAllUser(int offset);
	
	public void deleteById(int id);
	
	public User getUserById(int id);
	
	public void update(User user);
	
	public void add(ShowLog log);
	
	public User getUserByName(String username);
		
	public PageManage getUserLog(int id,int offset);
	
	public List getUserLogByPeriod(String fromTime, String toTime) throws ParseException;
	
	public String logout(HttpServletRequest request);
	
	public List queryQcId(); 
	
	public String queryFacilityByQcId(String qcid) ;
	
}
