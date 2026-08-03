<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>userDetail</title>
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
           function show(){
        	   document.getElementById("ess").style.display="none";
        	   document.getElementById("message").style.display="none";
           }

           function sh(){
        	   if(window.confirm("<spring:message code="confirm_logout" />")){
             	  window.location.href="logout.html";
                }
           }
           
           function back(){
        	  document.getElementById("ess").style.display="none";
        	  document.getElementById("message").style.display="none";
          	  window.location.href="all.html";  
           }
           
           function check(){
        	   document.getElementById("ess").style.display="none";
				var name = document.getElementById("username").value;
				if(name ==""){
                    document.getElementById("show").innerHTML = "<spring:message code="error_username_empty" />";
              	    document.getElementById("message").style.display=''; 
              	    return false;
					}else{
						if(name.length>10){
		                    document.getElementById("show").innerHTML = "<spring:message code="error_username_too_long" />";
		              	    document.getElementById("message").style.display=''; 
		              	    return false;
							}
						}
				var password = document.getElementById("password").value;
				if(password ==""){
                    document.getElementById("show").innerHTML = "<spring:message code="error_password_empty" />";
              	    document.getElementById("message").style.display=''; 
					return false;
					}else{
						if(password.length>6){
		                    document.getElementById("show").innerHTML = "<spring:message code="error_password_too_long" />";
		              	    document.getElementById("message").style.display=''; 
		              	    return false;
							}
						}
				var cpassword = document.getElementById("cpassword").value;
				if(cpassword ==""){
                    document.getElementById("show").innerHTML = "<spring:message code="error_confirm_password_empty" />";
              	    document.getElementById("message").style.display=''; 
					return false;
					}else{
						if(cpassword.length>6){
		                    document.getElementById("show").innerHTML = "<spring:message code="error_confirm_password_too_long" />";
		              	    document.getElementById("message").style.display=''; 
		              	    return false;
							}
						}
				if(password != cpassword){
                    document.getElementById("show").innerHTML = "<spring:message code="error_password__not_match" />";
              	    document.getElementById("message").style.display=''; 
              	    return false;
					}
				if(qcid.length>6){
                    document.getElementById("show").innerHTML = "<spring:message code="error_qcname_too_long" />";
              	    document.getElementById("message").style.display=''; 
              	    return false;
					}
				return true;
               }
     </script>
</head>
<body>
<c:url var="saveUser" value="save.html" />

     <div id="d1">
            <div title="LOGOUT" style="position:absolute;width:100%;cursor:pointer;right:134px;" align="right" ><img src=" <%=request.getContextPath()%>/images/logout.jpg" onclick="sh();"></img></div>
			<div id="d1_head">MODERN TERMINALS</div>
			<div id="d1_body">
			<form:form modelAttribute="user" method="POST" action="${saveUser}" onsubmit="return check();">
					<table cellpadding="0" cellspacing="5" style="text-align:center;" width="100%" >
						<tr>
							<td><spring:message code="username" /> :</td><td><input type="text" title="USERNAME" id="username" name="username" onfocus="show();"/></td>
						</tr>
						<tr>
							<td><spring:message code="qcname" /> :</td><td><input type="text"  title="QCRNAME" id="qcid" name="qcid" maxlength="6" /></td>
						</tr>
						<tr>
						    <td><spring:message code="role" /> :</td>
						    <td>
				                <label><input type="radio" id="userRole" name="role" checked="checked" value="USER"/><spring:message code="user" /></label>&nbsp;&nbsp;
				                <label><input type="radio" id="adminRole" name="role"  value="ADMIN"/><spring:message code="admin" /></label>
						    </td>
						</tr>
						<tr>
						   <td><spring:message code="password" /> :</td><td><input type="password" title="PASSWORD" id="password" width="100%" name="password" onfocus="show();"/></td>
						</tr>
						<tr>
						   <td> <spring:message code="confirm_password" /> :</td><td><input type="password" title="PASSWORD" width="100%" id="cpassword" name="cpassword" onfocus="show();"/></td>
						</tr>
						<tr></tr><tr></tr><tr></tr>
						<tr>
						   <td colspan="2" align='center'>
								<input type="submit" value="<spring:message code="ok" />" id="ok" name="ok"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<input type="button" value="<spring:message code="cancel" />" id="cancel" name="cancel" onclick="back();"/>
							</td>
						</tr>
						<tr></tr><tr></tr>
						<tr id="ess">
					      <c:if test="${!empty result }">
					          <td style="color:red;font-size:15px;" colspan="2" align="center">${result }</td>
					      </c:if>
					    </tr>
					    <tr id="message" style="display:none;"><td id="show" style="color:red;font-size:15px;" colspan="2" align="center"></td></tr>
					</table>
				</form:form>
			</div>
		</div>
</body>
</html>