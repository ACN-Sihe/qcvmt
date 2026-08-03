package com.springMVC.dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.springMVC.entity.PageManage;
import com.springMVC.entity.ShowLog;
import com.springMVC.entity.User;
import com.springMVC.util.Constants;
import com.springMVC.util.PropertiesUtil;
import com.springMVC.util.WebUtil;

@Repository
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class UserDaoImpl implements UserDao{
	
	@Resource
	private HibernateTemplate hibernateTemplate;
	
	@Resource
	private JdbcTemplate jdbcTemplate;
	
	
	@Override
	public User login(User user) {
		String hql = "from User WHERE username=? and password=? order by username";
		List list = this.hibernateTemplate.find(hql, new String[]{user.getUsername(),user.getPassword()});
		
		if(list !=null && list.size()>0){
			User u = (User) list.iterator().next();
			return u;
		}
		return null;
	}

	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void save(User user) {
		this.hibernateTemplate.save(user);
		
	}

	@Override
	public PageManage getAllUser(final int offset) {
		String countHql  = "select count(*) from User order by username";
		int countTotal =  DataAccessUtils.intResult(this.hibernateTemplate.find(countHql));
		
		final String hql = "from User order by username";

		List listDatas = this.hibernateTemplate.executeFind(new HibernateCallback(){
			 public Object doInHibernate(Session session) throws HibernateException, SQLException{
				List ls = session.createQuery(hql).setFirstResult(offset).setMaxResults(10).list();
				 return ls;
			 }
		});
		
		PageManage page  = new PageManage();
		page.setTotal(countTotal);
		page.setDatas(listDatas);
		page.setOffset(offset);
		page.setPagesize(10);
		
		return page;
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void deleteById(int id) {
		User user = new User();
		user.setId(id);
		this.hibernateTemplate.delete(user);
		
		String hql = "from ShowLog where userid=?" ;
        List list = this.hibernateTemplate.find(hql, id);
		this.hibernateTemplate.deleteAll(list);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void update(User user) {
		User u = this.hibernateTemplate.get(User.class, user.getId());
		u.setPassword(user.getPassword());
		u.setRole(user.getRole());
		u.setQcid(user.getQcid());
		this.hibernateTemplate.saveOrUpdate(u);
	}

	@Override
	public User getUserById(int id) {
		String sqlString = "from User u where u.id=?";
		User user = (User) this.hibernateTemplate.find(sqlString, id).iterator().next();
		
		return user;
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void add(ShowLog log) {
		this.hibernateTemplate.saveOrUpdate(log);
		
	}

	@Override
	public User getUserByName(String username) {
		String hql = "from User u where u.username=?";
		User user = null;
		List list = this.hibernateTemplate.find(hql, username);
		if(list !=null && list.size() >0){
			user = (User) list.iterator().next();
		}
		
		return user;
	}

	@Override
	public PageManage getUserLog(final int id,final int offset) {
		final String start = WebUtil.getPreMonthTime();
		final String end = WebUtil.getTime();
		String countHql  = "select count(*) from ShowLog where userid =? and (loginTime between ? and ?) order by loginTime desc";
		int countTotal =  DataAccessUtils.intResult(this.hibernateTemplate.find(countHql,new Object[]{id,start,end}));
		
		final String hql = "from ShowLog where userid =? and (loginTime between ? and ?) order by loginTime desc";
		List listDatas = this.hibernateTemplate.executeFind(new HibernateCallback(){
			 public Object doInHibernate(Session session) throws HibernateException, SQLException{
				 Query query = session.createQuery(hql);
				 query.setInteger(0, id);
				 query.setString(1, start);
				 query.setString(2, end);
				 List ls = query.setFirstResult(offset).setMaxResults(10).list();
				 return ls;
			 }
		});
		
		PageManage page  = new PageManage();
		page.setTotal(countTotal);
		page.setDatas(listDatas);
		page.setOffset(offset);
		page.setPagesize(10);
		page.setUserid(id);
		
		return page;
	}

	@Override
	public List getUserLogByPeriod(String fromTime, String toTime) throws ParseException{
		final String hql = "from ShowLog where qcid is not null and (loginTime between ? and ?) order by loginTime desc";
		final String newFromTime = WebUtil.DataFormatTransfer(fromTime);
		final String newToTime = WebUtil.DataFormatTransfer(toTime);
		List listDatas = this.hibernateTemplate.executeFind(new HibernateCallback(){
			 public Object doInHibernate(Session session) throws HibernateException, SQLException{
				 Query query = session.createQuery(hql);
				 query.setString(0, newFromTime);
				 query.setString(1, newToTime);
				 List ls = query.list();
				 return ls;
			 }
		});
		
		return listDatas;
	}
	
	@Override
	public String logout(HttpServletRequest request) {
		HttpSession hsession=request.getSession();
		User u = (User) hsession.getAttribute(Constants.USER_LOGIN);
		
		ShowLog logs = new ShowLog();
		try{
			logs.setQcid(u.getQcid());
			if("USER".equals(u.getRole())){
				String qc = (String) hsession.getAttribute(Constants.QC_ID);
				logs.setQcid(qc);
			}
			logs.setUserid(u.getId());
			logs.setUsername(u.getUsername());
			logs.setLoginTime(WebUtil.getTime());
			logs.setOperation("LOGOUT");
			this.hibernateTemplate.saveOrUpdate(logs);
			
			return "yes";
		}catch(Exception e){
		    return "no";	
		}
	}
	
	@Override
	public List queryQcId() {
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append(" select distinct xpow.name as qcid from MN4O_QC_xps_pointofwork xpow ");
		sqlBuffer.append(" where xpow.yard IN (select gkey from MN4O_QC_argo_yard WHERE fcy_gkey ");
		sqlBuffer.append("in ( select gkey from MN4O_QC_argo_facility where name in ('"+PropertiesUtil.getPropertiesValue("company")+"')))"); 
				
		List<Map<String,Object>> list = this.jdbcTemplate.queryForList(sqlBuffer.toString());
		List ls = new ArrayList();
		if(list !=null && list.size() > 0 ){
			for(Map<String,Object> item : list){
				String name = (String) item.get("qcid");
				ls.add(name);
			}
		}
		return ls;
	}

	@Override
	public String queryFacilityByQcId(String qcid) {
		StringBuffer sqlBuffer = new StringBuffer();
		PropertiesUtil properties = new PropertiesUtil();
		String facility="";
		sqlBuffer.append(" select af.name as facility from MN4O_QC_argo_facility af,MN4O_QC_argo_yard ay,MN4O_QC_xps_pointofwork xpow ");
		sqlBuffer.append(" where af.gkey=ay.fcy_gkey and xpow.yard=ay.gkey ");
		sqlBuffer.append(" and xpow.name = ? "); 
				
		List<Map<String,Object>> list = this.jdbcTemplate.queryForList(sqlBuffer.toString(),new String[]{qcid});		
		if(list !=null && list.size() > 0 ){
			for(Map<String,Object> item : list){
				facility = (String) item.get("facility");				
			}
		}
		return facility;
	}
	
}
