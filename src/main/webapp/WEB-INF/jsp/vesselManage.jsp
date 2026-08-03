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
<title>vessel manage</title>
    <style>
			#d1{
				width:80%;
				
				height: expression((documentElement.clientHeight > 200) ? "330px" : "100%")!important; 
				height:100%; 
				min-height:200px; 
				
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

 		function search(){
    		var key = document.getElementById("searchcontent").value;     	
	  		window.location.href="searchVessel.html?key="+encodeURIComponent(key);
 		}
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
	<div id="d1"><div title="LOGOUT" style="position:absolute;width:100%;cursor:pointer;right:134px;" align="right" ><img src=" <%=request.getContextPath()%>/images/logout.jpg" onclick="sh();"></img></div>
		<div id="d1_head">MODERN TERMINALS</div>
		<div id="d1_body">
			<table cellpadding="0" cellspacing="5" style="margin:1% auto;text-align:center;font-size:100%;" width="100%" >
						<tr>
					     	<td colspan="4" align="left">
						    	<input type="text" title="searchcontent" id="searchcontent" width="" name="searchcontent" value="<c:out value="${searchKey}"/>"/>&nbsp; <input type="button" value="Search" id="Search" name="Search" onclick="search();" />
						  	</td>
						  	<td colspan="2" align="right"> 
						  		<a href="importPage.html"><spring:message code="import_vessel_info" /></a>&nbsp;&nbsp;
						  	</td>
					     	<td colspan="2" align="right"> 
						    	<a href="addVessel.html"><spring:message code="add" /></a>
						  	</td>
					     </tr>
				<tr>
					<td>Vessel <spring:message code="name" /></td>
					<td><spring:message code="deck" />/<spring:message code="hold" /></td>
					<td><spring:message code="BAY" /></td>
					<td><spring:message code="rows" /></td>
					<td><spring:message code="rows" /></td>
					<td><spring:message code="tiers" /></td>
					<td><spring:message code="tiers" /></td>
					<td><spring:message code="operation" /></td>
				</tr>				
				<c:choose>
					<c:when test="${!empty pm.datas}">
						<c:forEach var="vessel" items="${pm.datas}">
							<tr>
								<td><c:out value="${vessel.vesselid}"/></td>
								<td><c:out value="${vessel.deck_hold}"/></td>
								<td><c:out value="${vessel.bay}"/></td>
								<td><c:out value="${vessel.rowStart}"/></td>
								<td><c:out value="${vessel.rowEnd}"/></td>
								<td><c:out value="${vessel.tierStart}"/></td>
								<td><c:out value="${vessel.tierEnd}"/></td>
								<td>
									<a id="del" href="delVessel.html?id=${vessel.id}" onclick="return confirm('<spring:message code="confirm_delete" />');"><spring:message code="delete" /></a>&nbsp;&nbsp;
									<a href="modifyVessel.html?id=${vessel.id}"><spring:message code="modify" /></a>
								</td>
							</tr>
						</c:forEach>
					</c:when>					
				</c:choose>	
			</table>
			
			<c:if test="${! empty searchKey}">
				<c:set var="pageURL" value="searchVessel.html" />
			</c:if>
			<c:if test="${ empty searchKey}">
				<c:set var="pageURL" value="allVessel.html" />
			</c:if>
			<table cellpadding="0" cellspacing="0" align="right" style="font-size:100%;">
					  <tbody>
					       <tr align="right">
					           <td>
								    <pg:pager items="${pm.total}" maxPageItems="${pm.pagesize}" url="${pageUrl}" export="offset,currentPageNumber=pageNumber">
								          <pg:param name="key" value="${searchKey}"/>
								           <pg:first>
											     <a href="${pageUrl}"><spring:message code="home" /></a>
										   </pg:first>
										    <pg:prev>
												 <a href="${pageUrl}"><spring:message code="pre" /></a>
										    </pg:prev>
										    <pg:pages>
												<c:choose>
													<c:when test="${currentPageNumber eq pageNumber}">
														<font color="red">${pageNumber}</font>
													</c:when>
													<c:otherwise>
														<a href="${pageUrl}">${pageNumber}</a>
													</c:otherwise>
												</c:choose>
										   </pg:pages>
										   <pg:next>
												<a href="${pageUrl}"><spring:message code="next" /></a>
										   </pg:next>
									        <pg:last>
												<a href="${pageUrl}"><spring:message code="end" /></a>
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