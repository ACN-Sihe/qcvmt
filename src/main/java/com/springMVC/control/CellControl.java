package com.springMVC.control;

import com.accenture.vmt.Busihandler;
import com.springMVC.dao.CellDao;
import com.springMVC.dao.VesselDao;
import com.springMVC.entity.*;
import com.springMVC.util.Constants;
import com.springMVC.util.GeneralException;
import com.springMVC.util.LogUtil;
import com.springMVC.util.MessageUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class CellControl {

    private static final Log LOG = LogFactory.getLog(CellControl.class);

    public MessageUtil messageUtil = new MessageUtil();
    @Resource
    private CellDao cellDao;

    @Resource
    private VesselDao vesselDao;

    @RequestMapping(value = "/setbay", method = RequestMethod.GET)
    public ModelAndView setBaySize(HttpServletRequest request, ModelMap model) {
        BaySize baySize = null;
        try {
            baySize = cellDao.getBaySize();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            model.addAttribute("baymsg", messageUtil.getMessage("error_query_db_error", request));
        }
        model.put("baySize", baySize);
        return new ModelAndView("setbaysize", model);
    }

    @RequestMapping(value = "/updateBay", method = RequestMethod.POST)
    public ModelAndView updateBay(@ModelAttribute("baySize") BaySize baySize, HttpServletRequest request, ModelMap model) {
        String holdTiers = (String) request.getParameter("holdTiers");//baysize.holdTiers is null
        baySize.setHoldTier(holdTiers);
        try {
            cellDao.updateCellMatrix(baySize);
        } catch (Exception e) {
            if (e instanceof SQLException) {
                model.addAttribute("baymsg", messageUtil.getMessage("error_db_not_connected", request));
            } else {
                model.addAttribute("baymsg", messageUtil.getMessage("error_can_not_update_bay_size", request));
            }
            return new ModelAndView("setbaysize", model);
        }
        return new ModelAndView("redirect:/user/all.html");
    }

    @RequestMapping(value = "/BusiQuery", method = RequestMethod.GET)
    public void busiQuery(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        String qcNum = request.getParameter("qcNum");
        try {
            Busihandler.returnResponse(request, response, qcNum, cellDao);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralException ge) {
            ge.printStackTrace();
            request.getSession().setAttribute("error", ge.getMessage());
        }
    }

    @RequestMapping(value = "/allColSet", method = RequestMethod.GET)
    public ModelAndView getAllColSet(HttpServletRequest request) {
        int offset = 0;
        try {
            offset = Integer.parseInt(request.getParameter("pager.offset"));
        } catch (NumberFormatException ex) {
            //ex.printStackTrace();
        }

        Map<String, Object> model = new HashMap<String, Object>();
        PageManage colset = null;
        try {
            colset = cellDao.getAllCol(offset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.put("pm", colset);

        return new ModelAndView("colorManage", model);
    }

    @RequestMapping(value = "/delColSet", method = RequestMethod.GET)
    public ModelAndView delUser(@ModelAttribute("colSet") ColSet colSet) {
        cellDao.delColSet(colSet.getId());
        return new ModelAndView("redirect:/user/allColSet.html");
    }

    @RequestMapping(value = "/addColor", method = RequestMethod.GET)
    public ModelAndView addUser(@ModelAttribute("colSet") ColSet colSet, BindingResult result) {
        return new ModelAndView("colSetDetail");
    }

    @RequestMapping(value = "/modifyColSet", method = RequestMethod.GET)
    public ModelAndView modUser(@ModelAttribute("colSet") ColSet colSet, HttpServletRequest request, ModelMap model) {
        ColSet col = cellDao.getColSetById(colSet.getId());
        model.addObject("col", col);

        return new ModelAndView("updateColSet", model);
    }

    @RequestMapping(value = "/updateColSet", method = RequestMethod.POST)
    public ModelAndView updateUser(HttpServletRequest request, ModelMap model) {
        int id = Integer.valueOf(request.getParameter("id"));
        String color = (String) request.getParameter("color");

        ColSet colSet = cellDao.getColSetById(id);
        colSet.setColor(color);

        boolean success = cellDao.saveOrUpdateColSet(colSet);
        if (success) {
            return new ModelAndView("redirect:/user/allColSet.html");
        } else {
            model.addAttribute("result", "The operation failed");
            return new ModelAndView("updateColSet", model);
        }
    }

    @RequestMapping(value = "/saveColSet", method = RequestMethod.POST)
    public ModelAndView saveColSet(HttpServletRequest request, ModelMap model) {
        String boxcase = request.getParameter("boxcase");
        String color = request.getParameter("color");

        if (cellDao.getColSetByBoxcase(boxcase) != null) {
            model.addAttribute("result", "A boxcase with the same name already exists!");
            return new ModelAndView("colSetDetail", model);
        }

        ColSet colSet = new ColSet();
        colSet.setBoxcase(boxcase);
        colSet.setColor(color);
        boolean success = cellDao.saveOrUpdateColSet(colSet);
        if (success) {
            return new ModelAndView("redirect:/user/allColSet.html");
        } else {
            model.addAttribute("result", "The operation failed");
            return new ModelAndView("saveColSet", model);
        }
    }


    ///////////////
    @RequestMapping(value = "/allVessel", method = RequestMethod.GET)
    public ModelAndView getVessel(HttpServletRequest request) {
        int offset = 0;
        try {
            offset = Integer.parseInt(request.getParameter("pager.offset"));
        } catch (NumberFormatException ex) {
            //ex.printStackTrace();
            offset = 0;
        }

        Map<String, Object> model = new HashMap<String, Object>();
        PageManage vessel = null;
        try {
            vessel = vesselDao.getAllVessel(offset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.put("pm", vessel);

        return new ModelAndView("vesselManage", model);
    }

    @RequestMapping(value = "/delVessel", method = RequestMethod.GET)
    public ModelAndView delVessel(@ModelAttribute("vessel") Vessel vessel) {
        vesselDao.deleteById(vessel.getId());
        return new ModelAndView("redirect:/user/allVessel.html");
    }

    @RequestMapping(value = "/addVessel", method = RequestMethod.GET)
    public ModelAndView addVessel(@ModelAttribute("vessel") Vessel vessel, BindingResult result) {
        return new ModelAndView("vesselDetail");
    }

    @RequestMapping(value = "/modifyVessel", method = RequestMethod.GET)
    public ModelAndView modVessel(@ModelAttribute("vessel") Vessel vessel, HttpServletRequest request, ModelMap model) {
        Vessel vessel2 = vesselDao.getVesselById(vessel.getId());
        model.addObject("vessel", vessel2);

        return new ModelAndView("updateVessel", model);
    }

    @RequestMapping(value = "/updateVessel", method = RequestMethod.POST)
    public ModelAndView updateVessel(HttpServletRequest request, ModelMap model) {
        int id = Integer.valueOf(request.getParameter("id"));
        String vesselid = request.getParameter("vesselid");
        String deck_hold = request.getParameter("deck_hold");
        String bay = request.getParameter("bay");


        List list = vesselDao.getVesselByCondition(vesselid, deck_hold, bay);
        if (list != null && list.size() > 0) {
            model.addAttribute("result", "the vesselid,deck_hold,bay already exists!");
            return new ModelAndView("updateVessel", model);
        }

        Vessel uv = vesselDao.getVesselById(id);

        uv.setVesselid(vesselid);
        uv.setDeck_hold(deck_hold);
        uv.setBay(bay);
        uv.setRowStart(request.getParameter("rowStart"));
        uv.setRowEnd(request.getParameter("rowEnd"));
        uv.setTierStart(request.getParameter("tierStart"));
        uv.setTierEnd(request.getParameter("tierEnd"));

        boolean success = vesselDao.saveOrUpdateVessel(uv);
        if (success) {
            return new ModelAndView("redirect:/user/allVessel.html");
        } else {
            model.addAttribute("result", "The operation failed");
            return new ModelAndView("updateVessel", model);
        }
    }

    @RequestMapping(value = "/saveVessel", method = RequestMethod.POST)
    public ModelAndView saveVessel(HttpServletRequest request, ModelMap model) {
        String vesselid = request.getParameter("vesselid");
        String deck_hold = request.getParameter("deck_hold");
        String bay = request.getParameter("bay");


        List list = vesselDao.getVesselByCondition(vesselid, deck_hold, bay);
        if (list != null && list.size() > 0) {
            model.addAttribute("result", "the vesselid,deck_hold,bay already exists!");
            return new ModelAndView("vesselDetail", model);
        }

        Vessel uv = new Vessel();
        uv.setVesselid(request.getParameter("vesselid"));
        uv.setDeck_hold(request.getParameter("deck_hold"));
        uv.setBay(request.getParameter("bay"));
        uv.setRowStart(request.getParameter("rowStart"));
        uv.setRowEnd(request.getParameter("rowEnd"));
        uv.setTierStart(request.getParameter("tierStart"));
        uv.setTierEnd(request.getParameter("tierEnd"));
        boolean success = vesselDao.saveOrUpdateVessel(uv);
        if (success) {
            return new ModelAndView("redirect:/user/allVessel.html");
        } else {
            model.addAttribute("result", "The operation failed");
            return new ModelAndView("saveVessel", model);
        }
    }

    @RequestMapping(value = "/searchVessel", method = RequestMethod.GET)
    public ModelAndView searchCompanyTractor(HttpServletRequest request, String key) {
        int offset = 0;
        try {
            offset = Integer.parseInt(request.getParameter("pager.offset"));
        } catch (NumberFormatException ex) {
        }
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("pm", vesselDao.searchVessel(offset, key));
        model.put("searchKey", key);
        //model.put("path", "tractor_company_setting");
        return new ModelAndView("vesselManage", model);
    }

    @RequestMapping(value = "/allVesselRefuel", method = RequestMethod.GET)
    public ModelAndView getVesselRefuel(HttpServletRequest request) {
        int offset = 0;
        try {
            offset = Integer.parseInt(request.getParameter("pager.offset"));
        } catch (NumberFormatException ex) {
            offset = 0;
        }

        Map<String, Object> model = new HashMap<>();
        PageManage vesselRefuel = null;
        try {
            vesselRefuel = vesselDao.getAllVesselRefuel(offset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.put("pm", vesselRefuel);

        return new ModelAndView("vesselRefuelManage", model);
    }

    @RequestMapping(value = "/searchVesselRefuel", method = RequestMethod.GET)
    public ModelAndView searchVesselRefuel(HttpServletRequest request, String key) {
        int offset = 0;
        try {
            offset = Integer.parseInt(request.getParameter("pager.offset"));
        } catch (NumberFormatException ex) {
        }
        Map<String, Object> model = new HashMap<>();
        model.put("pm", vesselDao.searchVesselRefuel(offset, key));
        model.put("searchKey", key);
        return new ModelAndView("vesselRefuelManage", model);
    }

    @RequestMapping(value = "/addVesselRefuel", method = RequestMethod.GET)
    public ModelAndView addVesselRefuel(HttpServletRequest request) {
        return new ModelAndView("vesselRefuelDetail");
    }

    @RequestMapping(value = "/modifyVesselRefuel", method = RequestMethod.GET)
    public ModelAndView updateVesselRefuel(@ModelAttribute("vesselRefuel") VesselRefuel vesselRefuel, HttpServletRequest request, ModelMap model) {
        VesselRefuel vesselRefuel2 = vesselDao.getVesselRefuelById(vesselRefuel.getId());
        model.addObject("vesselRefuel", vesselRefuel2);
        return new ModelAndView("vesselRefuelDetail", model);
    }

    @RequestMapping(value = "/delVesselRefuel", method = RequestMethod.GET)
    public ModelAndView delVesselRefuel(HttpServletRequest request, @ModelAttribute("vesselRefuel") VesselRefuel vesselRefuel) {
        vesselDao.deleteVesselRefuelById(vesselRefuel.getId());
        User user = (User) request.getSession().getAttribute(Constants.USER_LOGIN);
        vesselDao.saveOperationLog(LogUtil.buildOperationLog(user, LogUtil.Function.VESSEL_REFUEL_CONFIGURATION,
                LogUtil.ActionType.DELETE, vesselRefuel.toString(), null));
        return new ModelAndView("redirect:/user/allVesselRefuel.html");
    }

    @RequestMapping(value = "/updateVesselRefuelStatus", method = RequestMethod.POST)
    public ModelAndView updateVesselRefuelStatus(HttpServletRequest request, ModelMap model) {
        String vesselid = request.getParameter("vesselid");
        String is_refuel = request.getParameter("is_refuel");
        String idStr = request.getParameter("id");

        VesselRefuel existVR = null;
        if (StringUtils.isNotBlank(idStr)) {
            int id = Integer.parseInt(idStr);
            existVR = vesselDao.getVesselRefuelById(id);
        }
        User user = (User) request.getSession().getAttribute(Constants.USER_LOGIN);
        if (existVR != null) {
            // update VesselRefuel
            String oldValue = existVR.toString();
            existVR.setVesselid(vesselid);
            existVR.setIs_refuel(is_refuel);
            boolean success = vesselDao.saveOrUpdateVesselRefuel(existVR);
            if (success) {
                vesselDao.saveOperationLog(LogUtil.buildOperationLog(user, LogUtil.Function.VESSEL_REFUEL_CONFIGURATION,
                        LogUtil.ActionType.UPDATE, oldValue, existVR.toString()));
                return new ModelAndView("redirect:/user/allVesselRefuel.html");
            } else {
                model.addAttribute("result", "The operation failed");
                return new ModelAndView("vesselRefuelDetail", model);
            }
        } else {
            // add VesselRefuel
            VesselRefuel vr = new VesselRefuel();
            vr.setVesselid(vesselid);
            vr.setIs_refuel(is_refuel);
            boolean success = vesselDao.saveOrUpdateVesselRefuel(vr);
            if (success) {
                vesselDao.saveOperationLog(LogUtil.buildOperationLog(user, LogUtil.Function.VESSEL_REFUEL_CONFIGURATION,
                        LogUtil.ActionType.SAVE, null, vr.toString()));
                return new ModelAndView("redirect:/user/allVesselRefuel.html");
            } else {
                model.addAttribute("result", "The operation failed");
                return new ModelAndView("vesselRefuelDetail", model);
            }
        }
    }

    @RequestMapping(value = "/allVesselCol", method = RequestMethod.GET)
    public ModelAndView getVesselCol(HttpServletRequest request) {
        int offset = 0;
        try {
            offset = Integer.parseInt(request.getParameter("pager.offset"));
        } catch (NumberFormatException ex) {
            offset = 0;
        }

        Map<String, Object> model = new HashMap<>();
        PageManage vesselCol = null;
        try {
            vesselCol = vesselDao.getAllVesselCol(offset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.put("pm", vesselCol);

        return new ModelAndView("vesselColorManage", model);
    }

    @RequestMapping(value = "/searchVesselColor", method = RequestMethod.GET)
    public ModelAndView searchVesselCol(HttpServletRequest request, String key) {
        int offset = 0;
        try {
            offset = Integer.parseInt(request.getParameter("pager.offset"));
        } catch (NumberFormatException ex) {
        }
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("pm", vesselDao.searchVesselCol(offset, key));
        model.put("searchKey", key);
        return new ModelAndView("vesselColorManage", model);
    }

    @RequestMapping(value = "/addVesselCol", method = RequestMethod.GET)
    public ModelAndView addVesselBayColor(HttpServletRequest request) {
        return new ModelAndView("vesselColorDetail");
    }

    @RequestMapping(value = "/modifyVesselCol", method = RequestMethod.GET)
    public ModelAndView modVesselCol(@ModelAttribute("vesselCol") VesselCol vesselCol, HttpServletRequest request, ModelMap model) {
        VesselCol vesselCol2 = vesselDao.getVesselColById(vesselCol.getId());
        model.addObject("vesselCol", vesselCol2);
        return new ModelAndView("vesselColorDetail", model);
    }

    @RequestMapping(value = "/delVesselCol", method = RequestMethod.GET)
    public ModelAndView delVesselCol(HttpServletRequest request, @ModelAttribute("vesselCol") VesselCol vesselCol) {
        vesselDao.deleteVesselColById(vesselCol.getId());
        User user = (User) request.getSession().getAttribute(Constants.USER_LOGIN);
        vesselDao.saveOperationLog(LogUtil.buildOperationLog(user, LogUtil.Function.VESSEL_REFUEL_BAY_ROW_CONFIGURATION,
                LogUtil.ActionType.DELETE, vesselCol.toString(), null));
        return new ModelAndView("redirect:/user/allVesselCol.html");
    }

    @RequestMapping(value = "/saveVesselCol", method = RequestMethod.POST)
    public ModelAndView saveOrUpdateVesselCol(HttpServletRequest request, ModelMap model) {
        String vesselid = request.getParameter("vesselid");
        String deck_hold = request.getParameter("deck_hold");
        String bay = request.getParameter("bay");
        String rowStart = request.getParameter("rowStart");
        String rowEnd = request.getParameter("rowEnd");
        String tierStart = request.getParameter("tierStart");
        String tierEnd = request.getParameter("tierEnd");

        String idStr = request.getParameter("id");
        VesselCol existVC = null;
        if (StringUtils.isNotBlank(idStr)) {
            int id = Integer.parseInt(idStr);
            existVC = vesselDao.getVesselColById(id);
        }
        User user = (User) request.getSession().getAttribute(Constants.USER_LOGIN);
        if (existVC != null) {
            // update VesselCol
            String oldValue = existVC.toString();
            existVC.setVesselid(vesselid);
            existVC.setDeck_hold(deck_hold);
            existVC.setBay(bay);
            existVC.setRowStart(rowStart);
            existVC.setRowEnd(rowEnd);
            existVC.setTierStart(tierStart);
            existVC.setTierEnd(tierEnd);
            boolean success = vesselDao.saveOrUpdateVesselCol(existVC);
            if (success) {
                vesselDao.saveOperationLog(LogUtil.buildOperationLog(user, LogUtil.Function.VESSEL_REFUEL_BAY_ROW_CONFIGURATION,
                        LogUtil.ActionType.UPDATE, oldValue, existVC.toString()));
                return new ModelAndView("redirect:/user/allVesselCol.html");
            } else {
                model.addAttribute("result", "The operation failed");
                return new ModelAndView("vesselColorDetail", model);
            }
        } else {
            // add VesselCol
            VesselCol vc = new VesselCol();
            vc.setVesselid(vesselid);
            vc.setDeck_hold(deck_hold);
            vc.setBay(bay);
            vc.setRowStart(rowStart);
            vc.setRowEnd(rowEnd);
            vc.setTierStart(tierStart);
            vc.setTierEnd(tierEnd);
            boolean success = vesselDao.saveOrUpdateVesselCol(vc);
            if (success) {
                vesselDao.saveOperationLog(LogUtil.buildOperationLog(user, LogUtil.Function.VESSEL_REFUEL_BAY_ROW_CONFIGURATION,
                        LogUtil.ActionType.SAVE, null, vc.toString()));
                return new ModelAndView("redirect:/user/allVesselCol.html");
            } else {
                model.addAttribute("result", "The operation failed");
                return new ModelAndView("vesselColorDetail", model);
            }
        }
    }
}
