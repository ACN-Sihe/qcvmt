<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>showLog</title>
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
			}
			#d1_body{
				
				
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
<body >

<c:url var="showlog" value="/user/all.html" />
    <div id="d1">
            <div title="LOGOUT" style="position:absolute;width:100%;cursor:pointer;right:134px;" align="right" ><img src=" <%=request.getContextPath()%>/images/logout.jpg" onclick="sh();"></img></div>
			<div id="d1_head">MODERN TERMINALS</div>
			<div id="d1_body">
				<form:form method="GET" action="${showlog}">
					<table cellpadding="0" cellspacing="5" style="margin:0.1%;text-align:center" width="100%">
					     
					     <tr>
						     <td><spring:message code="username" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
							 <td><spring:message code="qc_number" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
							 <td><spring:message code="time" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
							 <td><spring:message code="operation" /></td>
						</tr>
					     
						<c:if test="${! empty pm.datas}">
							
							<c:forEach var="log" items="${pm.datas}">
								<tr>
									<td><c:out value="${log.username}"/></td>
									<td><c:out value="${log.qcid}"/></td>
									<td>
										<fmt:parseDate value="${log.loginTime }" pattern="yyyyMMddHHmmss" var="test"/>
										<fmt:formatDate value="${test}" pattern="yyyy-MM-dd HH:mm:ss"/>
									</td>
									<td><c:out value="${log.operation}"/></td>
								</tr>
							</c:forEach>
						</c:if>
					</table>
				    <table cellpadding="0" cellspacing="0" align="right">
					    <tbody>
					       <tr align="right">
					           <td>
								    <pg:pager items="${pm.total }" maxPageItems="${pm.pagesize }" url="log.html" export="offset,currentPageNumber=pageNumber">
								           <pg:param name="userid" value="${pm.userid }"/>
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
												<a href="${pageUrl }"><spring:message code="end" /></a>&nbsp;&nbsp;
											</pg:last>
								     </pg:pager>
							    </td>
							    <td><input type="hidden" value="${pm.userid }" id="idd"/></td>
							 </tr>
				 		 </tbody>
					  </table>
			          <br>
                      <div style="text-align:center;"><input type="submit" value="<spring:message code="back" />"/></div>
					 
			   </form:form>
		  </div>
	</div>
</body>
</html>