<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>import Page</title>
    <style>
			#d1{

				width:80%;
				height: expression((documentElement.clientHeight > 200) ? "200px" : "100%")!important; 
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

     
     function check(){
			var filename = document.getElementById("filename").value;
			if(filename==null || filename==""){
				alert("filename can't be empty.");
				return false;
			}
			//window.location.href="importVessel.html?filename="+filename;
			//window.location.href="importVessel.html";
			return true; 
	}

     function sh(){
   	   if(window.confirm("<spring:message code="confirm_logout" />")){
        	  window.location.href="logout.html";
          }
      }
     
	function bback(){		   	 	
     	 window.location.href="allVessel.html";  
     
	}
     </script>
</head>
<body>
<c:url var="importVessel" value="/user/importVessel.html" /> 

     <div id="d1">
            <div title="LOGOUT" style="position:absolute;width:100%;cursor:pointer;right:134px;" align="right" ><img src=" <%=request.getContextPath()%>/images/logout.jpg" onclick="sh();"></img></div>
			<div id="d1_head">MODERN TERMINALS</div>
			<div id="d1_body">
			<form:form modelAttribute="user" method="POST"  enctype="multipart/form-data" action="${importVessel}" onsubmit="return check();">
					<table cellpadding="0" cellspacing="5" style="text-align:center;" width="100%" >
						<tr>
							<td colspan="4">
								&nbsp;
							</td>
						</tr>
						<tr>
							<td colspan="4">
								&nbsp;
							</td>
						</tr>
						<tr>
							<td colspan="4" align="center">	 				     		
					     		<spring:message code="filename" />:<input type="file" id="filename" name="filename" title="filename"  />
					     		<input type="submit" id="importButton" name="importButton" value="<spring:message code="import"/>"  />
					     	</td>
						</tr>
						<tr>
							<td colspan="4">
								&nbsp;
							</td>
						</tr>
						<tr>
							<td colspan="4" align="center">
								<input type="button" value="<spring:message code="back"/>" id="back" name="back" onclick="bback();" />
							</td>
						</tr>
						<tr></tr><tr></tr><tr></tr><tr></tr><tr></tr><tr></tr>
						<tr id="ess">
					      <c:if test="${!empty result }">
					          <td style="color:red;font-size:15px;" colspan="2" align="center">${result }</td>
					      </c:if>
					  </tr>
					</table>
				</form:form>
			</div>
		</div>
</body>
</html>