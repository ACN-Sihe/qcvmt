<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>color manage</title>
    <style>
			#d1{
				width:600px;
				
				border:1px solid black;
				margin:0.1% auto;
				background-color:#cccccc;
				
				position: relative;
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
			
			td.case_lbl {
				text-align: left;
			}

     </style>
     
     <script type="text/javascript">
           function sh(){
        	   if(window.confirm("<spring:message code="confirm_logout" />")){
             	  window.location.href="logout.html";
                }
           }

   		function back() {
			window.location.href = "all.html";
		}
     </script>
</head>
<body>
	<div id="d1">
		<div title="LOGOUT" style="position:absolute;cursor:pointer;right:0px;" align="right" ><img src=" <%=request.getContextPath()%>/images/logout.jpg" onclick="sh();"></img></div>
		<div id="d1_head">MODERN TERMINALS</div>
		<div id="d1_body">
			<table cellpadding="0" cellspacing="5" style="margin:1% auto;text-align:center;font-size:100%;" width="100%" >
				<tr>
					<td style="width:7%">&nbsp;</td>
					<td style="width:20%;text-align:left"><spring:message code="box_case" /></td>
					<td style="width:46%"><spring:message code="color" /></td>
					<td style="width:27%"><spring:message code="operation" /></td>
				</tr>
				<c:choose>
					<c:when test="${!empty pm.datas}">
						<c:forEach var="col" items="${pm.datas}">
							<tr>
								<td></td>
								<td class="case_lbl"><c:out value="${col.boxcase}"/></td>
<!--								<td><c:out value="${col.color}"/></td>-->
								<td><input size="2" style="background-color:${col.color};" readonly="readonly"/></td>
								<td>
									<a href="modifyColSet.html?id=${col.id}"><spring:message code="modify" /></a>
								</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:otherwise>
						<tr>
							<td colspan="3"><spring:message code="no_color_setting_data" /></td>
					</c:otherwise>
				</c:choose>
			</table>
			
			<table cellpadding="0" cellspacing="0" align="right" style="font-size:100%;">
					  <tbody>
					       <tr align="right">
					           <td>
								    <pg:pager items="${pm.total }" maxPageItems="${pm.pagesize }" url="allColSet.html" export="offset,currentPageNumber=pageNumber">
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
							</tr>
				 	</tbody>
			</table>
			<br>
			<div style="text-align:center;"><input type="button" value="<spring:message code="back" />" id="back" name="back" onclick="back();" /></div>
		    <br>
		</div>
	</div>
</body>
</html>