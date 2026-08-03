package com.springMVC.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.springMVC.entity.*;
import com.springMVC.util.GeneralException;

public interface CellDao {

	public HashMap getCells(String vesselid,String qcid)throws GeneralException;
	public List getColSet();
	public BaySize getBaySize() throws Exception;
	public void updateCellMatrix(BaySize baySize) throws  Exception;
	public boolean saveOrUpdateColSet(ColSet colSet);
	public boolean delColSet(int id);
	public ColSet getColSetByBoxcase(String boxcase);
	public ColSet getColSetById(int id);
	
	public PageManage getAllCol(int offset);
}
