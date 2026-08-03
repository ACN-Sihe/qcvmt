package com.accenture.vmt;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.springMVC.dao.CellDao;
import com.springMVC.util.GeneralException;
import com.springMVC.util.MessageUtil;
import com.springMVC.util.WebUtil;

public class Busihandler {

    private static final Log LOG = LogFactory.getLog(Busihandler.class);
    public static MessageUtil messageUtil = new MessageUtil();

    public static void returnResponse(HttpServletRequest request, HttpServletResponse response, String qcid, CellDao cellDao) throws IOException, GeneralException {
        response.setContentType("text/xml; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        StringBuffer sb = new StringBuffer();
        StringBuffer sbEx = new StringBuffer();
        String message = "error_query_db_error";
        try {
            HashMap sequenceHm = (HashMap) cellDao.getCells("", qcid);
            String DeckHold = (String) sequenceHm.get("DeckHold");
            if ("A".equals(DeckHold)) {
                DeckHold = "D";
            } else if ("B".equals(DeckHold)) {
                DeckHold = "H";
            }
            String Bay = (String) sequenceHm.get("Bay");
            String order = (String) sequenceHm.get("QOrder");
            String QCAct = (String) sequenceHm.get("QType");
            String Vessl = (String) sequenceHm.get("Vessl");
            String temp = (String) sequenceHm.get("isRefuel");
            String isRefuel = StringUtils.equals(temp, "Yes") ? messageUtil.getMessage("yes", request) : messageUtil.getMessage("no", request);
            String remainCotainers = (String) sequenceHm.get("RemainCotainers");

            sb.append("<isRef>"+temp+"</isRef>");

            String head_Info = "bayNm;QCAct;rmain;reful;Vessl";
            sb.append("<type>");
            sb.append("<dateTimeNow>" + WebUtil.getDateTimeNow() + "</dateTimeNow>");
            sb.append("<hInfo>" + head_Info + "</hInfo>");
            sb.append("<bayNm>" + messageUtil.getMessage("BAY", request) + ":" + Bay + DeckHold + "</bayNm>");
            sb.append("<QCAct>" + messageUtil.getMessage(QCAct, request) + "</QCAct>");
            sb.append("<rmain>" + messageUtil.getMessage("remaining_container", request) + ":" + remainCotainers + "</rmain>");
            sb.append("<reful>" + messageUtil.getMessage("refueling", request) + ":" + isRefuel + "</reful>");
            sb.append("<Vessl>" + Vessl + "</Vessl>");
            //String qcid= (String)request.getSession().getAttribute(Constants.QC_ID);
            String cellTable = (String) sequenceHm.get("cellTable");

            /*START CGM170276 HEFENG Add*/
            String previousLog = (String) request.getSession().getAttribute("log");
            String newLog = (String) sequenceHm.get("log");
            String observedTime = (String) sequenceHm.get("observedTime");
            if (newLog != null && (previousLog == null || !previousLog.equals(newLog))) {

                String ipAddress = request.getHeader("X-Forwarded-For");
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("Proxy-Client-IP");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("HTTP_X_FORWARDED");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("HTTP_CLIENT_IP");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("HTTP_FORWARDED_FOR");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("HTTP_FORWARDED");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("HTTP_VIA");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("REMOTE_ADDR");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getRemoteAddr();
                    if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
                        InetAddress iNet = null;
                        try {
                            iNet = InetAddress.getLocalHost();
                            iNet = InetAddress.getLoopbackAddress();
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                        if (iNet != null) {
                            ipAddress = iNet.getHostAddress();
                        }
                    }
                }

                if (ipAddress != null && ipAddress.contains(",")) {// more than 1 addresses, seperate by ','
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
                if (ipAddress == null) ipAddress = "";
                String session = request.getSession().getId();
                LOG.info("(" + ipAddress + ", " + session + ")At " + observedTime + ", " + newLog);
                request.getSession().setAttribute("log", newLog);
            }
            /*End CGM170276 HEFENG ADD*/
            sb.append("<table_info>");
            sb.append("<table width=\"100%\"  height=\"70%\" align=\"center\">");
            sb.append(cellTable);
            sb.append("</table>");
            sb.append("</table_info>");
            sb.append("</type>");
        } catch (GeneralException e) {
            message = messageUtil.getMessage(e.getMessage(), request);
            sbEx.append("<type>");
            sbEx.append("<dateTimeNow>" + WebUtil.getDateTimeNow() + "</dateTimeNow>");
            sbEx.append("<table_info>");
            sbEx.append("<div align=\"center\">");
            sbEx.append("<br> <br> <br> <br>");
            sbEx.append("<span style=\"color:red;font-size:20px;\" id=\"error_msg\">" + message + "</span>");
            sbEx.append("</div>");
            sbEx.append("</table_info>");
            sbEx.append("</type>");
            sb = sbEx;
            String errMsg = e.getMessage();
            if (!"error_no_qc_working".equalsIgnoreCase(errMsg) &&
                    !"db_query_time_out".equalsIgnoreCase(errMsg) &&
                    !"cannot_get_connection".equals(errMsg)) {
                e.printStackTrace();
                LOG.error("General Exception (" + request.getRequestURI() + ")", e);
            }
        } catch (Exception e) {

            sbEx.append("<type>");
            sbEx.append("<dateTimeNow>" + WebUtil.getDateTimeNow() + "</dateTimeNow>");
            sbEx.append("<table_info>");
            sbEx.append("<div align=\"center\">");
            sbEx.append("<br> <br> <br> <br>");
            sbEx.append("<span style=\"color:red;font-size:20px;\" id=\"error_msg\">" + message + "</span>");
            sbEx.append("</div>");
            sbEx.append("</table_info>");
            sbEx.append("</type>");
            sb = sbEx;
            e.printStackTrace();

            LOG.error("Unknown Exception (" + request.getRequestURI() + ")", e);
        } finally {

            out.write(sb.toString());
            out.flush();
            out.close();

        }
    }
}
