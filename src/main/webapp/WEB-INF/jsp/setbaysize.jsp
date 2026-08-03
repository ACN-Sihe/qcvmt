<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>set by size</title>
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
               }
           function sh(){
        	   if(window.confirm("<spring:message code="confirm_logout" />")){
             	  window.location.href="logout.html";
                }
           }
           function checkv(){
               var reg = new RegExp("^[0-9]*$");
             var obj = document.getElementById("deckRows");
             if(obj.value==""){
            	 alert("The Rows of DECK can't be empty!");
                 return false;
                 }
            if(!reg.test(obj.value)){
                alert("The Rows of DECK must be a nubmer!");
                return false;
            }else{
                if(obj.value>100){
                	alert("The Rows of DECK must not more than 100!");
                	return false;
            }else if(obj.value<5){
            	alert("The Rows of DECK must not less than 5!");
            	return false;
            }
            }
            obj.value=formatNum(obj.value);
                obj = document.getElementById("deckTiers");
                if(obj.value==""){
               	 alert("The Tiers of DECK can't be empty!");
                    return false;
                    }
                if(!reg.test(obj.value)){
                    alert("The Tiers of DECK must be a nubmer!");
                    return false;
                }else{
                    if(obj.value>100){
                    	alert("The Tiers of DECK must not more than 100!");
                    	return false;
                }else if(obj.value<5){
                	alert("The Tiers of DECK must not less than 5!");
                	return false;
                }
                }
                obj.value=formatNum(obj.value);
                    obj = document.getElementById("holdRows");
                    if(obj.value==""){
                      	 alert("The Rows of HOLD can't be empty!");
                           return false;
                           }
                    if(!reg.test(obj.value)){
                        alert("The Rows of HOLD must be a nubmer!");
                        return false;
                    }else{
                        if(obj.value>100){
                        	alert("The Rows of HOLD must not more than 100!");
                        	return false;
                    } else if(obj.value<5){
                    	alert("The Rows of HOLD must not less than 5!");
                    	return false;

                        }
                    }     
                    obj.value=formatNum(obj.value);
                        obj = document.getElementById("holdTiers");
                        if(obj.value==""){
                         	 alert("The Tiers of HOLD can't be empty!");
                              return false;
                              }
                        if(!reg.test(obj.value)){
                            alert("The Tiers of HOLD must be a nubmer!");
                            return false;
                        }else{
                            if(obj.value>100){
                            	alert("The Tiers of HOLD must not more than 100!");
                            	return false;
                        }else if(obj.value<5){
                        	alert("The Tiers of HOLD must not less than 5!");
                        	return false;
                        }    
                        }
                        obj.value=formatNum(obj.value);
                        return true;        
           }
           function back(){
             	  window.location.href="all.html";  
           }
           function formatNum(value){
        	   var rvalue;
               if(value<10){
                   var rvalue="0"+value;
               }else{
                   rvalue=value;
               }
               return rvalue;
           }
     </script>
</head>
<body>
<c:url var="updateBay" value="/user/updateBay.html" />

     <div id="d1">
            <div title="LOGOUT" style="position:absolute;width:100%;cursor:pointer;right:134px;" align="right" ><img src=" <%=request.getContextPath()%>/images/logout.jpg" onclick="sh();"></img></div>
			<div id="d1_head">MODERN TERMINALS</div>
			<div id="d1_body">
			<form:form modelAttribute="baySize" method="POST" action="${updateBay}" onsubmit="return checkv();" >
					<table cellpadding="0" cellspacing="5" style="text-align:center;" width="100%" >
						<tr>
							<td align='right'><spring:message code="deck" /> :</td><td><spring:message code="rows" /><input type="text"  id="deckRows" name="deckRows" value='<c:out value="${baySize.deckRows }"></c:out>'/></td><td><spring:message code="tiers" /><input type="text"  id="deckTiers" name="deckTiers" value='<c:out value="${baySize.deckTiers}"></c:out>'/></td>
						</tr>
						<tr>
						    <td align='right'><spring:message code="hold" /> :</td><td><spring:message code="rows" /><input type="text"  id="holdRows" name="holdRows" value='<c:out value="${baySize.holdRows }"></c:out>'/></td><td><spring:message code="tiers" /><input type="text"  id="holdTiers" name="holdTiers" value='<c:out value="${baySize.holdTiers}"></c:out>'/></td>
					   </tr>
						<tr align='center'>
						   <td colspan="3" align='center'>
								<input type="submit" value="<spring:message code="ok" />" id="ok" name="ok" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<input type="button" value="<spring:message code="cancel" />" id="cancel" name="cancel" onclick="back();"/>
							</td>
						</tr>
						<tr></tr><tr></tr><tr></tr>
						<tr id="ess">
					      <c:if test="${!empty baymsg }">
					          <td style="color:red;font-size:15px;" colspan="2" align="center">${result }</td>
					      </c:if>
					  </tr>
					</table>
				</form:form>
			</div>
		</div>
<!--		width: expression((documentElement.clientWidth > 450) ? "450px" : "100%")!important; -->
<!--				width:100%; -->
<!--				min-width:450px;-->
</body>
</html>