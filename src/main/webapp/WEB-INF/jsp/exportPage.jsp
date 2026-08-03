<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>export page</title>
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

     
     function exportLogs(){
			var fromTime = document.getElementById("fromTime").value;
			var toTime = document.getElementById("toTime").value;
		//	var exportUserID = document.getElementById("exportUserID").value;
			if(fromTime==null || fromTime=="" || fromTime.length>19 || fromTime.length<19){
				alert("Please enter the start Time with correct format 'yyyy-mm-dd hh:mi:ss'.");
				return false;
			}
			if(toTime==null || toTime=="" || toTime.length>19 || toTime.length<19){
				alert("Please enter the End Time with correct format 'yyyy-mm-dd hh:mi:ss'.");
				return false;
			}

			if(!strDateTime(fromTime)){
				alert("Invail start time.");
				return false;
			}

			if(!strDateTime(toTime)){
				alert("Invail end time.");
				return false;
			}				
			if(comptime(fromTime,toTime)== -1){
				alert("The end time must be larger than the start time.");
				return false;
			}
			
			window.location.href="exportLogs.html?fromTime="+fromTime+"&toTime="+toTime;
	}

	function comptime(beginTime,endTime){
		var beginTimes=beginTime.substring(0,10).split('-');
		var endTimes=endTime.substring(0,10).split('-');
		beginTime=beginTimes[1]+'-'+beginTimes[2]+'-'+beginTimes[0]+' '+beginTime.substring(10,19);
		endTime=endTimes[1]+'-'+endTimes[2]+'-'+endTimes[0]+' '+endTime.substring(10,19);
		// alert(beginTime+endTime+beginTime);
		// alert(Date.parse(endTime));alert(Date.parse(beginTime)); 
		var a =(Date.parse(endTime)-Date.parse(beginTime))/3600/1000;
		if(a<0){
			return -1;
		}else if (a>0){
			return 1;
		}else if (a==0){
			return 0;
		}else{
			return 'exception';
		}
	} 

	function strDateTime(str) 
	{ 
		var reg = /^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2}) (\d{1,2}):(\d{1,2}):(\d{1,2})$/; 
		var r = str.match(reg); 
		if(r==null){
			return false; 
		}
		var d= new Date(r[1], r[3]-1,r[4],r[5],r[6],r[7]); 
		return (d.getFullYear()==r[1]&&(d.getMonth()+1)==r[3]&&d.getDate()==r[4]&&d.getHours()==r[5]&&d.getMinutes()==r[6]&&d.getSeconds()==r[7]); 
	} 

	function initTime(){
		var date = new Date();
	//	alert(date.getFullYear());
	//	alert(date.getMonth()+1);
	//	alert(date.getDate());
	//	alert(date.getHours());
	//	alert(date.getMinutes());
	//	alert(date.getSeconds());
		var timeString = "";
		var startTimeString = "";
		timeString = timeString+date.getFullYear()+"-";
		if((date.getMonth()+1)>=10){
			timeString = timeString+(date.getMonth()+1)+"-";
		}else{
			timeString = timeString+"0"+(date.getMonth()+1)+"-";
		}
		if(date.getDate()>=10){
			timeString = timeString+date.getDate()+" ";
			startTimeString = timeString;
		}else{
			timeString = timeString+"0"+date.getDate()+" ";
			startTimeString = timeString;
		}
		if(date.getHours()>=10){
			timeString = timeString+date.getHours()+":";
		}else{
			timeString = timeString+"0"+date.getHours()+":";
		}
		if(date.getMinutes()>=10){
			timeString = timeString+date.getMinutes()+":";
		}else{
			timeString = timeString+"0"+date.getMinutes()+":";
		}
		if(date.getSeconds()>=10){
			timeString = timeString+date.getSeconds();
		}else{
			timeString = timeString+"0"+date.getSeconds();
		}
		//alert(timeString);
		startTimeString = startTimeString+"00:00:00";
		document.getElementById("fromTime").value=startTimeString;
		document.getElementById("toTime").value=timeString;
	}


	function bback(){
		
   	 	
     	  window.location.href="all.html";  
     
	}
     </script>
</head>
<body onload="initTime();">
<c:url var="saveUser" value="save.html" />

     <div id="d1">
            <div title="LOGOUT" style="position:absolute;width:100%;cursor:pointer;right:134px;" align="right" ><img src=" <%=request.getContextPath()%>/images/logout.jpg" onclick="sh();"></img></div>
			<div id="d1_head">MODERN TERMINALS</div>
			<div id="d1_body">
			<form:form modelAttribute="user" method="POST" action="${saveUser}" onsubmit="return check();">
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
					     		<input type="text" id="fromTime" name="fromTime" value="" />
					     		&nbsp;To&nbsp;
					     		<input type="text" id="toTime" name="toTime" value="" />
					     		<input type="button" id="exportButton" name="exportButton" value="Export" onclick="exportLogs();" />
					     	</td>
						</tr>
						<tr>
							<td colspan="4">
								&nbsp;
							</td>
						</tr>
						<tr>
							<td colspan="4" align="center">
								<input type="button" value="<spring:message code="back" />" id="back" name="back" onclick="bback();" />
							</td>
						</tr>
					</table>
				</form:form>
			</div>
		</div>
</body>
</html>