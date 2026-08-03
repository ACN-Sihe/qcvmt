package com.springMVC.dao;

import java.util.List;

import com.springMVC.entity.*;

public interface VesselDao {

	
	public void save(Vessel vessel);
	
	public PageManage getAllVessel(int offset);
	
	public void deleteById(int id);
	
	public Vessel getVesselById(int id);
	
	public void update(Vessel vessel);
	
	public boolean saveOrUpdateVessel(Vessel vessel);

	public boolean saveOrUpdateVessel(List vesselList);

	public List getVesselByCondition(String vesselid,String deck_hold, String bay);
	
	public PageManage searchVessel(final int offset, String key);
	
	public String getN4VesselNameById(String vesselid);

	public List getVesselListByName(String vesselid);

	public PageManage getAllVesselCol(final int offset);

	public PageManage searchVesselCol(final int offset, String key);

	public VesselCol getVesselColById(int id);

	public boolean saveOrUpdateVesselCol(VesselCol vesselCol);

	public void deleteVesselColById(int id);

	public PageManage getAllVesselRefuel(final int offset);

	public PageManage searchVesselRefuel(final int offset, String key);

	public VesselRefuel getVesselRefuelById(Integer id);

	public void deleteVesselRefuelById(Integer id);

	public boolean saveOrUpdateVesselRefuel(VesselRefuel vr);

	public void saveOperationLog(OperationLog log);
}
