package com.springMVC.control;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.springMVC.util.*;
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
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.springMVC.dao.CellDao;
import com.springMVC.dao.UserDao;
import com.springMVC.entity.ColSet;
import com.springMVC.entity.PageManage;
import com.springMVC.entity.ShowLog;
import com.springMVC.entity.User;

@Controller
@RequestMapping("/user")
public class UserControl {

    private static final Log LOG = LogFactory.getLog(UserControl.class);

    @Resource
    private UserDao userDao;
    @Resource
    private CellDao cellDao;

    @Resource
    private ExportHandler exportHandler;

    @Resource
    private ImportHandler importHandler;

    public MessageUtil messageUtil = new MessageUtil();
    public CookiesUtil cookiesUtil = new CookiesUtil();

    @RequestMapping(value = "/index")
    public ModelAndView index(HttpServletRequest request, String local, ModelMap model, String url) {
        request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, Locale.TRADITIONAL_CHINESE);

        if (url != null && "admin".equals(url)) {
            return new ModelAndView("loginAdmin", model);
        }

        String defQCNUM = cookiesUtil.getCookieValueByKey(request, "defQCNUM");
        String defHCNUM = cookiesUtil.getCookieValueByKey(request, "defHCNUM");
        String defCNUM = cookiesUtil.getCookieValueByKey(request, "defCNUM");
        request.setAttribute("defQCNUM", defQCNUM);
        request.setAttribute("defHCNUM", defHCNUM);
        request.setAttribute("defCNUM", defCNUM);
        return new ModelAndView("login", model);
    }

    @RequestMapping(value = "/changeLan")
    public ModelAndView changeLan(HttpServletRequest request, String local, ModelMap model, String url) {

        if ("zh_CN".equals(local)) {
            request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, Locale.SIMPLIFIED_CHINESE);
        } else if ("zh_TW".equals(local)) {
            request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, Locale.TRADITIONAL_CHINESE);
        } else if ("en".equals(local)) {
            request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, Locale.ENGLISH);
        }
        String roles = request.getParameter("label");
        if ("ADMIN".equals(roles)) {
            model.addAttribute("admin", "admin");
        }

        if (url != null && "admin".equals(url)) {
            return new ModelAndView("loginAdmin", model);
        }


        String defQCNUM = cookiesUtil.getCookieValueByKey(request, "defQCNUM");
        String defHCNUM = cookiesUtil.getCookieValueByKey(request, "defHCNUM");
        String defCNUM = cookiesUtil.getCookieValueByKey(request, "defCNUM");
        request.setAttribute("defQCNUM", defQCNUM);
        request.setAttribute("defHCNUM", defHCNUM);
        request.setAttribute("defCNUM", defCNUM);
        return new ModelAndView("login", model);
    }


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView login(HttpServletRequest request, ModelMap model, HttpServletResponse response) {
        try {
            request.getSession().removeAttribute(Constants.USER_LOGIN);
            request.getSession().removeAttribute(Constants.QC_ID);
            request.getSession().removeAttribute("error");
            request.getSession().removeAttribute("msg");

            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String roles = request.getParameter("label");
            String idQc = request.getParameter("qc");
            String idHc = request.getParameter("hc");
            String idC = request.getParameter("c");
            String qcid = "".equals(idQc) ? ("".equals(idHc) ? "C" + idC : "HC" + idHc) : "QC" + idQc;

            if ("ADMIN".equals(roles)) {
                model.addAttribute("admin", "admin");
            }
            if ("USER".equals(roles)) {
                List qcList = userDao.queryQcId();

                if (qcList != null && qcList.size() > 0) {
                    if (!qcList.contains(qcid)) {

                        model.addAttribute("msg", messageUtil.getMessage("error_check_qc_number", request));
                        String defQCNUM = cookiesUtil.getCookieValueByKey(request, "defQCNUM");
                        String defHCNUM = cookiesUtil.getCookieValueByKey(request, "defHCNUM");
                        String defCNUM = cookiesUtil.getCookieValueByKey(request, "defCNUM");
                        request.setAttribute("defQCNUM", defQCNUM);
                        request.setAttribute("defHCNUM", defHCNUM);
                        request.setAttribute("defCNUM", defCNUM);
                        return new ModelAndView("login", model);
                    }
                } else {

                    model.addAttribute("msg", messageUtil.getMessage("error_check_qc_number", request));
                    String defQCNUM = cookiesUtil.getCookieValueByKey(request, "defQCNUM");
                    String defHCNUM = cookiesUtil.getCookieValueByKey(request, "defHCNUM");
                    String defCNUM = cookiesUtil.getCookieValueByKey(request, "defCNUM");
                    request.setAttribute("defQCNUM", defQCNUM);
                    request.setAttribute("defHCNUM", defHCNUM);
                    request.setAttribute("defCNUM", defCNUM);
                    return new ModelAndView("login", model);
                }
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(password);

            LOG.debug("execute login : " + username + "," + password + " START...");
            User u = null;
            try {
                u = userDao.login(user);
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("msg", messageUtil.getMessage("error_db_not_connected", request));
                String defQCNUM = cookiesUtil.getCookieValueByKey(request, "defQCNUM");
                String defHCNUM = cookiesUtil.getCookieValueByKey(request, "defHCNUM");
                String defCNUM = cookiesUtil.getCookieValueByKey(request, "defCNUM");
                request.setAttribute("defQCNUM", defQCNUM);
                request.setAttribute("defHCNUM", defHCNUM);
                request.setAttribute("defCNUM", defCNUM);
                return new ModelAndView("login", model);
            }
            LOG.debug("execute login : " + username + "," + password + " END...");

            if (u != null) {
                String loginRole = u.getRole();
                if (!roles.equals(loginRole)) {
                    model.addAttribute("msg", messageUtil.getMessage("error_user_or_admin", request));
                    String defQCNUM = cookiesUtil.getCookieValueByKey(request, "defQCNUM");
                    String defHCNUM = cookiesUtil.getCookieValueByKey(request, "defHCNUM");
                    String defCNUM = cookiesUtil.getCookieValueByKey(request, "defCNUM");
                    request.setAttribute("defQCNUM", defQCNUM);
                    request.setAttribute("defHCNUM", defHCNUM);
                    request.setAttribute("defCNUM", defCNUM);
                    return new ModelAndView("login", model);
                }
                if ("ADMIN".equals(loginRole)) {
                    qcid = "";
                }
                ShowLog logs = new ShowLog();
                logs.setQcid(qcid);
                logs.setUserid(u.getId());
                logs.setUsername(u.getUsername());
                logs.setLoginTime(WebUtil.getTime());
                logs.setOperation("LOGIN");
                userDao.add(logs);

                model.addObject("u", u);

                request.getSession().setAttribute(Constants.USER_LOGIN, u);
                request.getSession().setAttribute(Constants.QC_ID, qcid);

                if ("ADMIN".equals(loginRole)) {
                    return new ModelAndView("redirect:/user/all.html", model);
                }
                List list = cellDao.getColSet();
                Iterator<ColSet> iterator = list.iterator();
                while (iterator.hasNext()) {
                    ColSet colSet = (ColSet) iterator.next();
                    model.addAttribute(colSet.getBoxcase(), colSet.getColor());
                }

                String facility = userDao.queryFacilityByQcId(qcid);

                model.put("facility", facility);
                model.put("qcNum", qcid);

                if ("USER".equals(roles)) {
                    if (cookiesUtil.findCookieFile(request, "defQCNUM")) {
                        cookiesUtil.updateCookie(request, response, "defQCNUM", idQc);
                    } else {
                        cookiesUtil.createCookie(response, "defQCNUM", idQc);
                    }

                    if (cookiesUtil.findCookieFile(request, "defHCNUM")) {
                        cookiesUtil.updateCookie(request, response, "defHCNUM", idHc);
                    } else {
                        cookiesUtil.createCookie(response, "defHCNUM", idHc);
                    }

                    if (cookiesUtil.findCookieFile(request, "defCNUM")) {
                        cookiesUtil.updateCookie(request, response, "defCNUM", idC);
                    } else {
                        cookiesUtil.createCookie(response, "defCNUM", idC);
                    }
                }

                return new ModelAndView("tqcvmt", model);

            } else {
                model.addAttribute("msg", messageUtil.getMessage("error_nampass_incorrect", request));
                String defQCNUM = cookiesUtil.getCookieValueByKey(request, "defQCNUM");
                String defHCNUM = cookiesUtil.getCookieValueByKey(request, "defHCNUM");
                String defCNUM = cookiesUtil.getCookieValueByKey(request, "defCNUM");
                request.setAttribute("defQCNUM", defQCNUM);
                request.setAttribute("defHCNUM", defHCNUM);
                request.setAttribute("defCNUM", defCNUM);
                return new ModelAndView("login", model);
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("msg", messageUtil.getMessage("error_db_not_connected", request));
        }
        String defQCNUM = cookiesUtil.getCookieValueByKey(request, "defQCNUM");
        String defHCNUM = cookiesUtil.getCookieValueByKey(request, "defHCNUM");
        String defCNUM = cookiesUtil.getCookieValueByKey(request, "defCNUM");
        request.setAttribute("defQCNUM", defQCNUM);
        request.setAttribute("defHCNUM", defHCNUM);
        request.setAttribute("defCNUM", defCNUM);
        return new ModelAndView("login", model);

    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login(HttpServletRequest request, ModelMap model) {
        return index(request, null, model, "");
    }

    @RequestMapping(value = "/loginAdmin", method = RequestMethod.POST)
    public ModelAndView loginAdmin(HttpServletRequest request, ModelMap model, HttpServletResponse response) {
        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String lan = request.getParameter("lan");
            String qcid = "";
            String roles = request.getParameter("label");

            model.addAttribute("admin", "admin");

            User user = new User();
            user.setUsername(username);
            user.setPassword(password);

            LOG.debug("execute login : " + username + "," + password + " START...");
            User u = null;
            try {
                u = userDao.login(user);
            } catch (Exception e) {
                e.printStackTrace();
                return new ModelAndView("loginAdmin", model);
            }
            LOG.debug("execute admin login : " + username + "," + password + " END...");

            if (u != null) {
                String loginRole = u.getRole();
                if (!roles.equals(loginRole)) {
                    model.addAttribute("msg", messageUtil.getMessage("error_user_or_admin", request));
                    return new ModelAndView("loginAdmin", model);
                }

                ShowLog logs = new ShowLog();
                logs.setQcid(qcid);
                logs.setUserid(u.getId());
                logs.setUsername(u.getUsername());
                logs.setLoginTime(WebUtil.getTime());
                logs.setOperation("LOGIN");
                userDao.add(logs);

                model.addObject("u", u);

                request.getSession().setAttribute(Constants.USER_LOGIN, u);
                request.getSession().setAttribute(Constants.QC_ID, qcid);

                return new ModelAndView("redirect:/user/all.html", model);
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("msg", messageUtil.getMessage("error_db_not_connected", request));
        }

        return new ModelAndView("loginAdmin", model);

    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public ModelAndView addUser(@ModelAttribute("user") User user, BindingResult result) {

        return new ModelAndView("userDetail");
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ModelAndView add(HttpServletRequest request, ModelMap model) {
        String username = request.getParameter("username");
        String role = request.getParameter("role");
        String password = request.getParameter("password");
        String qcid = request.getParameter("qcid");

        User us = userDao.getUserByName(username);
        if (us != null) {
            model.addAttribute("result", messageUtil.getMessage("error_username_exists", request));
            return new ModelAndView("userDetail", model);
        }

        User ub = (User) request.getSession().getAttribute(Constants.USER_LOGIN);

        String time = WebUtil.getTime();
        User user = new User();
        user.setUsername(username);
        user.setQcid(qcid);
        user.setRole(role);
        user.setPassword(password);
        user.setCreatetime(time);
        user.setParent(ub.getUsername());

        LOG.debug("execute add USER : " + username + "," + role + " START...");

        try {
            userDao.save(user);
        } catch (Exception e) {
            model.addAttribute("result", messageUtil.getMessage("error_can_not_add_user", request));
            return new ModelAndView("userDetail", model);
        }

        LOG.debug("execute add USER : " + username + "," + role + " END...");

        return new ModelAndView("redirect:/user/all.html");

    }


    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ModelAndView listAllUser(HttpServletRequest request) {
        int offset = 0;
        try {
            offset = Integer.parseInt(request.getParameter("pager.offset"));
        } catch (NumberFormatException ex) {
        }
        Map<String, Object> model = new HashMap<>();
        model.put("pm", userDao.getAllUser(offset));

        User user = (User) request.getSession().getAttribute(Constants.USER_LOGIN);
        String limitAccountStr = PropertiesUtil.getPropertiesValue("limitAccount");
        for (String temp : limitAccountStr.split(",")) {
            if (StringUtils.equals(temp, user.getUsername())) {
                model.put("limit", "Yes");
            }
        }

        return new ModelAndView("admin", model);
    }

    @RequestMapping(value = "/del", method = RequestMethod.GET)
    public ModelAndView delUser(@ModelAttribute("user") User user) {
        try {
            userDao.deleteById(user.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView("redirect:/user/all.html");
    }

    @RequestMapping(value = "/modify", method = RequestMethod.GET)
    public ModelAndView modUser(@ModelAttribute("user") User user, HttpServletRequest request, ModelMap model) {
        User u = userDao.getUserById(user.getId());
        String result = request.getParameter("result");
        model.addObject("u", u);
        try {
            model.addAttribute("result", result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ModelAndView("update", model);
    }


    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ModelAndView updateUser(HttpServletRequest request, ModelMap model) {
        int id = 0;
        String role = request.getParameter("role");
        String password = request.getParameter("password");
        String ids = request.getParameter("u_id");
        String qcid = request.getParameter("qcid");

        try {
            id = Integer.valueOf(ids);
        } catch (NumberFormatException e) {
            id = 0;
        }
        User user = new User();
        user.setRole(role);
        user.setPassword(password);
        user.setId(id);
        user.setQcid(qcid);

        try {
            userDao.update(user);
            return new ModelAndView("redirect:/user/all.html");
        } catch (Exception e) {
            model.addAttribute("result", messageUtil.getMessage("error_can_not_update_user", request));
            return new ModelAndView("redirect:/user/modify.html", model);
        }
    }

    @RequestMapping(value = "/log", method = RequestMethod.GET)
    public ModelAndView showLog(@ModelAttribute("user") User user, ModelMap model, HttpServletRequest request) {
        int ids = 0;
        try {
            ids = user.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (user.getId() == null) {
            try {
                ids = Integer.parseInt(request.getParameter("userid"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int offset = 0;
        try {
            offset = Integer.parseInt(request.getParameter("pager.offset"));
        } catch (NumberFormatException ex) {
            //ex.printStackTrace();
        }
        PageManage log = null;
        try {
            log = userDao.getUserLog(ids, offset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("pm", log);

        return new ModelAndView("log", model);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest request, ModelMap model) {
        String flag = "";
        String qcid = (String) request.getSession().getAttribute(Constants.QC_ID);
        try {
            flag = userDao.logout(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ("yes".equals(flag)) {
            model.addAttribute("msg", "");
        } else {
            model.addAttribute("msg", messageUtil.getMessage("error_webpage_expired", request));
        }

        if ("".equals(qcid)) {
//			return new ModelAndView("loginAdmin",model);
            return new ModelAndView("redirect:/user/index.html?url=admin", model);//CGM70317 HE FENG MODIFY
        } else {
            String defQCNUM = cookiesUtil.getCookieValueByKey(request, "defQCNUM");
            String defHCNUM = cookiesUtil.getCookieValueByKey(request, "defHCNUM");
            String defCNUM = cookiesUtil.getCookieValueByKey(request, "defCNUM");
            request.setAttribute("defQCNUM", defQCNUM);
            request.setAttribute("defHCNUM", defHCNUM);
            request.setAttribute("defCNUM", defCNUM);
//			return new ModelAndView("login",model);
            return new ModelAndView("redirect:/user/index.html", model);//CGM170317 HE FENG MODIFY
        }
    }


    @RequestMapping(value = "/exportLogs", method = RequestMethod.GET)
    public void exportLogs(String fromTime, String toTime, HttpServletRequest request, ModelMap model, HttpServletResponse response) throws ParseException {


        List exportLogsList = new ArrayList();
        exportLogsList = userDao.getUserLogByPeriod(fromTime, toTime);

        exportHandler.exportQCLog(response, exportLogsList);
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public ModelAndView export(HttpServletRequest request, ModelMap model) {

        return new ModelAndView("exportPage", model);
    }

    @RequestMapping(value = "/importVessel", method = RequestMethod.POST)
    public ModelAndView importVessel(HttpServletRequest request, ModelMap model, HttpServletResponse response) throws ParseException {

        try {
            File returnFile = importHandler.uploadFile(request, response);
            importHandler.importVessel(returnFile);
            return new ModelAndView("importPage", model);
        } catch (Exception e) {
            e.printStackTrace();
            if ("error_no_vessel_found_in_n4".equals(e.getMessage())) {
                model.addAttribute("result", messageUtil.getMessage("error_no_vessel_found_in_n4", request));
            } else {
                model.addAttribute("result", messageUtil.getMessage("import_vessel_file_empty", request));
            }
            return new ModelAndView("importPage", model);
        }

    }

    @RequestMapping(value = "/importPage", method = RequestMethod.GET)
    public ModelAndView importPage(HttpServletRequest request, ModelMap model) {

        return new ModelAndView("importPage", model);
    }


}
