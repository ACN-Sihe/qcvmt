package com.springMVC.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@SequenceGenerator(name="log",sequenceName="log_seq")
@Table(name = "T_SHOWLOG")
public class ShowLog {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="log")
	@Column(name = "userlogid" , length = 7)
	private int id;
	
	@Column(name = "USERID" , length = 7)
	private int userid;
	
	@Column(name = "USERNAME" , length = 20)
	private String username;

	@Column(name = "QCID" , length = 20)
	private String qcid;
	
	@Column(name = "LOGINTIME" , length = 20)
	private String loginTime;
	
	@Column(name = "OPERATION" , length = 15)
	private String operation;

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public String getQcid() {
		return qcid;
	}

	public void setQcid(String qcid) {
		this.qcid = qcid;
	}

	public String getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(String loginTime) {
		this.loginTime = loginTime;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
}
