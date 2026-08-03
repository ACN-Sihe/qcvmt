package com.springMVC.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "T_CELLMATRIX")
public class CellMatrix {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "matrixid")
	private Integer id;
	
	@Column(name = "cmtype" , length = 4)
	private String type;
	
//	@Column(name = "BAY" , length = 10)
//	private String bay;
	
	@Column(name = "cmrow" , length = 4)
	private String row;
	
	@Column(name = "cmtier" , length = 4)
	private String tier;
	
	@Column(name = "active" , length = 2)
	private String active;// 
	
	@Transient
	private String tierStart;
	
	@Transient
	private String tierEnd;	
	
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

	public String getTier() {
		return tier;
	}
	public void setTier(String tier) {
		this.tier = tier;
	}
	public String getActive() {
		return active;
	}
	public void setActive(String active) {
		this.active = active;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getTierStart() {
		return tierStart;
	}

	public void setTierStart(String tierStart) {
		this.tierStart = tierStart;
	}

	public String getTierEnd() {
		return tierEnd;
	}

	public void setTierEnd(String tierEnd) {
		this.tierEnd = tierEnd;
	}
}
