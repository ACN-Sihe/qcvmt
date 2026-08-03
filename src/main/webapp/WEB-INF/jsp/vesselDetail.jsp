<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<script src="<%=request.getContextPath()%>/js/vmt.js?v=3" type="text/javascript"></script>
<title>vessel detail</title>
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
         	
     		var vesselid = document.getElementById("vesselid").value;
     		var deck_hold = document.getElementById("deck_hold").value;
     		var bay = document.getElementById("bay").value;
     		var tierStart = document.getElementById("tierStart").value;
     		var tierEnd = document.getElementById("tierEnd").value;
     		var rowStart = document.getElementById("rowStart").value;
     		var rowEnd = document.getElementById("rowEnd").value;
     			//validate vesselid
     			if(vesselid==""){
     			document.getElementById("show").innerHTML = "The vesselid cannot be empty!";
          	    document.getElementById("message").style.display='';  			
     			return false;
     			}else{
				  if(vesselid.length>30){
						document.getElementById("show").innerHTML = "The vesselid is too long!";
				  	    document.getElementById("message").style.display=''; 
				  	    return false;
	               }
         		}
     			//validate deck_hold
     			if(deck_hold ==""){
     			document.getElementById("show").innerHTML = "The deck_hold cannot be empty!";
          	    document.getElementById("message").style.display='';  
          	    return false;	
         		}else{
  				  	if(deck_hold.length>1){
						document.getElementById("show").innerHTML = "The deck_hold is too long!";
				  	    document.getElementById("message").style.display=''; 
				  	    return false;
  				  	}else if((deck_hold !="A")&&(deck_hold !="B")){
							document.getElementById("show").innerHTML = "The deck_hold should be 'A' or 'B' !";
					  	    document.getElementById("message").style.display=''; 
					  	    return false;
		              }
             	}
     			//validate bay
     			if(bay ==""){
     			document.getElementById("show").innerHTML = "The bay cannot be empty!";
          	    document.getElementById("message").style.display='';  
          	    return false;	
         		}else{
  				  	if(bay.length>10){
						document.getElementById("show").innerHTML = "The bay is too long!";
				  	    document.getElementById("message").style.display=''; 
				  	    return false;
	             	  }else if(!checkNumber(bay)){
  							document.getElementById("show").innerHTML = "The bay should be number !";
  					  	    document.getElementById("message").style.display=''; 
  					  	    return false;
  		              }
             	}

     			//validate rowStart
    			if(tierEnd ==""){
       			    document.getElementById("show").innerHTML = "The rowStart cannot be empty!";
            	    document.getElementById("message").style.display='';  
            	    return false;	
           		}else{
    				  	if(rowStart.length>2){
  						document.getElementById("show").innerHTML = "The rowStart is too long!";
  				  	    document.getElementById("message").style.display=''; 
  				  	    return false;
  	             	  }else if(!checkNumber(rowStart)){
  							document.getElementById("show").innerHTML = "The rowStart should be number !";
  					  	    document.getElementById("message").style.display=''; 
  					  	    return false;
  		              }
               	}

    			//validate rowEnd
    			if(rowEnd ==""){
       			    document.getElementById("show").innerHTML = "The rowEnd cannot be empty!";
            	    document.getElementById("message").style.display='';  
            	    return false;	
           		}else{
    				  	if(rowEnd.length>3){
  						document.getElementById("show").innerHTML = "The rowEnd is too long!";
  				  	    document.getElementById("message").style.display=''; 
  				  	    return false;
  	             	  }else if(!checkNumber(rowEnd)){
  							document.getElementById("show").innerHTML = "The rowEnd should be number !";
  					  	    document.getElementById("message").style.display=''; 
  					  	    return false;
  		              }
               	}
               	
				//validate tierStart
    			if(tierStart ==""){
       			    document.getElementById("show").innerHTML = "The tierStart cannot be empty!";
            	    document.getElementById("message").style.display='';  
            	    return false;	
           		}else{
    				  	if(tierStart.length>2){
  						document.getElementById("show").innerHTML = "The tierStart is too long!";
  				  	    document.getElementById("message").style.display=''; 
  				  	    return false;
  	             	  }else if(!checkNumber(tierStart)){
  							document.getElementById("show").innerHTML = "The tierStart should be number !";
  					  	    document.getElementById("message").style.display=''; 
  					  	    return false;
  		              }
               	}

    			//validate tierEnd
    			if(tierEnd ==""){
       			    document.getElementById("show").innerHTML = "The tierEnd cannot be empty!";
            	    document.getElementById("message").style.display='';  
            	    return false;	
           		}else{
    				  	if(tierEnd.length>3){
  						document.getElementById("show").innerHTML = "The tierEnd is too long!";
  				  	    document.getElementById("message").style.display=''; 
  				  	    return false;
  	             	  }else if(!checkNumber(tierEnd)){
  							document.getElementById("show").innerHTML = "The tierEnd should be number !";
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
			window.location.href = "allVessel.html";
		}

        function show(){
     	   document.getElementById("ess").style.display="none";
     	   document.getElementById("message").style.display="none";
        }
        
	</script>
</head>
<body>
<c:url var="actionUrl" value="saveVessel.html" />
	<div id="d1">
		<div title="LOGOUT" style="position: absolute; width: 100%; cursor: pointer;right:134px;" align="right"><img src=" <%=request.getContextPath()%>/images/logout.jpg" onclick="sh();"></img></div>
		<div id="d1_head">MODERN TERMINALS</div>
		<div id="d1_body">
			<br/>
			<form:form modelAttribute="vessel" method="POST" action="${actionUrl}" onsubmit="return checkValue();">
				<table cellpadding="0" cellspacing="0" style="text-align: center;" width="100%" >
					<tr>
						<td align="right">Vessel <spring:message code="name" />:</td>
						<td align="center"><input type="text" id="vesselid" name="vesselid" title="vesselid" maxlength="30" onfocus="show();"/></td>
					</tr>
					<tr>
						<td align="right"><spring:message code="deck" />/<spring:message code="hold" /> :</td>
						<td align="center">	
							<select id="deck_hold" name="deck_hold" title="deck_hold" >																	
 									<option value="A">A</option>
									<option value="B">B</option>																	
							</select>
						</td>							
					</tr>
					<tr>
						<td align="right"><spring:message code="BAY" /> :</td>
						<td align="center"><input type="text" id="bay" name="bay" title="bay" maxlength="3" /></td>
					</tr>
					<tr>
						<td align="right"><spring:message code="rows" /> :</td>
						<td align="center"><input type="text" id="rowStart" name="rowStart" title="rowStart" maxlength="2" /></td>
						<td align="left"><input type="text" id="rowEnd" name="rowEnd" title="rowEnd" maxlength="3" /></td>
					</tr>
					<tr>
						<td align="right"><spring:message code="tiers" /> :</td>
						<td align="center"><input type="text" id="tierStart" name="tierStart" title="TierStart" maxlength="2" /></td>
						<td align="left"><input type="text" id="tierEnd" name="tierEnd" title="TierEnd" maxlength="2" /></td>
					</tr>
					<tr height="50">
						<td colspan="3" align='center'>
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
	
</body>
</html>