package com.springMVC.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@SequenceGenerator(name="user",sequenceName="user_seq")  
@Table(name = "T_USER")
public class User {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="user")
	@Column(name = "userid" , length = 7)
	private Integer id;
	
	@Column(name = "QCID" , length = 20)
	private String qcid;
	
	@Column(name = "NAME" , length = 20)
	private String username;
	
	@Column(name = "PASSWORD" , length = 6)
	private String password;
	
	@Column(name = "ROLE" , length = 10)
	private String role;
	
	@Column(name = "PARENT" , length = 10)
	private String parent;// who create him
	
	@Column(name = "CREATETIME" , length = 14)
	private String createtime;
	
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public String getCreatetime() {
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getQcid() {
		return qcid;
	}
	public void setQcid(String qcid) {
		this.qcid = qcid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
}
