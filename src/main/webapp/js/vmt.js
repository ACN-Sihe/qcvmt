var firstLoad = true;
var firstLoadServerDateTime = null;
var firstLocalDateTime = null;
var dateTime;
var http_request=false;
var is_processing = false;
var debugMode = false;
/*Begin FIR-TMT-000005*/
var lastTimeOut = false;
var lastInterval = 0;
var thisInterval = 15000;
var firstLevelInterval  = 15000;
var secondLevelInterval = 20000;
var secondLevelTimeoutInterval = 20000;
var thirdLevelInterval  = 25000;
var thirdLevelTimeoutInterval = 25000;
var fourthLevelInterval = 30000;
var intervalID;
var refreshMode=3;
var mode1Interval = 10000;
var mode2Interval1 = 20000;
var mode2Interval2 = 10000;
var mode2StartTime = 0;
var mode2EndTime = 14;
var timeOutTimes = 0;
var allowedTimeOutTimes = 3;
/*End FIR-TMT-000005*/

//show time sent from server and convert yyyy-MM-dd HH:mm:ss string format to Date format
function setTime(dateTimeNowStr){
	var strArray = dateTimeNowStr.split(" ");  
	var strDate = strArray[0].split("-"); 
	var strTime = strArray[1].split(":"); 
	
	var year = parseInt(strDate[0],10);
	var month = parseInt(strDate[1],10)-1;
	var day = parseInt(strDate[2],10);
	var hour = parseInt(strTime[0],10);
	var min = parseInt(strTime[1],10);
	var sec = parseInt(strTime[2],10);
	
	serverDateTime = new Date();
	serverDateTime.setFullYear(year, month, day);
	serverDateTime.setHours(hour);
	serverDateTime.setMinutes(min);
	serverDateTime.setSeconds(sec);
	
	showTime();
}

function getResult(){
	var qcvalue=document.getElementById("qcNum").value;
	document.getElementById("QC").innerHTML=qcvalue;
	getData();
	
	//window.setInterval("getData()",10000);
	window.setInterval("showTime()",1000);
}

function getData(){
	if (is_processing) {
		return;
	}
	
	var url = getUrl();
	
	try {
		is_processing = true;
		
		// set default color of signal indicator
		//ChangeSignalIndicator("RED");
		
		$.ajax({
			url: url,
			cache: false,
			type: "get",
			dataType: "text",
			timeout: 9000,
			success: function(data, textStatus, xhr){
				try {
					if ((xhr.responseText.toLowerCase().indexOf("<title>login") > -1)
							|| (xhr.responseText.indexOf(" name=\"err_msg\" value=\"error_webpage_expired\"") > -1)) {
						var currentUrl = window.location.href;
						var loginPath = currentUrl.substr(0,currentUrl.indexOf("/user/") + 6) + "index.html";
						window.location = loginPath;
						return;
					}
					callBack(data, textStatus, xhr);
				} catch (e) {
					ChangeSignalIndicator("RED");
					PrintMsg(xhr.responseText);
				}
				is_processing = false;
				lastTimeOut = false ; //FIR-TMT-000005
				timeOutTimes = 0 ;
			},
			error: function(xhr, textStatus, errorThrown) {
				var msg = "readyState: " + xhr.readyState + "<br/>status: " + xhr.status + "<br/>errorThrown: " + errorThrown + "<br/>textStatus: " + textStatus + "<br/>responseText: " + xhr.responseText;
				PrintMsg(msg);
				
				if ((xhr.readyState == 0 && xhr.status == 0) 
						|| (xhr.readyState == 4 && 
							  (xhr.status == 12029 
							|| xhr.status == 12002
							|| xhr.status == 12012
							|| xhr.status == 12030
							|| xhr.status == 12031
							|| xhr.status == 12151
							|| xhr.status == 12152
							|| xhr.status == 12007)
						)
					) {
					// WIFI signal problem, show RED.
					ChangeSignalIndicator("RED");
				} else {
					// other problem, can change it to show YELLOW in the future enhancement, 
					// currently, keep using RED as the YELLOW logic not yet confirmed with user. 
					ChangeSignalIndicator("RED");
				}
				
				is_processing = false;
				lastTimeOut = true; //FIR-TMT-000005
				timeOutTimes++;
			}
		});
	} catch (e) {
		ChangeSignalIndicator("RED");
		is_processing = false;
		lastTimeOut = true; //FIR-TMT-000005
		timeOutTimes++;
	}finally {
		/*Begin FIR-TMT-000005*/
		if (refreshMode == 1){
			thisInterval = mode1Interval;
		}else if(refreshMode == 2){
			var currentTime = new Date();
			var currentHour = parseInt(currentTime.format("hh"));
			if (currentHour >= mode2StartTime && currentHour< mode2EndTime){
				thisInterval = mode2Interval1;
			}else{
				thisInterval = mode2Interval2;
			}
			
		}else if(refreshMode == 3){
			if (lastTimeOut) {
				if (timeOutTimes == 1){
					thisInterval = secondLevelTimeoutInterval;
				}else if(timeOutTimes == 2){
					thisInterval = thirdLevelTimeoutInterval;
				}else {
					thisInterval = fourthLevelInterval ;
				}
					
			}else{
				if(lastInterval == fourthLevelInterval){
					thisInterval = thirdLevelInterval;
				}else if(lastInterval == thirdLevelInterval || lastInterval == thirdLevelTimeoutInterval){
					thisInterval = secondLevelInterval;
				}else if(lastInterval == secondLevelInterval || lastInterval == secondLevelTimeoutInterval ){
					thisInterval = firstLevelInterval;
				}else{
					thisInterval = firstLevelInterval ;
				}
			}
		}
		if (lastInterval != thisInterval){
			lastInterval = thisInterval;
			window.clearInterval(intervalID);
			intervalID = window.setInterval("getData()",thisInterval);
		}
		
	}
}

function ChangeSignalIndicator(signal) {
	var signalIndicator = $("#tablebackground1");
	if (signal.toUpperCase() == "GREEN") {
		signalIndicator.html("<img src="+greenImg.src+">");
	} else if (signal.toUpperCase() == "RED") {
		signalIndicator.html("<img src="+redImg.src+">");
	}
}

function PrintMsg(msg) {
	if (debugMode) {
		$("#msg").css("display", "inline");
		$("#msg").html(msg);
	}
}

function showTime(){
	if (firstLoadServerDateTime == null || typeof(firstLoadServerDateTime) == "undefined") {
		return;
	}
	
	var currentTime = calculateCurrentTime();
	$("#current_date").html(currentTime.format("yyyy-MM-dd") + " ");
	$("#current_time").html(currentTime.format("hh:mm:ss"));
}

function calculateCurrentTime() {
	var sDateTime = new Date(firstLoadServerDateTime.getTime());
	var currentLocalDateTime = new Date();
	var elapsedSeconds = parseInt(Math.abs((currentLocalDateTime.getTime() - firstLocalDateTime.getTime()) / 1000)) + 1;
	sDateTime.setSeconds(sDateTime.getSeconds() + elapsedSeconds);
	
	return sDateTime;
}

function callBack(data, textStatus, xhr){
	var currentdate = new Date(); 
	var datetime = "Last Sync: " + currentdate.getDate() + "/"
	    + (currentdate.getMonth()+1)  + "/" 
	    + currentdate.getFullYear() + " @ "  
	    + currentdate.getHours() + ":"  
	    + currentdate.getMinutes() + ":" 
	    + currentdate.getSeconds();

	var msg = ("readyState: " + xhr.readyState + "<br>status: " + xhr.status + "<br>" + datetime);
	PrintMsg(msg);
	
	if (firstLoad) {	// page refreshed
		if (firstLocalDateTime == null) {
			firstLocalDateTime = new Date();
		}
		
		var dateTimeNowStr_server = getTimeContent(xhr,"dateTimeNow");
		if(dateTimeNowStr_server != "" && dateTimeNowStr_server != null){
			firstLoadServerDateTime = convertStrToDate(dateTimeNowStr_server);
		}
		
		firstLoad = false;
	}
	
	var headInfo = getBaseContent(xhr,"hInfo");
	msg += "<br/>responseText: <xmp>" + xhr.responseText + "</xmp>";
	PrintMsg(msg);

	var InfoList = headInfo.split(";");
	for (var i = 0; i < InfoList.length; i++) {
		var name = InfoList[i];
		var value = getBaseContent(xhr, name);
		if(value != "" && value != null){
			setContent(name, value);
		}
	}

	var isRefuel = getBaseContent(xhr,"isRef");
	if (isRefuel == 'Yes') {
	    var reful = document.getElementById("reful");
	    reful.style = "color: red !important;";
	}else {
	    var reful = document.getElementById("reful");
	    reful.style = "";
	}

	var tableInfo = getListContent(xhr, "table_info");
	setContent("tableList", tableInfo);
	
	ChangeSignalIndicator("GREEN");
}

function getUrl(){
	var qcNum=document.getElementById("qcNum").value;
	var url="BusiQuery.html?qcNum="+qcNum;
	return url;
}

function getTimeContent(http_request,text){
    var conttext=http_request.responseText;
    var begin = "<"+text+">";
 	var end = "</"+text+">";
 	var content=conttext.substring(conttext.indexOf(begin)+text.length+2,conttext.indexOf(end));
// 	alert("getTimeContent:"+content);
 	return content;
}

function convertStrToDate(dateStr) {
	var strArray = dateStr.split(" ");  
	var strDate = strArray[0].split("-"); 
	var strTime = strArray[1].split(":"); 
	
	var year = parseInt(strDate[0],10);
	var month = parseInt(strDate[1],10)-1;
	var day = parseInt(strDate[2],10);
	var hour = parseInt(strTime[0],10);
	var min = parseInt(strTime[1],10);
	var sec = parseInt(strTime[2],10);
	
	var d = new Date();
	d.setFullYear(year, month, day);
	d.setHours(hour);
	d.setMinutes(min);
	d.setSeconds(sec);
	
	return d;
}

function getBaseContent(http_request,text){  
    var conttext=http_request.responseText;
    var begin = "<"+text+">";
 	var end = "</"+text+">";
 	var content=conttext.substring(conttext.indexOf(begin)+7,conttext.indexOf(end));
 	//var contextList = http_request.responseXML.getElementsByTagName(text);
 	//var content = contextList[0].firstChild.nodeValue;
 	return content;
 }
function getListContent(http_request,text){
 	var noticeText =http_request.responseText;
 	var begin = "<"+text+">";
 	var end = "</"+text+">";
    var mortInfo = noticeText.substring(noticeText.indexOf(begin)+12,noticeText.indexOf(end));
    return mortInfo;
 }

function setContent(param,value){
	//var praramList = document.getElementsByName(param);
	/*
	for( var i=0;i<praramList.length;i++){
		praramList[i].innerHTML=value;
	}
	*/
	var pnl = document.getElementById(param);
	if (pnl != null) {
		pnl.innerHTML = value;
	}
}

Date.prototype.format = function(format){ 
	 var o = { 
	 "M+" : this.getMonth()+1, //month 
	 "d+" : this.getDate(), //day 
	 "h+" : this.getHours(), //hour 
	 "m+" : this.getMinutes(), //minute 
	 "s+" : this.getSeconds(), //second 
	 "q+" : Math.floor((this.getMonth()+3)/3), //quarter 
	 "S" : this.getMilliseconds() //millisecond 
	 }; 
	 if(/(y+)/.test(format)) { 
	 format = format.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); 
	 } 

	 for(var k in o) { 
	 if(new RegExp("("+ k +")").test(format)) { 
	 format = format.replace(RegExp.$1, RegExp.$1.length==1 ? o[k] : ("00"+ o[k]).substr((""+ o[k]).length)); 
	 } 
	 } 
	 return format; 
	 };
	 
function checkNumber(str){                 
   if(str==null||str==""){   
        return  false;   
   }else if(str.length==0){   
				return  false;   
   }else{   
         for(i=0;i<str.length;i++){   
           if(str.charAt(i)<'0'||str.charAt(i)>'9'){   
              return   false;   
              break;   
            }   
         }   
    }   
     
   return true;   
}    
