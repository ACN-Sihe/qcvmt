package com.springMVC.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CELL")
public class Cell {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
	private Integer id;
	
//	@Column(name = "DECK_HOLD" , length = 10)
//	private String deck_hold;
	
	@Column(name = "BAY" , length = 10)
	private String bay;
	
	@Column(name = "ROW" , length = 10)
	private String row;
	
	@Column(name = "TIER" , length = 10)
	private String tier;
	
	@Column(name = "CELL_INFO" , length = 10)
	private String cellinfo;// 
	
	@Column(name = "TYPE" , length = 10)
	private String type;// 
	
//	@Column(name = "QCID" , length = 4)
//	private String qcid;
//	
//	@Column(name = "VESSELID" , length = 4)
//	private String vesselid;
//	
//	@Column(name = "CREATEUSER" , length = 10)
//	private String createuser;
//	
//	@Column(name = "CREATETIME" , length = 14)
//	private String createtime;
	
	public String getBay() {
		return bay;
	}
	public void setBay(String bay) {
		this.bay = bay;
	}
//	public String getCreatetime() {
//		return createtime;
//	}
//	public void setCreatetime(String createtime) {
//		this.createtime = createtime;
//	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getRow() {
		return row;
	}
	
	public void setRow(String row) {
		this.row = row;
	}
//	public void setQcid(String qcid) {
//		this.qcid = qcid;
//	}
	public String getTier() {
		return tier;
	}
	public void setTier(String tier) {
		this.tier = tier;
	}
	public String getCellinfo() {
		return cellinfo;
	}
	public void setCellinfo(String cellinfo) {
		this.cellinfo = cellinfo;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
