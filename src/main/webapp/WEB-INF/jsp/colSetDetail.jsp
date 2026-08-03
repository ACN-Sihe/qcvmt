<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<link href="<%=request.getContextPath()%>/css/colorPickerStyle.css" rel="stylesheet" type="text/css" media="all"/>
<script src="<%=request.getContextPath()%>/js/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/jquery.soColorPicker-1.0.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/jquery.bgiframe-2.1.2.js" type="text/javascript"></script>

<title>colset detail</title>
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
     	function checkValue(){
      	    document.getElementById("ess").style.display="none";
     	    document.getElementById("message").style.display="none";
         	
     		var boxcase = document.getElementById("boxcase").value;
     		var color = document.getElementById("color").value;

     		if(boxcase==""){
     			document.getElementById("show").innerHTML = "The boxcase cannot be empty!";
          	    document.getElementById("message").style.display='';  			
     			return false;
     		}else{
				  if(boxcase.length>20){
						document.getElementById("show").innerHTML = "The boxcase is too long!";
				  	    document.getElementById("message").style.display=''; 
				  	    return false;
	               }
         		}
     		if(color ==""){
     			document.getElementById("show").innerHTML = "The color cannot be empty!";
          	    document.getElementById("message").style.display='';  
          	    return false;	
         		}else{
  				  	if(color.length>12){
						document.getElementById("show").innerHTML = "The color is too long!";
				  	    document.getElementById("message").style.display=''; 
				  	    return false;
	             	  }
             	}
     		return true;
     	}
     	 	
		function sh() {
			if (window.confirm("<spring:message code="confirm_logout" />")) {
				window.location.href = "logout.html";
			}
		}
		
		function back() {
			window.location.href = "allColSet.html";
		}

        function show(){
     	   document.getElementById("ess").style.display="none";
     	   document.getElementById("message").style.display="none";
        }
        
	</script>
</head>
<body>
<c:url var="actionUrl" value="saveColSet.html" />
	<div id="d1">
		<div title="LOGOUT" style="position: absolute; width: 100%; cursor: pointer;right:134px;" align="right"><img src=" <%=request.getContextPath()%>/images/logout.jpg" onclick="sh();"></img></div>
		<div id="d1_head">MODERN TERMINALS</div>
		<div id="d1_body">
			<form:form modelAttribute="colSet" method="POST" action="${actionUrl}" onsubmit="return checkValue();">
				<table cellpadding="0" cellspacing="5" style="text-align: center;" width="100%" >
					<tr>
						<td align="right"><spring:message code="box_case" /> :</td>
						<td align="center"><input type="text" id="boxcase" name="boxcase" title="BOXCASE" onfocus="show();"/></td>
					</tr>
					<tr>
						<td align="right"><spring:message code="color" /> :</td>
						<td align="center">
						    <input type="text" readonly="readonly" id="colors" style="cursor:pointer;" title="COLOR" onfocus="show();"/>
						    <input type="text" id="color" style="display:none;" name="color" />
						</td>
					</tr>
					<tr height="50">
						<td colspan="2" align='center'>
							<input type="submit" value="<spring:message code="ok" />" id="ok" name="ok" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
							<input type="button" value="<spring:message code="cancel" />" id="cancel" name="cancel" onclick="back();" />
						</td>
					</tr>
					<tr id="ess">
						<c:if test="${!empty result }">
							<td style="color: red; font-size: 15px;" colspan="2" align="center">${result}</td>
						</c:if>
					</tr>
					<tr id="message" style="display:none;"><td id="show" style="color:red;font-size:15px;" colspan="2" align="center"></td></tr>
				</table>
			</form:form>
        </div>
    </div>
	<script type="text/javascript">
			
			jQuery('#colors').soColorPacker({
				size:2,
				textChange:false, 
				colorChange:2,
				callback:function (c) {
					process(c.color);
				}
			});
			
			function process(colorSelected){
				document.getElementById("color").value=colorSelected;
			}
    </script>
</body>
</html>