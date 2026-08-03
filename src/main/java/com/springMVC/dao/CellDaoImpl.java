package com.springMVC.dao;

import com.springMVC.entity.*;
import com.springMVC.util.GeneralException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class CellDaoImpl implements CellDao {
    private static final Log LOG = LogFactory.getLog(CellDaoImpl.class);

    public static HashMap cellMatrixHM = new HashMap();
    @Resource
    private HibernateTemplate hibernateTemplate;

    @Resource
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        try {
            //	org.apache.log4j.spi.ThrowableInformation
//           ApplicationContext ctx = new ClassPathXmlApplicationContext("springMVC-servlet.xml");  
//           VesselDao vesselDao = (VesselDao)ctx.getBean("vesselDaoImpl");
//		   CellDao celldao = (CellDao)ctx.getBean("cellDaoImpl");	
//           UserDao userDao = (UserDao)ctx.getBean("userDaoImpl");	
//			"FORM_TRUCK_LADEN_TO_DEST".equals(Constants.FORM_TRUCK_LADEN_TO_DEST)
//		    //for test
//		   celldao.getCells("", "QC83");
//           Vessel vessel=new Vessel ();
//           vessel.setVesselid("V123456");
//           vessel.setDeck_hold("H");
//           vessel.setBay("17H");
//           vessel.setRowStart("1");
//           vessel.setRowEnd("19");
//           vessel.setTierStart("82");
//           vessel.setTierEnd("90");
//           vesselDao.save(vessel);
            for (int i = 1; i <= 5; i++) {
                System.out.println("i=" + i);

                if (i > 3) break;

            }

//           List alist=userDao.queryQcId();
//           org.springframework.orm.hibernate3.SpringSessionContext sc=(org.springframework.orm.hibernate3.SpringSessionContext)ctx.getBean("springSessionContext");
//           org.hibernate.cfg.Configuration cofig= (org.hibernate.cfg.Configuration)ctx.getBean("configuration");
//           org.hibernate.impl.SessionFactoryImpl sessionFactory = (org.hibernate.impl.SessionFactoryImpl)ctx.getBean("sessionFactory");	
            System.out.println((57 % 2 != 0) + " \u8acb\u6aa2\u67e5\u6587\u4ef6\u5167\u5bb9\u548c\u683c\u5f0f\u662f\u5426\u6b63\u78ba.: ");


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" getMessage(): " + e.getMessage());
        }
    }


    @Override
    public HashMap getCells(String vessel, String qcid) throws GeneralException {
        //get min order by the qcid
        HashMap cellHashMap = getQorder(qcid);

//		check if the sequence include more than 2 bays. 
        checkSequenceList(cellHashMap);

        //get the sequence info by the qcid and order number
        cellHashMap = getSequenceList(cellHashMap);
        String qdeck = (String) cellHashMap.get("DeckHold");
        String minBay = (String) cellHashMap.get("MinBay");
        String vesselid = (String) cellHashMap.get("Vessl");
        HashMap tierHM = (HashMap) cellHashMap.get("TierHM");

        //get the ROB info
        cellHashMap = getROBList(cellHashMap);

        /*Begin PCR-VMT-000004 Tony Add*/
        //if current Bay is even, then check whether there is twenty containers under it
        String qType = (String) cellHashMap.get("QType");
        if (qType != null && "DISCH".equals(qType)) {
            String maxBay = (String) cellHashMap.get("MaxBay");
            if (minBay != null && maxBay != null && minBay.equals(maxBay) && (Integer.parseInt(minBay) % 2 == 0)) {
                cellHashMap = getTwentyUnitList(cellHashMap, qcid, vesselid, minBay);
            }
        }
        /*End PCR-VMT-000004 Tony Add*/

        //get the cellMatrixistList by deck hold type
        List cellMatrixistList = getCellMatrixFromnN4(vesselid, minBay, qdeck);

        //get the first line of the bay
        String firstLine = getFirstLine(cellMatrixistList, tierHM);

        //build the table of bay
        String cellTable = buildBay(cellMatrixistList, cellHashMap);

        //build the cell table by firstLine and cellTable
        cellTable = firstLine + cellTable;

        cellHashMap.put("cellTable", cellTable);

//		LOG.debug(" cellTable(): "+cellTable); 
//		System.out.println(" cellTable(): "+cellTable); 

        return cellHashMap;
    }

    public List getCellMatrixFromnN4(String vesselid, String bay, String qdeck) throws GeneralException {

        List cellMatrixistList = null;
        List returnCellMatrixistList = new ArrayList();
        List<Map<String, Object>> vesselList = null;
        String rowStart = "0", rowEnd = "0";
        String tierStart = "0";
        String tierEnd = "0";
        int tier = 0;
        int bayMinus1 = Integer.parseInt(bay) - 1;

        try {
            StringBuffer n4sql = new StringBuffer();
            n4sql.append(" select t.rowStart as rowStart,t.rowEnd as rowEnd, ");
            n4sql.append(" t.tierStart as tierStart,t.tierEnd as tierEnd ");
            n4sql.append(" from MN4O_QC_argo_carrier_visit a,MN4O_QC_vsl_vsl_visit_details b,MN4O_QC_vsl_vessels c,t_vessel t ");
            n4sql.append(" where a.cvcvd_gkey = b.vvd_gkey and b.vessel_gkey = c.gkey ");
            n4sql.append(" and t.vesselid=c.name ");
            n4sql.append(" and a.phase not in ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED') ");
            n4sql.append(" and a.id= ?  ");
            n4sql.append(" and ( t.bay= ? or t.bay= ?) ");
            n4sql.append(" and t.deck_hold= ? ");

            vesselList = jdbcTemplate.queryForList(n4sql.toString(), new Object[]{vesselid, bay, "" + bayMinus1, qdeck});

        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getCellMatrixFromnN4()-1");
            throw new GeneralException("db_query_time_out");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getCellMatrixFromnN4()");
            throw new GeneralException("cannot_get_connection");
        } catch (Exception e) {
            LOG.debug(e.toString());
            e.printStackTrace();
            throw new GeneralException("error_query_db_error");
        }
        //check if the cellMatrixistList is null
        if (vesselList == null || vesselList.size() == 0) {
            cellMatrixistList = getCellMatrix(qdeck);
        } else {
            try {
                for (Map<String, Object> rs : vesselList) {
                    rowStart = rs.get("rowStart").toString();
                    rowEnd = rs.get("rowEnd").toString();
                    tierStart = rs.get("tierStart").toString();
                    tierEnd = rs.get("tierEnd").toString();
                    break;
                }
                //need check if the data is the number
                tier = tierEnd.compareTo(tierStart);

                StringBuffer sql2 = new StringBuffer();
                if (rowStart.length() == 1) {
                    rowStart = "0" + rowStart;
                }
                if (rowEnd.length() == 1) {
                    rowEnd = "0" + rowEnd;
                }
                sql2.append(" from CellMatrix ");
                sql2.append(" where type= ?  ");
                sql2.append(" and row between '" + rowStart + "' and '" + rowEnd + "'");
                sql2.append(" order by id desc ");
                cellMatrixistList = this.hibernateTemplate.find(sql2.toString(), new Object[]{qdeck});

            } catch (RecoverableDataAccessException e) {
                LOG.info("db_query_time_out: getCellMatrixFromnN4()-2");
                throw new GeneralException("db_query_time_out");
            } catch (Exception e) {
                cellMatrixistList = getCellMatrix(qdeck);
            }
        }

        int size = cellMatrixistList.size();
        if (size <= 0) {
            cellMatrixistList = getCellMatrix(qdeck);
            size = cellMatrixistList.size();
        }

        for (int i = 0; i < size; i++) {
            CellMatrix cell = (CellMatrix) cellMatrixistList.get(i);
            CellMatrix newCell = new CellMatrix();
            newCell.setId(cell.getId());
            newCell.setType(cell.getType());
            newCell.setRow(cell.getRow());
            if (tier > 0) {
                newCell.setTier("" + tier);
            } else {
                newCell.setTier(cell.getTier());
            }

            newCell.setTierStart(tierStart);
            newCell.setTierEnd(tierEnd);
            returnCellMatrixistList.add(newCell);
        }

        return returnCellMatrixistList;
    }


    /**
     * get cell matrix from table T_cellmatrix, according to the deckhold type
     */
    public List getCellMatrix(String qdeck) throws GeneralException {
        List cellMatrixistList = null;

        try {
            String hql = " from CellMatrix where type= ? and active ='1' order by id desc ";
            cellMatrixistList = this.hibernateTemplate.find(hql, new Object[]{qdeck});

        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getCellMatrix()");
            throw new GeneralException("db_query_time_out");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getCellMatrix()");
            throw new GeneralException("cannot_get_connection");
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }
        //check if the cellMatrixistList is null
        if (cellMatrixistList == null || cellMatrixistList.size() == 0) {
            throw new GeneralException("DeckHold type " + qdeck + " has no data ");
        }

        return cellMatrixistList;
    }

    /**
     * get minimum order by qcid
     */
    public HashMap getQorder(String qcid) throws GeneralException {
        HashMap hm = new HashMap();

        Object dischOrder = getDischargeOrder(qcid).get("QOrder");
        Object loadOrder = getLoadOrder(qcid).get("QOrder");
        if (dischOrder == null && loadOrder == null) {
            throw new GeneralException("error_no_qc_working");
        } else if (dischOrder == null && loadOrder != null) {
            hm.put("QType", "LOAD");
            hm.put("QOrder", loadOrder.toString());

        } else if (dischOrder != null && loadOrder == null) {
            hm.put("QType", "DISCH");
            hm.put("QOrder", dischOrder.toString());

        } else if (dischOrder != null && loadOrder != null) {
            if (loadOrder.toString().compareTo(dischOrder.toString()) > 0) {
                hm.put("QType", "DISCH");
                hm.put("QOrder", dischOrder.toString());
            } else {
                hm.put("QType", "LOAD");
                hm.put("QOrder", loadOrder.toString());
            }
        }

        //set qcid
        hm.put("QCid", qcid);

        return hm;
    }

    /**
     * get minimum order by qcid in load case
     */
    public HashMap getLoadOrder(String qcid) throws GeneralException {

        StringBuffer sql = new StringBuffer();
        HashMap hm = new HashMap();
        String qorder;
        try {
            sql.append(" select min(iq.qorder) as qorder ");
            sql.append(" from MN4O_QC_inv_wq iq, MN4O_QC_inv_wi iw,MN4O_QC_inv_unit_yrd_visit iuyv, MN4O_QC_inv_unit_fcy_visit iufv, MN4O_QC_inv_unit iu, ");
            sql.append(" MN4O_QC_xps_craneshift xcs,MN4O_QC_xps_pointofwork xpow,MN4O_QC_ref_equipment re,MN4O_QC_inv_goods ig, MN4O_QC_argo_carrier_visit acv ");
            sql.append(" where ");
            sql.append(" iw.work_queue_gkey = iq.gkey and iw.uyv_gkey = iuyv.gkey and iuyv.ufv_gkey = iufv.gkey and iq.pos_locid = acv.id ");
            sql.append(" and iufv.unit_gkey = iu.gkey and iq.first_shift_pkey=xcs.pkey and xcs.owner_pow=xpow.pkey");
            sql.append(" and re.gkey  = iu.eq_gkey and ig.gkey=iu.goods");
            sql.append(" and acv.phase not in ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED') ");
            sql.append(" and iq.qtype in ('LOAD') ");
            sql.append(" and iq.qdeck in ('A', 'B') ");//restrict the scope,except G
            sql.append(" and iq.pos_loctype = 'VESSEL' ");
            sql.append(" and iq.is_blue = '1' ");
            sql.append(" and iw.move_kind != 'YARD' ");
            sql.append(" and iw.move_kind != 'SHFT' ");
            sql.append(" and iw.move_stage = 'COMPLETE' ");
            // 2014-10-16 Added By Leo Begin
            // Revised the condition according to Ben's suggestion to leverage the index
            //sql.append(" and (sysdate-to_date(to_char(iufv.time_move,'yyyy-mm-dd hh24:mi:ss'),'yyyy-mm-dd hh24:mi:ss'))*24*60>0 ");
            //sql.append(" and (sysdate-to_date(to_char(iufv.time_move,'yyyy-mm-dd hh24:mi:ss'),'yyyy-mm-dd hh24:mi:ss'))*24*60<1 ");
            sql.append(" and  iufv.time_move BETWEEN sysdate-1/1440 AND sysdate ");
            // 2014-10-16 Added By Leo End
            sql.append(" and xpow.name= ? ");

            qorder = (String) this.jdbcTemplate.queryForObject(sql.toString(), new Object[]{qcid}, String.class);

        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getLoadOrder()-1");
            throw new GeneralException("db_query_time_out");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getLoadOrder()-1");
            throw new GeneralException("cannot_get_connection");
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }

        if (qorder == null) {
            try {
                StringBuffer sql2 = new StringBuffer();
                sql2.append(" select min(iq.qorder) as qorder ");
                sql2.append(" from MN4O_QC_inv_wq iq, MN4O_QC_inv_wi iw,MN4O_QC_inv_unit_yrd_visit iuyv, MN4O_QC_inv_unit_fcy_visit iufv, MN4O_QC_inv_unit iu, ");
                sql2.append(" MN4O_QC_xps_craneshift xcs,MN4O_QC_xps_pointofwork xpow,MN4O_QC_ref_equipment re,MN4O_QC_inv_goods ig, MN4O_QC_argo_carrier_visit acv ");
                sql2.append(" where ");
                sql2.append(" iw.work_queue_gkey = iq.gkey and iw.uyv_gkey = iuyv.gkey and iuyv.ufv_gkey = iufv.gkey and iq.pos_locid = acv.id ");
                sql2.append(" and iufv.unit_gkey = iu.gkey and iq.first_shift_pkey=xcs.pkey and xcs.owner_pow=xpow.pkey");
                sql2.append(" and re.gkey =  iu.eq_gkey and ig.gkey=iu.goods");
                sql2.append(" and acv.phase not in ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED') ");
                sql2.append(" and iq.qtype in ('LOAD') ");
                sql2.append(" and iq.qdeck in ('A', 'B') ");//restrict the scope,except G
                sql2.append(" and iq.pos_loctype = 'VESSEL' ");
                sql2.append(" and iq.is_blue = '1' ");
                sql2.append(" and iw.move_kind != 'YARD' ");
                sql2.append(" and iw.move_kind != 'SHFT' ");
                sql2.append(" and iw.move_stage != 'COMPLETE' ");
                sql2.append(" and xpow.name= ? ");

                qorder = (String) this.jdbcTemplate.queryForObject(sql2.toString(), new Object[]{qcid}, String.class);

            } catch (RecoverableDataAccessException e) {
                LOG.info("db_query_time_out: getLoadOrder()-2");
                throw new GeneralException("db_query_time_out");
            } catch (CannotGetJdbcConnectionException e) {
                LOG.info("cannot_get_connection: getLoadOrder()-2");
                throw new GeneralException("cannot_get_connection");
            } catch (Exception e) {
                LOG.debug(e.toString());
                throw new GeneralException("error_query_db_error");
            }
        }

        hm.put("QOrder", qorder);
        return hm;
    }

    /**
     * get minimum order by qcid in discharge case
     */
    public HashMap getDischargeOrder(String qcid) throws GeneralException {

        StringBuffer sql = new StringBuffer();
        HashMap hm = new HashMap();
        String qorder;
        try {
            sql.append(" select min(iq.qorder) as qorder ");
            sql.append(" from MN4O_QC_inv_wq iq, MN4O_QC_inv_wi iw,MN4O_QC_inv_unit_yrd_visit iuyv, MN4O_QC_inv_unit_fcy_visit iufv, MN4O_QC_inv_unit iu, ");
            sql.append(" MN4O_QC_xps_craneshift xcs,MN4O_QC_xps_pointofwork xpow,MN4O_QC_ref_equipment re,MN4O_QC_inv_goods ig, MN4O_QC_argo_carrier_visit acv ");
            sql.append(" where ");
            sql.append(" iw.work_queue_gkey = iq.gkey and iw.uyv_gkey = iuyv.gkey and iuyv.ufv_gkey = iufv.gkey and iq.pos_locid = acv.id ");
            sql.append(" and iufv.unit_gkey = iu.gkey and iq.first_shift_pkey=xcs.pkey and xcs.owner_pow=xpow.pkey");
            sql.append(" and re.gkey  = iu.eq_gkey and ig.gkey=iu.goods");
            sql.append(" and acv.phase not in ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED') ");
            sql.append(" and iq.qtype in ('DISCH') ");
            sql.append(" and iq.qdeck in ('A', 'B') ");//restrict the scope,except G
            sql.append(" and iq.pos_loctype = 'VESSEL' ");
            sql.append(" and iq.is_blue = '1' ");
            sql.append(" and iw.move_kind != 'YARD' ");
            sql.append(" and iw.move_kind != 'SHFT' ");
            sql.append(" and iw.move_stage in ('PLANNED', 'NONE')  ");
            sql.append(" and xpow.name= ? ");

            qorder = (String) this.jdbcTemplate.queryForObject(sql.toString(), new Object[]{qcid}, String.class);
        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getDischargeOrder()");
            throw new GeneralException("db_query_time_out");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getDischargeOrder()");
            throw new GeneralException("cannot_get_connection");
        } catch (Exception e) {
            e.printStackTrace();
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }

        hm.put("QOrder", qorder);

        return hm;
    }

    /**
     * check if the sequence include more than 2 bays. and get the max and minimum bay number
     *
     * @param hm
     * @return
     * @throws GeneralException
     */
    public HashMap checkSequenceList(HashMap hm) throws GeneralException {
        List<Map<String, Object>> list = null;
        String qtype = (String) hm.get("QType");
        String qcid = (String) hm.get("QCid");
        String qorder = (String) hm.get("QOrder");

        try {
            StringBuffer sql = new StringBuffer();
            if (qtype.equals("DISCH")) {
                sql = checkDischargeSequenceList(hm);
            } else if (qtype.equals("LOAD")) {
                sql = checkLoadSequenceList(hm);
            }

            list = this.jdbcTemplate.queryForList(sql.toString(), new String[]{qcid});

        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: checkSequenceList()");
            throw new GeneralException("db_query_time_out");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: checkSequenceList()");
            throw new GeneralException("cannot_get_connection");
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }

        //check if the list is null
        if (list == null || list.size() == 0) {
            throw new GeneralException("error_no_qc_working");
        }

        //check if the container more than 2 bays
        if (list.size() > 2) {
            throw new GeneralException("error_more_than_3bay");
        }

        int i = 1;
        //for it need to show 33/35 in twin and quad, but 34 in tandeam
        int minBay = 0;
        int maxBay = 0;

        if (list != null && list.size() > 0) {
            for (Map<String, Object> rs : list) {
                try {
                    //check the bay number is integer
                    int bayNumber = Integer.parseInt(rs.get("last_pos_slot").toString());
                    if (i == 1) {
                        minBay = bayNumber;
                        maxBay = bayNumber;
                    }
                    if (i == 2) {
                        maxBay = bayNumber;
                    }
                } catch (Exception e) {
                    LOG.debug(e.toString());
                    throw new GeneralException("error_bay_number_integer");
                }
                i++;
            }
        }

        String bay = "";
        String acrossBay = "";
        //multi lift (twin,tandem,quad)
        if (minBay != maxBay) {
            if (Math.abs(maxBay - minBay) > 2) {
                throw new GeneralException("error_more_than_3bay");
            }
            bay = minBay + "/" + maxBay;
            acrossBay = "1";
        } else {
            bay = "" + minBay;
        }

        hm.put("MinBay", "" + minBay);
        hm.put("MaxBay", "" + maxBay);
        hm.put("AcrossBay", acrossBay);
        hm.put("Bay", bay);
        return hm;

    }

    /**
     * check if the sequence include more than 2 bays.
     *
     * @param hm
     * @return
     * @throws GeneralException
     */
    public StringBuffer checkLoadSequenceList(HashMap hm) throws GeneralException {

        String qcid = (String) hm.get("QCid");
        String qorder = (String) hm.get("QOrder");

        StringBuffer sql = new StringBuffer();
        sql.append(" select  last_pos_slot from ( ");
        sql.append(" select substr(last_pos_slot,1,2) as last_pos_slot ");
        sql.append(" from MN4O_QC_inv_wq iq, MN4O_QC_inv_wi iw,MN4O_QC_inv_unit_yrd_visit iuyv, MN4O_QC_inv_unit_fcy_visit iufv, MN4O_QC_inv_unit iu, ");
        sql.append(" MN4O_QC_xps_craneshift xcs,MN4O_QC_xps_pointofwork xpow,MN4O_QC_ref_equipment re,MN4O_QC_inv_goods ig, MN4O_QC_argo_carrier_visit acv ");
        sql.append(" where ");
        sql.append(" iw.work_queue_gkey = iq.gkey and iw.uyv_gkey = iuyv.gkey and iuyv.ufv_gkey = iufv.gkey and iq.pos_locid = acv.id ");
        sql.append(" and iufv.unit_gkey = iu.gkey and iq.first_shift_pkey=xcs.pkey and xcs.owner_pow=xpow.pkey");
        sql.append(" and re.gkey  = iu.eq_gkey and ig.gkey=iu.goods");
        sql.append(" and acv.phase not in ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED') ");
        sql.append(" and iq.qtype in ('LOAD') ");
        sql.append(" and iq.qdeck in ('A', 'B') ");
        sql.append(" and iq.pos_loctype = 'VESSEL' ");
        sql.append(" and iq.is_blue = '1' ");
        sql.append(" and iw.move_kind != 'YARD' ");
        sql.append(" and iw.move_kind != 'SHFT' ");
        sql.append(" and iw.move_stage = 'COMPLETE' ");
        sql.append(" and xpow.name= ? ");
        sql.append(" and iq.qorder='" + qorder + "' ");
        sql.append(" group by substr(last_pos_slot,1,2) ");
        sql.append(" ) order by last_pos_slot ");

        return sql;
    }

    /**
     * check if the sequence include more than 2 bays.
     *
     * @param hm
     * @return
     * @throws GeneralException
     */
    public StringBuffer checkDischargeSequenceList(HashMap hm) throws GeneralException {

        String qcid = (String) hm.get("QCid");
        String qorder = (String) hm.get("QOrder");

        StringBuffer sql = new StringBuffer();
        sql.append(" select  last_pos_slot from ( ");
        sql.append(" select substr(last_pos_slot,1,2) as last_pos_slot ");
        sql.append(" from MN4O_QC_inv_wq iq, MN4O_QC_inv_wi iw,MN4O_QC_inv_unit_yrd_visit iuyv, MN4O_QC_inv_unit_fcy_visit iufv, MN4O_QC_inv_unit iu, ");
        sql.append(" MN4O_QC_xps_craneshift xcs,MN4O_QC_xps_pointofwork xpow,MN4O_QC_ref_equipment re,MN4O_QC_inv_goods ig, MN4O_QC_argo_carrier_visit acv ");
        sql.append(" where ");
        sql.append(" iw.work_queue_gkey = iq.gkey and iw.uyv_gkey = iuyv.gkey and iuyv.ufv_gkey = iufv.gkey and iq.pos_locid = acv.id ");
        sql.append(" and iufv.unit_gkey = iu.gkey and iq.first_shift_pkey=xcs.pkey and xcs.owner_pow=xpow.pkey");
        sql.append(" and re.gkey =  iu.eq_gkey and ig.gkey=iu.goods");
        sql.append(" and acv.phase not in ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED') ");
        sql.append(" and iq.qtype in ('DISCH') ");
        sql.append(" and iq.qdeck in ('A', 'B') ");//restrict the scope,except G
        sql.append(" and iq.pos_loctype = 'VESSEL' ");
        sql.append(" and iq.is_blue = '1' ");
        sql.append(" and iw.move_kind != 'YARD' ");
        sql.append(" and iw.move_kind != 'SHFT' ");
        sql.append(" and iw.move_stage in ('PLANNED', 'NONE')  ");
        sql.append(" and xpow.name= ? ");
        sql.append(" and iq.qorder='" + qorder + "' ");
        sql.append(" group by substr(last_pos_slot,1,2) ");
        sql.append(" ) order by last_pos_slot ");

        return sql;

    }

    /**
     * get cell by condition
     *
     * @param hm
     * @return
     * @throws GeneralException
     */
    public HashMap getSequenceList(HashMap hm) throws GeneralException {
        List<Map<String, Object>> list = null;
        HashMap tierHM = new HashMap();
        String qcid = (String) hm.get("QCid");
        String qorder = (String) hm.get("QOrder");
        String qtype = (String) hm.get("QType");
        String acrossBay = (String) hm.get("AcrossBay");

        try {
            StringBuffer sql = new StringBuffer();

            sql.append(" select iufv.last_pos_slot as current_pos_slot, ");
            sql.append(" iu.id as unit_number, ");//CGM170276 HE FENG ADD
            sql.append(" to_char(iufv.time_move,'yyyy-mm-dd hh24:mi:ss') as time_move, ");
            sql.append(" iq.qtype, iw.pos_slot as planned_pos_slot,iw.move_stage, ");
            sql.append(" iq.pos_locid, xpow.name as qc_id, iq.qdeck, iq.qrow,  ");
            sql.append(" iq.qorder, iw.sequence,iu.is_oog, ");
            sql.append(" case when (twin_with ='PREV' or twin_with ='NEXT') and twin_int_fetch=1 and (is_tandem_with_next=1 or is_tandem_with_previous=1 ) then '1' else '0' end as isquad,  ");
            sql.append(" case when twin_with ='NONE' and twin_int_fetch=0 and (is_tandem_with_next=1 or is_tandem_with_previous=1 ) then '1' else '0' end as istandem,  ");
            sql.append(" case when (twin_with ='PREV' or twin_with ='NEXT') and twin_int_fetch=1 and (is_tandem_with_next=0 and is_tandem_with_previous=0 ) then '1' else '0' end as istwin,  ");
            sql.append(" case when twin_with ='NONE' and twin_int_fetch=0 and (is_tandem_with_next=0 and is_tandem_with_previous=0 ) or is_tandem_with_next is null or is_tandem_with_previous is null then '1' else '0' end as issingle,  ");
            sql.append(" case when ig.temp_reqd_c is null then '0' else '1' end as is_powered,");
            sql.append(" case re.iso_group when 'TN' then '1' when 'TD' then '1' when 'TG' then '1' else '0' end as istank ");
            /*Start PCR-DG*/
            sql.append(", iu.gkey as unit_fcy_gkey ");
            /*End PCR-DG*/
            sql.append(" from MN4O_QC_inv_wq iq, MN4O_QC_inv_wi iw,MN4O_QC_inv_unit_yrd_visit iuyv, MN4O_QC_inv_unit_fcy_visit iufv, MN4O_QC_inv_unit iu, ");
            sql.append(" MN4O_QC_xps_craneshift xcs,MN4O_QC_xps_pointofwork xpow,MN4O_QC_ref_equipment re,MN4O_QC_inv_goods ig, MN4O_QC_argo_carrier_visit acv ");
            sql.append(" where ");
            sql.append(" iw.work_queue_gkey = iq.gkey and iw.uyv_gkey = iuyv.gkey and iuyv.ufv_gkey = iufv.gkey and iq.pos_locid = acv.id ");
            sql.append(" and iufv.unit_gkey = iu.gkey and iq.first_shift_pkey=xcs.pkey and xcs.owner_pow=xpow.pkey");
            sql.append(" and re.gkey =  iu.eq_gkey and ig.gkey=iu.goods");
            sql.append(" and acv.phase not in ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED') ");
            sql.append(" and iq.qtype in ('LOAD', 'DISCH') ");
            sql.append(" and iq.qdeck in ('A', 'B') ");//restrict the scope,except G
            sql.append(" and iq.pos_loctype = 'VESSEL' ");
            sql.append(" and iq.is_blue = '1' ");
            sql.append(" and iw.move_kind != 'YARD' ");
            sql.append(" and iw.move_kind != 'SHFT' ");
            sql.append(" and xpow.name= ? ");
            sql.append(" and iq.qorder='" + qorder + "' ");
            // need restrict
            if (qtype.equals("DISCH")) {
                sql.append(" and iw.move_stage in ('PLANNED', 'NONE')  ");
                //for deal with the cell by tier and row, show the 35first, 33 later , and we can see the info in front ,so should use desc
                sql.append(" order by  iufv.last_pos_slot desc  ");
            } else if (qtype.equals("LOAD")) {
                sql.append(" and iw.move_stage = 'COMPLETE' ");
                //for deal with the cell by time , so should use desc
                sql.append(" order by iufv.time_move desc ,iufv.last_pos_slot desc ");
            }
            list = this.jdbcTemplate.queryForList(sql.toString(), new Object[]{qcid});

        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getSequenceList()");
            throw new GeneralException("db_query_time_out");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getSequenceList()");
            throw new GeneralException("cannot_get_connection");
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }

        //check if the list is null
        if (list == null || list.size() == 0) {
            throw new GeneralException("error_no_qc_working");
        }
        /*START CGM170276 HE FENG ADD*/
        Map<String, Object> firstList = null;
        if (qtype.equals("DISCH")) {
            List<Map<String, Object>> finishedList = getFinishedDischList(qorder, qcid);
            if (finishedList != null && finishedList.size() > 0) {
                firstList = finishedList.get(0);
            }
        } else if (qtype.equals("LOAD") && list != null && list.size() > 0) {
            firstList = list.get(0);
        }
        if (firstList != null) {
            String lodingTime = firstList.get("time_move").toString();
            String current_pos_slot = firstList.get("current_pos_slot") == null ? "" : firstList.get("current_pos_slot").toString();
            String planned_pos_slot = firstList.get("planned_pos_slot") == null ? "" : firstList.get("planned_pos_slot").toString();
            String unitFcyGkey = firstList.get("unit_fcy_gkey").toString();
            String unitNumber = firstList.get("unit_number").toString();
            String observedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String log = "found unit: " + unitNumber + " (facility gkey: " + unitFcyGkey
                    + ") is finished moving from " + planned_pos_slot + " to " +
                    current_pos_slot + ". Move time is " + lodingTime + ". Move type is " + qtype + ".";
            hm.put("log", log);
            hm.put("observedTime", observedTime);
        }
        /*END CGM170276 HE FENG ADD*/
        String qdeck = "";
        String vessl = "";
        String firstLoadCellTime = "";
        int remainCotainers = 0;
        int i = 1;
        /*PCR-DG*/
        List<String> hazardList = new ArrayList<>();
        List<String> unitList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (Map<String, Object> rs : list) {
                String unitKey = rs.get("unit_fcy_gkey").toString();
                unitList.add(unitKey);
            }
            hazardList = getHazardList(unitList);
        }
        /*PCR-DG*/
        try {
            if (list != null && list.size() > 0) {
                remainCotainers = list.size();
                for (Map<String, Object> rs : list) {
                    SequenceVO sequenceVO = new SequenceVO();
                    String current_pos_slot = rs.get("current_pos_slot") == null ? "" : rs.get("current_pos_slot").toString();
                    String planned_pos_slot = rs.get("planned_pos_slot") == null ? "" : rs.get("planned_pos_slot").toString();
                    qtype = rs.get("qtype").toString();
                    qdeck = rs.get("qdeck").toString();
                    vessl = rs.get("pos_locid").toString();
                    String isquad = rs.get("isquad").toString();
                    String istandem = rs.get("istandem").toString();
                    String istwin = rs.get("istwin").toString();
                    String issingle = rs.get("issingle").toString();
                    String moveStage = rs.get("move_stage").toString();
                    String qrow = rs.get("qrow").toString();
                    String cellbay = current_pos_slot.substring(0, 2);
                    /*Start PCR-DG*/
                    String unitFcyGkey = rs.get("unit_fcy_gkey").toString();
                    String is_dg = isHazard(unitFcyGkey, hazardList);
                    sequenceVO.setIs_dg(is_dg);
                    /*End PCR-DG*/
                    sequenceVO.setCurrent_pos_slot(current_pos_slot);
                    sequenceVO.setQtype(qtype);
                    sequenceVO.setPlanned_pos_slot(planned_pos_slot);
                    sequenceVO.setPos_locid(vessl);
                    sequenceVO.setQrow(qrow);
                    sequenceVO.setIs_oog(rs.get("is_oog").toString());
                    sequenceVO.setIs_powered(rs.get("is_powered").toString());
                    sequenceVO.setIstank(rs.get("istank").toString());
                    sequenceVO.setPos_locid(vessl);
                    sequenceVO.setQdeck(qdeck);
                    sequenceVO.setIsQuad(isquad);
                    sequenceVO.setIsTandem(istandem);
                    sequenceVO.setIsTwin(istwin);
                    sequenceVO.setIsSingle(issingle);
                    sequenceVO.setBay(cellbay);
                    String tier = current_pos_slot.substring(4, 6);
                    String row = current_pos_slot.substring(2, 4);
                    if (qtype.equals("LOAD")) {
                        //when load case, only the first one cell will be display
                        if (i == 1) {
                            sequenceVO.setStatus("load");
                            firstLoadCellTime = rs.get("time_move").toString();
                            //
                            tierHM.put(row, row);
                            //because tandem will deal with two units together, so it should show two load cell in screen.
                            //compare first unit with time, for two units deal with together , so the time is the same.
                        } else if (firstLoadCellTime.equals(rs.get("time_move").toString())) {
                            sequenceVO.setStatus("load");
                            tierHM.put(row, row);
                        } else {
                            sequenceVO.setStatus("inactive");
                        }
                        //when load case , not need to deal with the front,rear unit.
                        if (hm.containsKey(tier + row)) {
                            /*Begin PCR-VMT-DG Tony Add*/
                            SequenceVO oldSeqVO = (SequenceVO) hm.get(tier + row);
                            if (!sequenceVO.getIs_dg().equals(oldSeqVO.getIs_dg())) {
                                if (sequenceVO.getIs_dg().equals("1")) {
                                    oldSeqVO.setIs_dg("1");
                                    hm.put(tier + row, oldSeqVO);
                                }
                            }
                            /*End PCR-VMT-DG Tony Add*/
                            continue;
                        }
                        hm.put(tier + row, sequenceVO);

                    } else {
                        sequenceVO.setStatus("discharge");
                        //only deal with 20' unit and accrossbay
                        if (acrossBay.equals("1") && hm.containsKey(tier + row)) {
                            SequenceVO oldSequenceVO = (SequenceVO) hm.get(tier + row);
                            if (oldSequenceVO.getStatus().equals(sequenceVO.getStatus())) {
                                copySequenceVO(oldSequenceVO, sequenceVO);
                            }
                        }
                        hm.put(tier + row, sequenceVO);

                    }

                    i++;
                }
            }

            //count container in load case
            if (qtype.equals("LOAD")) {
                int loadCount = getLoadCount(qcid, qorder);
                remainCotainers = loadCount - remainCotainers;
            }

            hm.put("DeckHold", qdeck);
            hm.put("QType", qtype);
            hm.put("RemainCotainers", "" + remainCotainers);
            hm.put("Vessl", vessl);
            hm.put("TierHM", tierHM);
        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getSequenceList()");
            throw new GeneralException("db_query_time_out");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getSequenceList()");
            throw new GeneralException("cannot_get_connection");
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }

        return hm;
    }

    /**
     * when two bay has unit, if reefer or OOG or tank  is not the same, copy status from the oldSequenceVO to newSequenceVO
     *
     * @param oldSequenceVO
     * @param newSequenceVO
     * @return
     * @throws GeneralException
     */
    public SequenceVO copySequenceVO(SequenceVO oldSequenceVO, SequenceVO newSequenceVO) throws GeneralException {
        String complexunit = "0";
        if (!oldSequenceVO.getIs_oog().equals(newSequenceVO.getIs_oog())) {
            complexunit = "1";
            if (oldSequenceVO.getIs_oog().equals("1")) {
                newSequenceVO.setIs_oog("1");
            }
        }
        if (!oldSequenceVO.getIs_powered().equals(newSequenceVO.getIs_powered())) {
            complexunit = "1";
            if (oldSequenceVO.getIs_powered().equals("1")) {
                newSequenceVO.setIs_powered("1");
            }
        }
        if (!oldSequenceVO.getIstank().equals(newSequenceVO.getIstank())) {
            complexunit = "1";
            if (oldSequenceVO.getIstank().equals("1")) {
                newSequenceVO.setIstank("1");
            }
        }
        /*Begin PCR-VMT-DG Tony Add*/
        if (!oldSequenceVO.getIs_dg().equals(newSequenceVO.getIs_dg())) {
            if (oldSequenceVO.getIs_dg().equals("1")) {
                newSequenceVO.setIs_dg("1");
            }
        }
        /*End PCR-VMT-DG Tony Add*/
        newSequenceVO.setComplexunit(complexunit);
        return newSequenceVO;
    }

    /**
     * count container in load case
     *
     * @param qcid
     * @param qorder
     * @return
     * @throws GeneralException
     */
    public int getLoadCount(String qcid, String qorder) throws GeneralException {

        StringBuffer sql = new StringBuffer();
        int loadCount = 0;
        try {
            sql.append(" select count(1) as loadCount ");
            sql.append(" from MN4O_QC_inv_wq iq, MN4O_QC_inv_wi iw,MN4O_QC_inv_unit_yrd_visit iuyv, MN4O_QC_inv_unit_fcy_visit iufv, MN4O_QC_inv_unit iu, ");
            sql.append(" MN4O_QC_xps_craneshift xcs,MN4O_QC_xps_pointofwork xpow,MN4O_QC_ref_equipment re,MN4O_QC_inv_goods ig, MN4O_QC_argo_carrier_visit acv ");
            sql.append(" where ");
            sql.append(" iw.work_queue_gkey = iq.gkey and iw.uyv_gkey = iuyv.gkey and iuyv.ufv_gkey = iufv.gkey and iq.pos_locid = acv.id ");
            sql.append(" and iufv.unit_gkey = iu.gkey and iq.first_shift_pkey=xcs.pkey and xcs.owner_pow=xpow.pkey");
            sql.append(" and re.gkey  = iu.eq_gkey and ig.gkey=iu.goods");
            sql.append(" and acv.phase not in ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED') ");
            sql.append(" and iq.qtype in ('LOAD') ");
            sql.append(" and iq.qdeck in ('A', 'B') ");//restrict the scope,except G
            sql.append(" and iq.pos_loctype = 'VESSEL' ");
            sql.append(" and iq.is_blue = '1' ");
            sql.append(" and iw.move_kind != 'YARD' ");
            sql.append(" and iw.move_kind != 'SHFT' ");
            sql.append(" and xpow.name= ? ");
            sql.append(" and iq.qorder='" + qorder + "' ");

            loadCount = this.jdbcTemplate.queryForObject(sql.toString(), new Object[]{qcid}, Integer.class);
        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getLoadCount()");
            throw new GeneralException("db_query_time_out");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getLoadCount()");
            throw new GeneralException("cannot_get_connection");
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }

        return loadCount;
    }

    /**
     * get roblist by vessel and row
     *
     * @param hm
     * @return
     * @throws GeneralException
     */
    public HashMap getROBList(HashMap hm) throws GeneralException {
        String minBay = (String) hm.get("MinBay");
        String maxBay = (String) hm.get("MaxBay");
        String qType = (String) hm.get("QType");
        if (qType != null && qType.equalsIgnoreCase("LOAD")) {
            if (!minBay.equals(maxBay)) {
                getROBListByBayNew(hm, minBay);
                getROBListByBayNew(hm, maxBay);
            } else {
                getROBListByBayNew(hm, minBay);
            }
            if (minBay != null) {
                try {
                    int tempInt = Integer.parseInt(minBay);
                    if (tempInt % 2 == 0) {
                        getROBListByBayNew(hm, StringUtils.leftPad(String.valueOf(tempInt - 1), 2, "0"), StringUtils.leftPad(String.valueOf(tempInt + 1), 2, "0"));

                    } else {
                        //getROBListByBayNew(hm,getEvenBay(minBay));
                        getROBListByBayNew(hm, StringUtils.leftPad(String.valueOf(tempInt - 1), 2, "0"));
                        getROBListByBayNew(hm, StringUtils.leftPad(String.valueOf(tempInt + 1), 2, "0"));
                    }
                } catch (Exception ex) {

                }
            }
        } else {
            if (!minBay.equals(maxBay)) {
                getROBListByBay(hm, minBay);
                getROBListByBay(hm, maxBay);
            } else {
                getROBListByBay(hm, minBay);
            }
        }


        return hm;
    }


    private String getEvenBay(String minBay) {
        String evenBay = "";
        int tempInt = Integer.parseInt(minBay);
        int tempEvenBay = tempInt - 1;
        if (tempEvenBay == 2 || (tempEvenBay - 2) % 4 == 0) {
            evenBay = StringUtils.leftPad(String.valueOf(tempEvenBay), 2, "0");
        } else {
            tempEvenBay = tempInt + 1;
            evenBay = StringUtils.leftPad(String.valueOf(tempEvenBay), 2, "0");
        }
        return evenBay;
    }

    /**
     * get roblist by vessel and row
     */
    public HashMap getROBListByBay(HashMap hm, String bay) throws GeneralException {
        List<Map<String, Object>> list = null;
        String vessel = (String) hm.get("Vessl");
        try {
            StringBuffer sql = new StringBuffer();

            sql.append(" select iu.id, iu.category, iufv.restow_typ, iufv.last_pos_slot ");
            /*Start PCR-DG*/
            sql.append(", iu.gkey as unit_fcy_gkey ");
            /*End PCR-DG*/
            sql.append(" from MN4O_QC_inv_unit_fcy_visit iufv, MN4O_QC_inv_unit iu, MN4O_QC_argo_carrier_visit acv ");
            sql.append(" where");
            sql.append(" iu.gkey = iufv.unit_gkey ");
            sql.append(" and iufv.actual_ib_cv = acv.gkey");
            sql.append(" and iufv.transit_state= 'S20_INBOUND' ");
            sql.append(" and iu.category = 'THRGH' ");
            sql.append(" and iufv.restow_typ = 'NONE' ");
            sql.append(" and acv.phase not in ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED') ");
            sql.append(" and acv.id= ? ");
            sql.append(" and substr(iufv.last_pos_slot,1,2) = ? ");
            sql.append(" order by iufv.last_pos_slot ");

            bay = StringUtils.leftPad(bay, 2, "0");

            list = this.jdbcTemplate.queryForList(sql.toString(), new Object[]{vessel, bay});

        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getROBListByBay()");
            throw new GeneralException("db_query_time_out");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getROBListByBay()");
            throw new GeneralException("cannot_get_connection");
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }
        /*PCR-DG*/
        List<String> hazardList = new ArrayList();
        List<String> unitList = new ArrayList();
        if (list != null && list.size() > 0) {
            for (Map<String, Object> rs : list) {
                String unitKey = rs.get("unit_fcy_gkey").toString();
                unitList.add(unitKey);
            }
            hazardList = getHazardList(unitList);
        }
        /*PCR-DG*/
        try {
            if (list != null && list.size() > 0) {
                for (Map<String, Object> rs : list) {
                    String last_pos_slot = rs.get("last_pos_slot").toString();
                    String tierrow = last_pos_slot.substring(4, 6) + last_pos_slot.substring(2, 4);

                    if (!hm.containsKey(tierrow)) {
                        SequenceVO sequenceVO = new SequenceVO();
                        sequenceVO.setStatus("inactive");
                        /*Start PCR-DG*/
                        String unitFcyGkey = rs.get("unit_fcy_gkey").toString();
                        String is_dg = isHazard(unitFcyGkey, hazardList);
                        sequenceVO.setIs_dg(is_dg);
                        /*End PCR-DG*/
                        hm.put(tierrow, sequenceVO);
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }

        return hm;
    }


    public HashMap getROBListByBayNew(HashMap hm, String bay) throws GeneralException {
        List<Map<String, Object>> list = null;
        String vessel = (String) hm.get("Vessl");
        try {
            StringBuffer sql = new StringBuffer();

            sql.append(" select iu.id, iu.category, iufv.restow_typ, iufv.last_pos_slot ");
            sql.append(", iu.gkey as unit_fcy_gkey ");
            sql.append(" from MN4O_QC_inv_unit_fcy_visit iufv, MN4O_QC_inv_unit iu, MN4O_QC_argo_carrier_visit acv ");
            sql.append(" where");
            sql.append(" iu.gkey = iufv.unit_gkey ");
            sql.append(" and ((iufv.actual_ib_cv = acv.gkey and iufv.transit_state= 'S20_INBOUND' ) or (iufv.actual_ob_cv = acv.gkey and iufv.transit_state= 'S60_LOADED'))");
            sql.append(" and acv.phase not in ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED') ");
            sql.append(" and acv.id= ? ");
            sql.append(" and substr(iufv.last_pos_slot,1,2) = ? ");
            sql.append(" order by iufv.last_pos_slot ");

            bay = StringUtils.leftPad(bay, 2, "0");

            list = this.jdbcTemplate.queryForList(sql.toString(), new Object[]{vessel, bay});

        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getROBListByBay()");
            throw new GeneralException("db_query_time_out");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getROBListByBay()");
            throw new GeneralException("cannot_get_connection");
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }
        /*PCR-DG*/
        List<String> hazardList = new ArrayList();
        List<String> unitList = new ArrayList();
        if (list != null && list.size() > 0) {
            for (Map<String, Object> rs : list) {
                String unitKey = rs.get("unit_fcy_gkey").toString();
                unitList.add(unitKey);
            }
            hazardList = getHazardList(unitList);
        }
        /*PCR-DG*/
        try {
            if (list != null && list.size() > 0) {
                for (Map<String, Object> rs : list) {
                    String last_pos_slot = rs.get("last_pos_slot").toString();
                    String tierrow = last_pos_slot.substring(4, 6) + last_pos_slot.substring(2, 4);

                    if (!hm.containsKey(tierrow)) {
                        SequenceVO sequenceVO = new SequenceVO();
                        sequenceVO.setStatus("inactive");
                        /*Start PCR-DG*/
                        String unitFcyGkey = rs.get("unit_fcy_gkey").toString();
                        String is_dg = isHazard(unitFcyGkey, hazardList);
                        sequenceVO.setIs_dg(is_dg);
                        /*End PCR-DG*/
                        hm.put(tierrow, sequenceVO);
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }

        return hm;
    }

    public HashMap getROBListByBayNew(HashMap hm, String minbay, String maxBay) throws GeneralException {
        List<Map<String, Object>> list = null;
        List<Map<String, Object>> maxBayList = null;
        String vessel = (String) hm.get("Vessl");
        try {
            StringBuffer sql = new StringBuffer();

            sql.append(" select iu.id, iu.category, iufv.restow_typ, iufv.last_pos_slot ");
            sql.append(", iu.gkey as unit_fcy_gkey ");
            sql.append(" from MN4O_QC_inv_unit_fcy_visit iufv, MN4O_QC_inv_unit iu, MN4O_QC_argo_carrier_visit acv ");
            sql.append(" where");
            sql.append(" iu.gkey = iufv.unit_gkey ");
            sql.append(" and ((iufv.actual_ib_cv = acv.gkey and iufv.transit_state= 'S20_INBOUND' ) or (iufv.actual_ob_cv = acv.gkey and iufv.transit_state= 'S60_LOADED'))");
            sql.append(" and acv.phase not in ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED') ");
            sql.append(" and acv.id= ? ");
            sql.append(" and substr(iufv.last_pos_slot,1,2) = ? ");
            sql.append(" order by iufv.last_pos_slot ");

            minbay = StringUtils.leftPad(minbay, 2, "0");

            maxBay = StringUtils.leftPad(maxBay, 2, "0");

            list = this.jdbcTemplate.queryForList(sql.toString(), new Object[]{vessel, minbay});
            maxBayList = this.jdbcTemplate.queryForList(sql.toString(), new Object[]{vessel, maxBay});

        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getROBListByBay()");
            throw new GeneralException("db_query_time_out");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getROBListByBay()");
            throw new GeneralException("cannot_get_connection");
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }
        /*PCR-DG*/
        List<String> hazardList = new ArrayList();
        List<String> unitList = new ArrayList();
        if (list != null && list.size() > 0) {
            for (Map<String, Object> rs : list) {
                String unitKey = rs.get("unit_fcy_gkey").toString();
                unitList.add(unitKey);
            }
            hazardList = getHazardList(unitList);
        }
        /*PCR-DG*/
        try {
            if (list != null && list.size() > 0) {
                for (Map<String, Object> rs : list) {
                    boolean existAnotherBay = false;
                    String last_pos_slot = rs.get("last_pos_slot").toString();
                    String tierrow = last_pos_slot.substring(4, 6) + last_pos_slot.substring(2, 4);
                    if (maxBayList != null && maxBayList.size() > 0) {
                        for (Map<String, Object> tempRs : maxBayList) {
                            String tempLastPos = tempRs.get("last_pos_slot").toString();
                            String tempTierRow = tempLastPos.substring(4, 6) + tempLastPos.substring(2, 4);
                            if (tierrow.equals(tempTierRow)) {
                                existAnotherBay = true;
                                break;
                            }
                        }
                    }

                    if (!existAnotherBay) continue;
                    if (!hm.containsKey(tierrow)) {
                        SequenceVO sequenceVO = new SequenceVO();
                        sequenceVO.setStatus("inactive");
                        /*Start PCR-DG*/
                        String unitFcyGkey = rs.get("unit_fcy_gkey").toString();
                        String is_dg = isHazard(unitFcyGkey, hazardList);
                        sequenceVO.setIs_dg(is_dg);
                        /*End PCR-DG*/
                        hm.put(tierrow, sequenceVO);
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }

        return hm;
    }

    /**
     * get first line of the cell matrix
     *
     * @param
     */


    public String getFirstLine(List cellMatrixistList, HashMap tierHM) throws GeneralException {
        try {
            StringBuffer firstline = new StringBuffer();
            int size = cellMatrixistList.size();
            int i, j;
            firstline.append(" <tr> \n");
            firstline.append(" <td id=\"tier\" class=\"tierNum\">Tier</td> \n");
            for (i = 0; i < size; i++) {
                CellMatrix cell = (CellMatrix) cellMatrixistList.get(i);
                String row = cell.getRow();
                if (tierHM.containsKey(row)) {
                    firstline.append(" <td id=\"R" + row + "\" class=\"load\">" + row + "</td> \n");
                } else {
                    firstline.append(" <td id=\"R" + row + "\" class=\"tierNum\">" + row + "</td> \n");
                }

            }
            firstline.append(" </tr> \n");

            return firstline.toString();
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }
    }

    /**
     * build cell matrix
     *
     * @param
     */
    public String buildBay(List cellMatrixistList, HashMap hm) throws GeneralException {
        try {
            StringBuffer getcelltable = new StringBuffer();
            int size = cellMatrixistList.size();
            //get some info from the first bayList
            CellMatrix firstcellmatrix = (CellMatrix) cellMatrixistList.get(0);
            int tierStart = Integer.parseInt(firstcellmatrix.getTierStart());
            int tierEnd = Integer.parseInt(firstcellmatrix.getTierEnd());
            String deckHold = (String) hm.get("DeckHold");
            String qtype = getQType((String) hm.get("QType"));
            String bay = getQType((String) hm.get("Bay"));
            String acrossBay = (String) hm.get("AcrossBay");
            String vesselid = (String) hm.get("Vessl");

            //get the tier mumber
            int tierNum = Integer.parseInt(firstcellmatrix.getTier());
            int i, j = 0, s = 0;
            if (tierEnd > 0) {
                j = tierEnd;
                s = tierStart;
            } else {
                j = tierNum;
                s = 0;
            }
            for (j = j; j >= s; j--) {
                //the Hold(B) has not 00 tier;
                if (j == 0 && deckHold.equals("B")) break;

                if (j % 2 != 0) {    // skip generating the row if the bay number is odd
                    continue;
                }

                getcelltable.append(" <tr> \n");
                for (i = 0; i < size; i++) {
                    CellMatrix cell = (CellMatrix) cellMatrixistList.get(i);
                    String row = cell.getRow();
                    String tier = "";
                    if (tierEnd > 0) {
                        if (j % 2 != 0) {
                            continue;
                        }
                        if (j >= 10) {
                            tier = "" + j;
                        } else {
                            tier = "0" + j;
                        }
                    } else {
                        tier = getTier(deckHold, j);
                    }
                    //get first grid
                    if (i == 0) {
                        getcelltable.append(" <td id=\"T" + tier + "\" class=\"tierNum\">" + tier + "</td> \n");
                    }
                    List<String> matchRefuelRange = getRefuelRangeListByVesselId(vesselid, deckHold, (String) hm.get("Bay"));
                    boolean isRefuel = getVesselVisitRefuelStatus(vesselid);
                    if (isRefuel) {
                        hm.put("isRefuel", "Yes");
                    }
                    if (matchRefuelRange.contains(tier + row) || matchRefuelRange.contains("blank" + row)) {
                        getcelltable.append(" <td id=\"T" + tier + "R" + row + "\" class=\"refuel\">&nbsp;</td> \n");
                    } else {
                        if (hm.containsKey(tier + row)) {
                            SequenceVO sequenceVO = (SequenceVO) hm.get(tier + row);
                            String status = sequenceVO.getStatus();
                            String complexunit = sequenceVO.getComplexunit();
                            String cellInfo = getCellInfo(sequenceVO, acrossBay);

                            if ("".equals(cellInfo)) {
                                cellInfo = "&nbsp;";
                            }
                            /*Start PCR-DG*/
                            String is_dg = sequenceVO.getIs_dg();
                            if (is_dg != null && is_dg == "1") {    // To-Do: Check if is dangerous container
                                if (cellInfo.equals("&nbsp;")) {
                                    cellInfo += "<span class=\"dgind\">*</span>";
                                } else {
                                    cellInfo += "<span class=\"infodgind\">*</span>";
                                }

                            }
                            /*End PCR-DG*/
                            if (complexunit.equals("1")) {
                                getcelltable.append(" <td id=\"T" + tier + "R" + row + "\" class=\"complexunit\">" + cellInfo + "</td> \n");
                            } else {
                                /*Begin PCR-VMT-000004 Tony Add*/
                                String info = getCellInfo(sequenceVO, acrossBay);
                                if (info.equals("20")) {
                                    getcelltable.append(" <td id=\"T" + tier + "R" + row + "\" class=\"twenty " + status + "\">" + cellInfo + "</td> \n");
                                } else {
                                    getcelltable.append(" <td id=\"T" + tier + "R" + row + "\" class=\"" + status + "\">" + cellInfo + "</td> \n");
                                }
                                /*End PCR-VMT-000004 Tony Add*/
                            }
                        } else {
                            getcelltable.append(" <td id=\"T" + tier + "R" + row + "\" class=\"empty\">&nbsp;</td> \n");
                        }
                    }
                }

                getcelltable.append(" </tr> \n");
            }

            return getcelltable.toString();

        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }
    }

    public boolean getVesselVisitRefuelStatus(String vesselId) {
        String countHql = "select count(*) from VesselRefuel where vesselid = :vesselid and is_refuel = 'Yes' ";
        int countTotal = DataAccessUtils.intResult(this.hibernateTemplate.findByNamedParam(countHql, new String[]{"vesselid"}, new Object[]{vesselId}));
        return countTotal > 0;
    }

    public List<String> getRefuelRangeListByVesselId(String vesselId, String deckHold, String bay) throws GeneralException {
        List<String> result = new ArrayList<>();
        List<Map<String, Object>> list = null;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append(" select vc.rowstart, vc.rowend, vc.tierstart, vc.tierend ");
            sql.append(" from t_vesselrefuel vr, t_vesselcol vc ");
            sql.append(" where vr.is_refuel = 'Yes' ");
            sql.append(" and vr.vesselid = vc.vesselid ");
            sql.append(" and vc.vesselid = ? ");
            sql.append(" and vc.deck_hold = ? ");
            //Check access bay or not
            if (StringUtils.contains(bay, "/")) {
                String[] bays = bay.split("/");
                sql.append(" and vc.bay in (?,?) ");
                list = jdbcTemplate.queryForList(sql.toString(), new Object[]{vesselId, deckHold, bays[0], bays[1]});
            } else {
                sql.append(" and vc.bay = ? ");
                list = jdbcTemplate.queryForList(sql.toString(), new Object[]{vesselId, deckHold, bay});
            }
        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getRefuelRowListByVesselId()");
            throw new GeneralException("db_query_time_out");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getRefuelRowListByVesselId()");
            throw new GeneralException("cannot_get_connection");
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                int startRow = Integer.parseInt((String) map.get("rowstart"));
                int endRow = Integer.parseInt((String) map.get("rowend"));
                String startTierStr = (String) map.get("tierstart");
                String endTierStr = (String) map.get("tierend");
                if (StringUtils.isNotBlank(startTierStr) && StringUtils.isNotBlank(endTierStr)) {
                    int startTier = Integer.parseInt(startTierStr);
                    int endTier = Integer.parseInt(endTierStr);
                    for (int i = startRow; i <= endRow; i = i + 2) {
                        for (int j = startTier; j <= endTier; j = j + 2) {
                            String tempRow = i < 10 ? "0" + i : String.valueOf(i);
                            String tempTier = j < 10 ? "0" + j : String.valueOf(j);
                            result.add(tempTier + tempRow);
                        }
                    }
                } else {
                    for (int i = startRow; i <= endRow; i = i + 2) {
                        String tempRow = i < 10 ? "0" + i : String.valueOf(i);
                        result.add("blank" + tempRow);
                    }
                }
            }
        }
        return result;
    }

    /**
     * String qtype
     *
     * @param qtype
     * @return
     */
    public String getQType(String qtype) {
        if (qtype.equals("LOAD")) {
            qtype = "load";
        } else {
            qtype = "discharge";
        }

        return qtype;
    }

    /**
     * get cell info
     *
     * @param sequenceVO
     * @param acrossBay
     * @return
     */
    public String getCellInfo(SequenceVO sequenceVO, String acrossBay) {
        StringBuffer cellInfo = new StringBuffer();
        String isoog = sequenceVO.getIs_oog();
        String ispowered = sequenceVO.getIs_powered();
        String istank = sequenceVO.getIstank();
        String isquad = sequenceVO.getIsQuad();
        String istwin = sequenceVO.getIsTwin();
        String istandem = sequenceVO.getIsTandem();
        String issingle = sequenceVO.getIsSingle();
        String cellbay = sequenceVO.getBay();
        String complexunit = sequenceVO.getComplexunit();
        /*Begin PCR-VMT-000004 Tony Add*/
        String twentyInd = sequenceVO.getTwentyInd();
        /*End PCR-VMT-000004 Tony Add*/

        if (isoog.equals("1")) {
            cellInfo.append("O");
        }
        if (ispowered.equals("1")) {
            cellInfo.append("R");
        }
        if (istank.equals("1")) {
            cellInfo.append("X");
        }
        if (isquad.equals("1")) {
            cellInfo.append("Q");
        }
        if (istwin.equals("1")) {
            cellInfo.append("W");
        }
        if (istandem.equals("1")) {
            cellInfo.append("T");
        }
        if (acrossBay.equals("1") && issingle.equals("1") && complexunit.equals("")) {
            cellInfo.append(cellbay);
        }
        /*Begin PCR-VMT-000004 Tony Add*/
        if (twentyInd != null && twentyInd.equals("Y")) {
            cellInfo.append("20");
        }
        /*End PCR-VMT-000004 Tony Add*/

        return cellInfo.toString();
    }

    /**
     * change normal tier to vessel tiers
     *
     * @param deckhold
     * @param j
     * @return
     */
    public String getTier(String deckhold, int j) {
        String tier = "";
        if (deckhold.equals("A")) {
            tier = getDeckTier(deckhold, j);
        } else if (deckhold.equals("B")) {
            tier = getHoldTier(deckhold, j);
        }

        return tier;
    }


    /**
     * change normal tier to vessel tier
     */
    public String getHoldTier(String deckhold, int j) {
        String tier = "";

        if (j >= 5) {
            int tiernum = j * 2;
            tier = String.valueOf(tiernum);
        } else if (j == 4) {
            tier = "08";

        } else if (j == 3) {
            tier = "06";

        } else if (j == 2) {
            tier = "04";

        } else if (j == 1) {
            tier = "02";

        } else if (j == 0) {
            tier = "00";

        }

        return tier;
    }

    /**
     * change normal tier to vessel tier
     *
     * @param deckhold
     * @param j
     * @return
     */
    public String getDeckTier(String deckhold, int j) {

        return String.valueOf(78 + j * 2);

    }

    @Override
    public List getColSet() {
        String hql = "from ColSet";
        List list = this.hibernateTemplate.find(hql);
        return list;
    }

    @Override
    public BaySize getBaySize() throws Exception {
        final String sql = " select max(cmrow) as bayrows, max(cmtier) as baytiers from t_cellmatrix where active='1' and  cmtype=?";
        //this.hibernateTemplate.execute(action)
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = this.hibernateTemplate.executeFind(new HibernateCallback() {

            public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {

                Query query = session.createSQLQuery(sql);
                query.setString(0, "A");
                query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

                return query.list();
            }

        });
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list2 = this.hibernateTemplate.executeFind(new HibernateCallback() {

            public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {

                Query query = session.createSQLQuery(sql);
                query.setString(0, "B");
                query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
                return query.list();
            }

        });
        BaySize baySize = new BaySize();
        if (list != null && list.size() > 0) {
            Map<String, Object> item = list.get(0);
            String deckRows = (String) item.get("BAYROWS");
            String deckTiers = (String) item.get("BAYTIERS");
            baySize.setDeckRows(getRealSize(deckRows));
            baySize.setDeckTiers(getRealSize(deckTiers));

        }
        if (list2 != null && list2.size() > 0) {
            Map<String, Object> item = list2.get(0);
            String holdRows = (String) item.get("BAYROWS");
            String holdTiers = (String) item.get("BAYTIERS");
            baySize.setHoldRows(getRealSize(holdRows));
            baySize.setHoldTier(getRealSize(holdTiers));
        }
        return baySize;
    }

    @Override
    public void updateCellMatrix(BaySize baySize) throws Exception {
        String deckTier = String.valueOf(Integer.valueOf(baySize.getDeckTiers()) - 1);
        String sql = "update t_cellmatrix set active='1' ,cmtier='" + deckTier + "' where  cmrow<?  and cmtype=?";
        org.hibernate.Session session = this.hibernateTemplate.getSessionFactory().getCurrentSession();
        Query query = session.createSQLQuery(sql);
        query.setString(0, baySize.getDeckRows());
        query.setString(1, "A");
        query.executeUpdate();
        String holdTier = String.valueOf(Integer.valueOf(baySize.getHoldTiers()) - 1);
        sql = "update t_cellmatrix set active='1', cmtier='" + holdTier + "' where  cmrow<?  and cmtype=?";
        query = session.createSQLQuery(sql);
        query.setString(0, baySize.getHoldRows());
        query.setString(1, "B");
        query.executeUpdate();

        sql = "update t_cellmatrix set active='0' where cmrow>=? and  cmtype=?";
        query = session.createSQLQuery(sql);
        query.setString(0, baySize.getDeckRows());
        query.setString(1, "A");
        query.executeUpdate();
        query.setString(0, baySize.getHoldRows());
        query.setString(1, "B");
        query.executeUpdate();
        //this.hibernateTemplate.bulkUpdate("update t_cellmatrix set active='1' where type='A' and row<? and tier<?", new Object[]{baySize.getDeckRows(), baySize.getDeckTiers()});
        //this.hibernateTemplate.bulkUpdate("update t_cellmatrix set active='1' where type='B' and row<? and tier<?", new Object[]{baySize.getHoldRows(), baySize.getHoldTiers()});
    }

    public String getRealSize(String size) {
        int i = Integer.parseInt(size) + 1;
        String realSize = "" + i;
        return realSize;
    }


    @Override
    public boolean saveOrUpdateColSet(ColSet colSet) {
        boolean success = false;
        try {
            this.hibernateTemplate.saveOrUpdate(colSet);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public boolean delColSet(int id) {
        boolean success = false;
        try {
            ColSet colset = new ColSet();
            colset.setId(id);
            this.hibernateTemplate.delete(colset);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public ColSet getColSetByBoxcase(String boxcase) {
        ColSet colSet = null;
        try {
            String hql = "from ColSet c where c.boxcase=?";
            List list = this.hibernateTemplate.find(hql, boxcase);
            if (list != null && list.size() > 0) {
                colSet = (ColSet) list.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return colSet;
    }

    @Override
    public ColSet getColSetById(int id) {
        String sqlString = "from ColSet c where c.id=?";
        ColSet colSet = (ColSet) this.hibernateTemplate.find(sqlString, id).iterator().next();

        return colSet;
    }

    @Override
    public PageManage getAllCol(final int offset) {
        String countHql = "select count(*) from ColSet";
        int countTotal = DataAccessUtils.intResult(this.hibernateTemplate.find(countHql));

        final String hql = "from ColSet";
        List listDatas = this.hibernateTemplate.executeFind(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                List ls = session.createQuery(hql).setFirstResult(offset).setMaxResults(10).list();
                return ls;
            }
        });

        PageManage page = new PageManage();
        page.setTotal(countTotal);
        page.setDatas(listDatas);
        page.setOffset(offset);
        page.setPagesize(10);

        return page;
    }

    /*Begin PCR-VMT-000004 Tony Add*/
    private HashMap getTwentyUnitList(HashMap hm, String qcID, String VesselID, String bay) throws GeneralException {
        try {
            int bayID = Integer.parseInt(bay);
            int minBayInt = bayID - 1;
            int maxBayInt = bayID + 1;
            String minBay = "";
            String maxBay = "";
            if (bayID != 0) {
                minBay = StringUtils.leftPad(Integer.toString(minBayInt), 2, '0');
                maxBay = StringUtils.leftPad(Integer.toString(maxBayInt), 2, '0');
            } else {
                minBay = StringUtils.leftPad(Integer.toString(maxBayInt), 2, '0');
                maxBay = StringUtils.leftPad(Integer.toString(maxBayInt), 2, '0');
            }
            List<Map<String, Object>> list = null;
            StringBuffer sql = new StringBuffer();
            sql.append(" select iufv.last_pos_slot as current_pos_slot , iu.gkey as unit_fcy_gkey, to_char(re.eqtyp_gkey) as eqtyp_gkey");
            sql.append(" from MN4O_QC_inv_unit_fcy_visit iufv,MN4O_QC_inv_unit iu, ");
            sql.append(" MN4O_QC_argo_carrier_visit acv, ");
            sql.append(" MN4O_QC_ref_equipment re");
            sql.append(" where iufv.unit_gkey = iu.gkey ");
            sql.append(" and re.gkey = iu.eq_gkey ");
            sql.append(" and ((iufv.actual_ib_cv = acv.gkey and iufv.transit_state = 'S20_INBOUND') or ");
            sql.append(" (iufv.actual_ob_cv = acv.gkey and iufv.transit_state = 'S60_LOADED' )) ");
            sql.append(" and acv.id = ? ");
            sql.append(" and substr(iufv.last_pos_slot,1,2) in (?,?) ");
            sql.append(" order by iufv.last_pos_slot ");
            list = this.jdbcTemplate.queryForList(sql.toString(), new Object[]{VesselID, minBay, maxBay});

            List<String> hazardList = new ArrayList();
            List<String> unitList = new ArrayList();
            if (list != null && list.size() > 0) {
                for (Map<String, Object> rs : list) {
                    String unitKey = rs.get("unit_fcy_gkey").toString();
                    unitList.add(unitKey);
                }
                hazardList = getHazardList(unitList);
            }
            if (list != null && list.size() > 0) {
                for (Map<String, Object> rs : list) {
                    String last_pos = (String) rs.get("current_pos_slot");
                    String tierRow = last_pos.substring(4, 6) + last_pos.substring(2, 4);
                    String eqType = (String) rs.get("eqtyp_gkey");
                    String unitGkey = rs.get("unit_fcy_gkey").toString();
                    if (!hm.containsKey(tierRow)) {
                        SequenceVO svo = new SequenceVO();
                        svo.setStatus("inactive");
                        svo.setTwentyInd("Y");
                        svo.setIs_dg(isHazard(unitGkey, hazardList));
                        hm.put(tierRow, svo);
                    } else {
                        SequenceVO oldSVO = (SequenceVO) hm.get(tierRow);
                        String isHazards = isHazard(unitGkey, hazardList);
                        if (StringUtils.isNotBlank(isHazards) && isHazards.equals("1")) {
                            oldSVO.setIs_dg(isHazards);
                            hm.put(tierRow, oldSVO);
                        }
                    }
                }
            }
            return hm;
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getTwentyUnitList()");
            throw new GeneralException("cannot_get_connection");
        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getTwentyUnitList()");
            throw new GeneralException("db_query_time_out");
        } catch (Exception e) {
            LOG.debug(e.toString());
            throw new GeneralException("error_query_db_error");
        }
    }

    private boolean isTwentyUnit(long equipmentType) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select NOMINAL_LENGTH from MN4O_REF_EQUIP_TYPE where gkey = ? ");
        List<Map<String, Object>> resultList = null;
        resultList = this.jdbcTemplate.queryForList(sql.toString(), new Object[]{equipmentType});
        if (resultList != null && resultList.size() > 0) {
            for (Map<String, Object> rs : resultList) {
                String size = (String) rs.get("nominal_length");
                if (size != null && size.indexOf("20") > 0) {
                    return true;
                }
            }
        }
        return false;
    }
    /*End PCR-VMT-000004 Tony Add*/

    /*Start PCR-DG */
    private String isHazard(String unitFcyGkey, List<String> hazardList) {
        String isHazard = "0";
        if (hazardList != null && hazardList.size() > 0) {
            if (hazardList.contains(unitFcyGkey)) {
                isHazard = "1";
            }
        }
        return isHazard;
    }

    private List<String> getHazardList(List<String> unitGkeys) throws GeneralException {
        return null;
        //FIR-TMT-000005 block this SQL function temporary, it will always show no DG indicator in QCVMT.
		/*
		if (unitGkeys == null || unitGkeys.size() < 1){
			return null;
		}
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" select iu.gkey ");
			sql.append(" from MN4O_QC_inv_unit iu, MN4O_QC_inv_hazards ih, MN4O_QC_INV_HAZARD_ITEMS ihi ");
			sql.append(" where iu.goods = ih.owner_gkey ");
			sql.append(" and ih.gkey = ihi.hzrd_gkey ");
			sql.append(" and iu.gkey in ");
			StringBuffer  sb = new StringBuffer();
			sb.append("(");
			for (String key:unitGkeys){
				sb.append("'");
				sb.append(key);
				sb.append("'");
				sb.append(",");
			}
			String gkeys = sb.toString();
			gkeys = StringUtils.left(gkeys, gkeys.length() -1);
			gkeys += ')';
			sql.append(gkeys);
			List<Map<String,Object>> list = this.jdbcTemplate.queryForList(sql.toString());
			
			if (list ==null || list.size() < 1){
				return null;
			}
			List<String> result = new ArrayList();
			for (Map<String,Object> rs:list){
				String hazardUnitGkey = rs.get("gkey").toString();
				result.add(hazardUnitGkey);
			}
			return result;
		}catch(RecoverableDataAccessException e){
			LOG.info("db_query_time_out: getHazardList()" );
			throw new GeneralException("db_query_time_out");
		}catch(CannotGetJdbcConnectionException e){
			LOG.info("cannot_get_connection: getHazardList()");
			throw new GeneralException("cannot_get_connection");
		}catch(Exception e){
		     LOG.debug(e.toString());	
			 throw new GeneralException("error_query_db_error");		
		}
		*/
    }

    /*START CGM170276 HE FENG ADD*/
    List<Map<String, Object>> getFinishedDischList(String qorder, String qcid) {
        List<Map<String, Object>> list = null;
        try {
            StringBuffer sql = new StringBuffer();

            sql.append(" select iufv.last_pos_slot as current_pos_slot, ");
            sql.append(" iu.id as unit_number, ");//CGM170276 HE FENG ADD
            sql.append(" to_char(iufv.time_move,'yyyy-mm-dd hh24:mi:ss') as time_move, ");
            sql.append(" iq.qtype, iw.pos_slot as planned_pos_slot,iw.move_stage, ");
            sql.append(" iq.pos_locid, xpow.name as qc_id, iq.qdeck, iq.qrow,  ");
            sql.append(" iq.qorder, iw.sequence,iu.is_oog, ");
            sql.append(" case when (twin_with ='PREV' or twin_with ='NEXT') and twin_int_fetch=1 and (is_tandem_with_next=1 or is_tandem_with_previous=1 ) then '1' else '0' end as isquad,  ");
            sql.append(" case when twin_with ='NONE' and twin_int_fetch=0 and (is_tandem_with_next=1 or is_tandem_with_previous=1 ) then '1' else '0' end as istandem,  ");
            sql.append(" case when (twin_with ='PREV' or twin_with ='NEXT') and twin_int_fetch=1 and (is_tandem_with_next=0 and is_tandem_with_previous=0 ) then '1' else '0' end as istwin,  ");
            sql.append(" case when twin_with ='NONE' and twin_int_fetch=0 and (is_tandem_with_next=0 and is_tandem_with_previous=0 ) or is_tandem_with_next is null or is_tandem_with_previous is null then '1' else '0' end as issingle,  ");
            sql.append(" case when ig.temp_reqd_c is null then '0' else '1' end as is_powered,");
            sql.append(" case re.iso_group when 'TN' then '1' when 'TD' then '1' when 'TG' then '1' else '0' end as istank ");
            sql.append(", iu.gkey as unit_fcy_gkey ");
            sql.append(" from MN4O_QC_inv_wq iq, MN4O_QC_inv_wi iw,MN4O_QC_inv_unit_yrd_visit iuyv, MN4O_QC_inv_unit_fcy_visit iufv, MN4O_QC_inv_unit iu, ");
            sql.append(" MN4O_QC_xps_craneshift xcs,MN4O_QC_xps_pointofwork xpow,MN4O_QC_ref_equipment re,MN4O_QC_inv_goods ig, MN4O_QC_argo_carrier_visit acv ");
            sql.append(" where ");
            sql.append(" iw.work_queue_gkey = iq.gkey and iw.uyv_gkey = iuyv.gkey and iuyv.ufv_gkey = iufv.gkey and iq.pos_locid = acv.id ");
            sql.append(" and iufv.unit_gkey = iu.gkey and iq.first_shift_pkey=xcs.pkey and xcs.owner_pow=xpow.pkey");
            sql.append(" and re.gkey =  iu.eq_gkey and ig.gkey=iu.goods");
            sql.append(" and acv.phase not in ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED') ");
            sql.append(" and iq.qtype in ('LOAD', 'DISCH') ");
            sql.append(" and iq.qdeck in ('A', 'B') ");
            sql.append(" and iq.pos_loctype = 'VESSEL' ");
            sql.append(" and iq.is_blue = '1' ");
            sql.append(" and iw.move_kind != 'YARD' ");
            sql.append(" and iw.move_kind != 'SHFT' ");
            sql.append(" and xpow.name= ? ");
            sql.append(" and iq.qorder='" + qorder + "' ");
            sql.append(" and iw.move_stage ='COMPLETE'  ");
            sql.append(" order by  iufv.time_move desc  ");

            list = this.jdbcTemplate.queryForList(sql.toString(), new Object[]{qcid});

        } catch (RecoverableDataAccessException e) {
            LOG.info("db_query_time_out: getFinishedDischList()");
        } catch (CannotGetJdbcConnectionException e) {
            LOG.info("cannot_get_connection: getFinishedDischList()");
        } catch (Exception e) {
            LOG.debug(e.toString());
        }
        return list;
    }
    /*END CGM170276 HE FENG ADD*/
}
