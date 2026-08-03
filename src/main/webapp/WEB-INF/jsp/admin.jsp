<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>admin</title>
      <style>
			#d1{
               width:80%;
				
				height: expression((documentElement.clientHeight > 320) ? "320px" : "100%")!important; 
				height:100%; 
				min-height:320px; 

				border:1px solid black;
				margin:0.1% auto;
				background-color:#cccccc;
				
			}
			#d1_head{
				font-size:20px;
				color:white;
				font-family:Times New Roman;
				background-color:blue;
				text-align:center;
				width:100%;
				
			}

     </style>
     <script type="text/javascript">
          function sh(){
              if(window.confirm("<spring:message code="confirm_logout" />")){
            	  window.location.href="logout.html";
               }
        }
        
     </script>
</head>
<body>

    <div id="d1"><div title="LOGOUT" style="position:absolute;width:100%;cursor:pointer;right:134px;" align="right" ><img src=" <%=request.getContextPath()%>/images/logout.jpg" onclick="sh();"></img></div>
        <div id="d1_head">MODERN TERMINALS</div>
        <c:choose>
            <c:when test="${ limit == 'Yes' }">
                <div id="d1_body" style="float:right;margin-right:1%;">
                    <a href="allVesselRefuel.html updateVesselRefuel.html">Vessel <spring:message code="refuel" /><spring:message code="configure" /></a>&nbsp;&nbsp;
                    <a href="allVesselCol.html">Vessel Refuel Bay Row<spring:message code="configure" /></a>
                </div>
            </c:when>
            <c:otherwise>
                <div id="d1_body" style="float:right;margin-right:1%;">
                    <a href="allVesselRefuel.html updateVesselRefuel.html">Vessel <spring:message code="refuel" /><spring:message code="configure" /></a>&nbsp;&nbsp;
                    <a href="allVesselCol.html">Vessel Refuel Bay Row<spring:message code="configure" /></a>&nbsp;&nbsp;
                    <a href="allVessel.html">Vessel <spring:message code="configure" /></a>&nbsp;&nbsp;
                    <a href="export.html"><spring:message code="export_user_logs" /></a>&nbsp;&nbsp;
                    <a href="setbay.html"><spring:message code="set_bay_size" /></a>&nbsp;&nbsp;
                    <a href="allColSet.html"><spring:message code="color" /></a>&nbsp;&nbsp;
                    <a href="add.html"><spring:message code="create_user" /></a>
                </div>
                <div id="d1_body">
                    <table cellpadding="0" cellspacing="5" style="margin:1% auto;text-align:center;font-size:100%;" width="100%">
                        <tr>
                            <td><spring:message code="username" /></td>
                            <td><spring:message code="qcname" /></td>
                            <td><spring:message code="role" /></td>
                            <td><spring:message code="operation" /></td>
                        </tr>

                        <c:if test="${! empty pm.datas}">
                            <c:forEach var="user" items="${pm.datas}">
                                <tr>
                                    <td><c:out value="${user.username}"/></td>
                                    <td><c:out value="${user.qcid}"/></td>
                                    <td><c:out value="${user.role}"/></td>
                                    <td>
                                        <a id="del" href="del.html?id=${user.id}" onclick="return confirm('<spring:message code="confirm_delete" />');"><spring:message code="delete" /></a>&nbsp;&nbsp;
                                        <a href="modify.html?id=${user.id}"><spring:message code="modify" /></a>&nbsp;&nbsp;
                                        <a href="log.html?id=${user.id}"><spring:message code="log" /></a>
                                        <input type="hidden" value="${user.id }" id="userid"/>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:if>
                    </table>
                    <table cellpadding="0" cellspacing="0" align="right" style="font-size:100%;">
                        <tbody>
                            <tr align="right">
                                <td>
                                    <pg:pager items="${pm.total }" maxPageItems="${pm.pagesize }" url="all.html" export="offset,currentPageNumber=pageNumber">
                                       <pg:first>
                                             <a href="${pageUrl}"><spring:message code="home" /></a>
                                       </pg:first>
                                        <pg:prev>
                                             <a href="${pageUrl }"><spring:message code="pre" /></a>
                                        </pg:prev>
                                        <pg:pages>
                                            <c:choose>
                                                <c:when test="${ currentPageNumber eq pageNumber}">
                                                    <font color="red">${pageNumber}</font>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${pageUrl }">${pageNumber }</a>
                                                </c:otherwise>
                                            </c:choose>
                                       </pg:pages>
                                       <pg:next>
                                            <a href="${pageUrl }"><spring:message code="next" /></a>
                                       </pg:next>
                                        <pg:last>
                                            <a href="${pageUrl }"><spring:message code="end" /></a>
                                        </pg:last>
                                     </pg:pager>
                                </td>
                                <td><input type="hidden" value="${pm.userid }" id="idd"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <br><br>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>