package com.springMVC.dao;

import com.springMVC.entity.*;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;

@Repository
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class VesselDaoImpl implements VesselDao {

    @Resource
    private HibernateTemplate hibernateTemplate;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void save(Vessel vessel) {
        Vessel v = new Vessel();

        @SuppressWarnings("unchecked")
        List<Vessel> list = this.hibernateTemplate.findByNamedParam("from Vessel where vesselid=:vesselid and deck_hold=:deckhold and bay=:bay",
                new String[]{"vesselid", "deckhold", "bay"},
                new Object[]{vessel.getVesselid(), vessel.getDeck_hold(), vessel.getBay()});
        if (list.size() == 0) {
            v = vessel;
        } else {
            v = list.get(0);
            v.setRowStart(vessel.getRowStart());
            v.setRowEnd(vessel.getRowEnd());
            v.setTierStart(vessel.getTierStart());
            v.setTierEnd(vessel.getTierEnd());
        }

        this.hibernateTemplate.saveOrUpdate(v);
    }

    @Override
    public PageManage getAllVessel(final int offset) {
        String countHql = "select count(*) from Vessel order by vesselid,deck_hold,id ";
        int countTotal = DataAccessUtils.intResult(this.hibernateTemplate.find(countHql));

        final String hql = "from Vessel order by vesselid,deck_hold,id ";

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

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(int id) {
        Vessel vessel = new Vessel();
        vessel.setId(id);
        this.hibernateTemplate.delete(vessel);

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void update(Vessel vessel) {
        Vessel uv = this.hibernateTemplate.get(Vessel.class, vessel.getId());
        uv.setVesselid(vessel.getVesselid());
        uv.setDeck_hold(vessel.getDeck_hold());
        uv.setBay(vessel.getBay());
        uv.setRowStart(vessel.getRowStart());
        uv.setRowEnd(vessel.getRowEnd());
        uv.setTierStart(vessel.getTierStart());
        uv.setTierEnd(vessel.getTierEnd());
        this.hibernateTemplate.saveOrUpdate(uv);
    }

    @Override
    public Vessel getVesselById(int id) {
        String sqlString = "from Vessel v where v.id=?";
        Vessel vessel = (Vessel) this.hibernateTemplate.find(sqlString, id).iterator().next();

        return vessel;
    }

    @Override
    public boolean saveOrUpdateVessel(Vessel vessel) {
        boolean success = false;
        try {
            this.hibernateTemplate.saveOrUpdate(vessel);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public boolean saveOrUpdateVessel(List vesselList) {
        boolean success = false;
        try {
            this.hibernateTemplate.saveOrUpdateAll(vesselList);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public List getVesselByCondition(String vesselid, String deck_hold, String bay) {
        List list = null;
        try {
            String hql = "from Vessel where vesselid=? and deck_hold=? and bay=? ";
            list = this.hibernateTemplate.find(hql, new String[]{vesselid, deck_hold, bay});
//			if (list != null && list.size() > 0) {
//				colSet = (ColSet) list.iterator().next();
//			}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public PageManage searchVessel(final int offset, final String key) {
        String countHql = "select count(*) from Vessel where vesselid like :vesselid or deck_hold like :deckhold or bay like :bay order by vesselid,deck_hold,id ";
        int countTotal = DataAccessUtils.intResult(this.hibernateTemplate.findByNamedParam(countHql, new String[]{"vesselid", "deckhold", "bay"}, new Object[]{'%' + key + '%', '%' + key + '%', '%' + key + '%'}));

        final String hql = "from Vessel where vesselid like :vesselid or deck_hold like :deckhold or bay like :bay order by vesselid,deck_hold,id ";

        List listDatas = this.hibernateTemplate.executeFind(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                List ls = session.createQuery(hql)
                        .setString("vesselid", "%" + key + "%")
                        .setString("deckhold", "%" + key + "%")
                        .setString("bay", "%" + key + "%")
                        .setFirstResult(offset)
                        .setMaxResults(10).list();
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

    @Override
    public String getN4VesselNameById(String vesselid) {
        String sql = "select name from MN4O_QC_vsl_vessels where id = ?";
        String vesselName = this.jdbcTemplate.queryForObject(sql, new Object[]{vesselid}, String.class);
        return vesselName;
    }

    @Override
    public List getVesselListByName(String vesselid) {
        List list = null;
        try {
            String hql = "from Vessel where vesselid=?";
            list = this.hibernateTemplate.find(hql, new String[]{vesselid});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public PageManage getAllVesselCol(final int offset) {
        String countHql = "select count(*) from VesselCol order by vesselid,deck_hold,id ";
        int countTotal = DataAccessUtils.intResult(this.hibernateTemplate.find(countHql));

        final String hql = "from VesselCol order by vesselid,deck_hold,id ";

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

    @Override
    public PageManage searchVesselCol(final int offset, final String key) {
        String countHql = "select count(*) from VesselCol where vesselid like :vesselid or deck_hold like :deckhold or bay like :bay order by vesselid,deck_hold,id ";
        int countTotal = DataAccessUtils.intResult(this.hibernateTemplate.findByNamedParam(countHql, new String[]{"vesselid", "deckhold", "bay"}, new Object[]{'%' + key + '%', '%' + key + '%', '%' + key + '%'}));

        final String hql = "from VesselCol where vesselid like :vesselid or deck_hold like :deckhold or bay like :bay order by vesselid,deck_hold,id ";

        List listDatas = this.hibernateTemplate.executeFind(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                List ls = session.createQuery(hql)
                        .setString("vesselid", "%" + key + "%")
                        .setString("deckhold", "%" + key + "%")
                        .setString("bay", "%" + key + "%")
                        .setFirstResult(offset)
                        .setMaxResults(10).list();
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

    @Override
    public VesselCol getVesselColById(int id) {
        String sqlString = "from VesselCol v where v.id=?";
        VesselCol vesselCol = (VesselCol) this.hibernateTemplate.find(sqlString, id).iterator().next();
        return vesselCol;
    }

    @Override
    public boolean saveOrUpdateVesselCol(VesselCol vesselCol) {
        boolean success = false;
        try {
            this.hibernateTemplate.saveOrUpdate(vesselCol);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public void deleteVesselColById(int id) {
        VesselCol vesselCol = new VesselCol();
        vesselCol.setId(id);
        this.hibernateTemplate.delete(vesselCol);
    }

    @Override
    public PageManage getAllVesselRefuel(final int offset) {
        String countHql = "select count(*) from VesselRefuel order by vesselid ";
        int countTotal = DataAccessUtils.intResult(this.hibernateTemplate.find(countHql));

        final String hql = "from VesselRefuel order by vesselid ";

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

    @Override
    public PageManage searchVesselRefuel(final int offset, final String key) {
        String countHql = "select count(*) from VesselRefuel where vesselid like :vesselid or is_refuel like :is_refuel order by vesselid ";
        int countTotal = DataAccessUtils.intResult(this.hibernateTemplate.findByNamedParam(countHql, new String[]{"vesselid", "is_refuel"}, new Object[]{'%' + key + '%', '%' + key + '%'}));

        final String hql = "from VesselRefuel where vesselid like :vesselid or is_refuel like :is_refuel order by vesselid ";

        List listDatas = this.hibernateTemplate.executeFind(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                List ls = session.createQuery(hql)
                        .setString("vesselid", "%" + key + "%")
                        .setString("is_refuel", "%" + key + "%")
                        .setFirstResult(offset)
                        .setMaxResults(10).list();
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

    @Override
    public VesselRefuel getVesselRefuelById(Integer id) {
        String sqlString = "from VesselRefuel v where v.id=?";
        VesselRefuel vesselRefuel = (VesselRefuel) this.hibernateTemplate.find(sqlString, id).iterator().next();
        return vesselRefuel;
    }

    @Override
    public void deleteVesselRefuelById(Integer id) {
        VesselRefuel vesselRefuel = new VesselRefuel();
        vesselRefuel.setId(id);
        this.hibernateTemplate.delete(vesselRefuel);
    }

    @Override
    public boolean saveOrUpdateVesselRefuel(VesselRefuel vesselRefuel) {
        boolean success = false;
        try {
            this.hibernateTemplate.saveOrUpdate(vesselRefuel);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public void saveOperationLog(OperationLog log) {
        try {
            this.hibernateTemplate.saveOrUpdate(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
