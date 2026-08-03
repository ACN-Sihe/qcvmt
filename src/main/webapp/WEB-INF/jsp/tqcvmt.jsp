<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ page import="java.util.HashMap"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=5">
<script src="<%=request.getContextPath()%>/js/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/vmt.js?v=4" type="text/javascript"></script>
<link href="<%=request.getContextPath()%>/css/box.css" rel="stylesheet" type="text/css">
<%
//String width=(String)request.getAttribute("width"); 
String inactive=(String)request.getAttribute("inactive");
String unable =(String)request.getAttribute("unable");
String empty  =(String)request.getAttribute("empty");
String discharge=(String)request.getAttribute("discharge");
String load  =(String)request.getAttribute("load");
String complexunit  =(String)request.getAttribute("complexunit");
%>
<style type="text/css">
	#tableList table td {
		font-size:18px;
		text-align:center;
		color:black;
		border:1px solid #dedede;
	}
	
	.inactive {
		background-color: <%=inactive%>;
	}
	
	.unable {
	    background-color:  <%=unable%>;
	}
	
	.empty {
	    background-color:  <%=empty%>;
	}
	
	.tierNum {
		background-color: white;
	}
	
	.discharge {
		background-color:<%=discharge%>;
	}
	
	.complexunit {
	    background-color:<%=complexunit%>;
	}
	
	.load {
	    background-color: <%=load%>;
	}

	.twenty {
		color: red !important;
		background-color: <%=inactive%>;
	}

	.refuel {
        color: red !important;
        background-color: red;
    }
	
	span.dgind {
		position: absolute;
		top: 1px;
		right: 1px;
		background: Yellow;
		color: Red;
		font-size: 35px;
		line-height: 15px;
		padding: 0 1px;
		padding-top: 5px;
		display: block;
	}
	
	span.infodgind {
		position: absolute;
		top: 1px;
		right: 1px;
		background: Yellow;
		color: Red;
		font-size: 20px;
		line-height: 6.5px;
		padding: 0 1px;
		padding-top: 5px;
		display: block;
	}

</style>
<script type="text/javascript">
var http_request2=false;
var greenImg = new Image();
var redImg = new Image();
greenImg.src = "<%=request.getContextPath()%>/images/green.gif";  
redImg.src = "<%=request.getContextPath()%>/images/red.gif";   

function sh(){
	if(window.confirm("<spring:message code="confirm_logout" />")){
		window.location.href="logout.html";
	}
}

function mykeydown(e){
	var e = window.event;
	if (e.keyCode == 106){
		sh();
	} 
}
</script>
</head>
<body onload="getResult();" style="margin:0;" onkeydown="mykeydown(event)">
	<div id="loading" style="position:absolute;left:0;top:0;color:Red;font-weight:bold;font-size:20px;">Loading</div>
	<div id="msg" style="display:none;position:absolute;left:0;top:130px;border:solid 1px Red;background-color:white;width:300px;height:300px;overflow:auto;"></div>

	<div id="headerInfo">
		<div title="LOGOUT" style="position:absolute;width:100%;cursor:pointer;" align="right" ><img src=" <%=request.getContextPath()%>/images/QClogout.gif" onclick="sh();"></img></div>
		<table style="background-color:blue;width:100%;height:30%;">
		<tr>
			<td class="d1_d"><span id="current_date"></span><span id="current_time"></span></td>
			<td id="tablebackground1"  class="d1_d"> <img src="<%=request.getContextPath()%>/images/green.gif"></td>
			<td class="d1_d"><span>MODERN TERMINALS</span>&nbsp;&nbsp;&nbsp;</td>
			<td class="d1_d" style="text-align:right;"><span>MTL</span>&nbsp;&nbsp;<span><c:out value="${facility}"></c:out></span></td>
		</tr>
		<tr>
			<td class="d1_head" style="text-align:left;"><span id="QC"></span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span id="bayNm"></span></td>
			<td class="d1_head" style="text-align:center;"><span id="QCAct"></span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
			<td class="d1_head" style="text-align:center;"><span id="rmain"></span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
			<td class="d1_head" style="text-align:center;"><span id="reful"></span></td>
			<td class="d1_head" style="text-align:right;"><span id="Vessl"></span></td>
		</tr>
		</table>
		<div id="tableList"></div>
	</div>
	
	<input type="hidden" id="qcNum" name="qcNum" value='<c:out value="${qcNum}"></c:out>'>
	
	<script type="text/javascript"> 
	function LoadingComplete() {
		document.getElementById("loading").style.cssText = "display:none";

		$(document).on('keydown',function(e) {
			var key = e.charCode || e.keyCode;
			
			if (key == 8 && !$(document.activeElement).is('input')) {
				e.preventDefault();
			}
		});
	}

	LoadingComplete();
	</script>
</body>
</html>