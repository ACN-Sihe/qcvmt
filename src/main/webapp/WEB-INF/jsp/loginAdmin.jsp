<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ page language="java" import="com.springMVC.util.CookiesUtil"%>
<%@ page language="java" import="java.util.*"%>
<%@ page language="java" import="org.springframework.web.servlet.i18n.SessionLocaleResolver"%>

 <%
  String local = request.getParameter("local");
  Locale locale = (Locale)request.getSession().getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
  String language = locale.getLanguage();
  String country = locale.getCountry();
  String locLan ="";
  if(country == null || "".equals(country)){
	  locLan = language;
  }else{
	  locLan =  language+"_"+country;
  }
     
%>



<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>admin login QCVMT</title>
      <style>
			#d1{

				width:80%;
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
			
			#loginForm td {
				padding: 15px 0;
			}

     </style>
       <script type="text/javascript">
           
           function show(){
        	   //document.getElementById("ess").style.display="none";
        	   //document.getElementById("message").style.display="none";
               }
           
           function set(){
        	   document.getElementById("ess").style.display="none";
        	   document.getElementById("message").style.display="none";
        	   document.getElementById("username").value='';        	   
        	   document.getElementById("password").value='';
        	   
				
               }

           function check(){
        	   document.getElementById("ess").style.display="none";
              
                     var name = document.getElementById("username");
                     if(name.value ==""){
                           document.getElementById("show").innerHTML = "<spring:message code="error_username_empty" />";
                     	  document.getElementById("message").style.display=''; 
                     	  return false;
                         }
 					 					var pwd = document.getElementById("password");
                     if(pwd.value ==""){
                         document.getElementById("show").innerHTML = "<spring:message code="error_password_empty" />";
                   	    document.getElementById("message").style.display=''; 
                   	    return false;
                         }
                       
               return true;
               }

           function init(){
        	    document.getElementById("username").focus(); 								
               }

           function changeLan(){
        	   var value = document.getElementById("lan").value;
        	   var form = document.forms[0];
               form.action = "${pageContext.request.contextPath}/user/changeLan.html?local="+value+"&url=admin";;
               form.method = "post";
               form.submit();
        	   
               	   
           }

           function sh(){
        	    if(window.confirm("<spring:message code="confirm_logout" />")){
        	  	  window.location.href="logout.html";
        	     }
        	}

        	function mykeydown(e){   
							var ele = window.event.srcElement; 
			        	 if (window.event.keyCode==109){ 
				        	 document.getElementById(ele.id).value="";
				        	 e.returnValue=false;  
				        	 }
			        	 if (window.event.keyCode==107){ 
							if(ele.id=="username"){
							document.getElementById("qc").focus(); 
					
							}
							if(ele.id=="qc"){
							document.getElementById("password").focus(); 
		
							}
							if(ele.id=="password"){
							document.getElementById("username").focus(); 
							}
							e.returnValue=false;  							
				 		}  
			     	 
        	}
        	
     </script>

</head>
<body onload="init();" onkeydown="mykeydown(event)">
	<c:url var="saveUser" value="/user/loginAdmin.html" />
	<div id="d1">
		<div id="d1_head">MODERN TERMINALS</div>
		<div id="d1_body">
			<form:form id="loginForm" method="post" action="${saveUser}" onsubmit="return check();">
				<input type="hidden" value="ADMIN" name="label" id="lab"/>
				<table style="margin:1% auto;" width="80%">
				<tr>
					<td align="right"><spring:message code="username" /> :</td>
					<td align="left">
						<input type="text" id="username" name="username" title="USERNAME"  value="admin" style="width:85%;" onfocus="show();">
					</td>
				</tr>
				<tr>
					<td align="right"><spring:message code="password" /> :</td>
					<td align="left">
						<input type="password" id="password" name="password" title="PASSWORD" value="admin" style="width:85%;" onfocus="show();"/>
					</td>
				</tr>
				<tr>
					<td align="right"><spring:message code="language" /> :</td>
					<td align="left">
						<select id="lan" name="lan" onchange="changeLan();" >
							<%if (locLan == null || "".equals(locLan)){ %>										
										<option value="zh_TW"  selected="selected"><spring:message code="Traditional_Chinese" /></option>
								<option value ="en"><spring:message code="English" /></option>
							<%}else if("zh_TW".equals(locLan)){ %>										
										<option value="zh_TW"  selected="selected"><spring:message code="Traditional_Chinese" /></option>
								<option value ="en"><spring:message code="English" /></option>
							<%}else if("zh_CN".equals(locLan)){ %>										
										<option value="zh_TW"  selected="selected"><spring:message code="Traditional_Chinese" /></option>
								<option value ="en"><spring:message code="English" /></option>
							<%}else if("en".equals(locLan)){ %>										
										<option value="zh_TW" ><spring:message code="Traditional_Chinese" /></option>
								<option value ="en" selected="selected"><spring:message code="English" /></option>
							<%}else{%>										
										<option value="zh_TW"  selected="selected"><spring:message code="Traditional_Chinese" /></option>
								<option value ="en"><spring:message code="English" /></option>
							<%} %>
						</select>
					</td>
				</tr>
				<tr>
					<td colspan="2" align='center'>
						<input type="submit" style="height:35px;width:80px;font-size:18px;" value="<spring:message code="login"/>"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<input type="button" style="height:35px;width:80px;font-size:18px;" value="<spring:message code="reset"/>" onclick="set();"/>
					</td>
				</tr>
				<tr id="ess">
					<c:if test="${!empty msg }">
						<td style="color:red;font-size:15px;" colspan="2" align="center">${msg }</td>
					</c:if>
					
					<%
						String error = (String)session.getAttribute("error");
						if(error != null){
					%>
						<td style="color:red;font-size:15px;" colspan="2" align="center"><%=error %></td>
					<%  } %>
				</tr>
				<tr id="message" style="display:none;"><td id="show" style="color:red;font-size:15px;" colspan="2" align="center"></td></tr>
				</table>
				<br/>
			</form:form>
		</div>
	</div>
</body>
</html>