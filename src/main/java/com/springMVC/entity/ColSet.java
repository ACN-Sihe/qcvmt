package com.springMVC.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@SequenceGenerator(name="colset",sequenceName="colset_seq")  
@Table(name = "T_COLSET")
public class ColSet {
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="colset")
	@Column(name = "colsetid")
	private Integer id;
	
	@Column(name = "COLOR" , length = 15)
	private String color;
	
	@Column(name = "BOXCASE" , length = 10)
	private String boxcase;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getBoxcase() {
		return boxcase;
	}

	public void setBoxcase(String boxcase) {
		this.boxcase = boxcase;
	}
	
	
}
