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
<title>vessel refuel status</title>
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
            var is_refuel = document.getElementById("is_refuel").value;

            //validate vesselid
            if(vesselid==""){
                document.getElementById("show").innerHTML = "Vessel Visit Id cannot be empty!";
                document.getElementById("message").style.display='';
                return false;
            }else{
                if(vesselid.length>30){
                    document.getElementById("show").innerHTML = "Vessel Visit Id is too long!";
                    document.getElementById("message").style.display='';
                    return false;
               }
            }
            //validate is_refuel
            if(is_refuel ==""){
                document.getElementById("show").innerHTML = "Is Refuel cannot be empty!";
                document.getElementById("message").style.display='';
                return false;
            }else{
                if(is_refuel.length>3){
                    document.getElementById("show").innerHTML = "Is Refuel is too long!";
                    document.getElementById("message").style.display='';
                    return false;
                }else if((is_refuel !="Yes")&&(is_refuel !="No")){
                        document.getElementById("show").innerHTML = "Is Refuel should be 'Yes' or 'No'!";
                        document.getElementById("message").style.display='';
                        return false;
                }
            }

            return true;
        }

        function show(){
           document.getElementById("ess").style.display="none";
           document.getElementById("message").style.display="none";
        }

		function sh() {
			if (window.confirm("<spring:message code="confirm_logout" />")) {
				window.location.href = "logout.html";
			}
		}

		function back() {
			window.location.href = "allVesselRefuel.html";
		}
	</script>
</head>
<body>
<c:url var="actionUrl" value="updateVesselRefuelStatus.html" />
	<div id="d1">
		<div title="LOGOUT" style="position: absolute; width: 100%; cursor: pointer;right:134px;" align="right"><img src=" <%=request.getContextPath()%>/images/logout.jpg" onclick="sh();"></img></div>
		<div id="d1_head">MODERN TERMINALS</div>
		<div id="d1_body">
			<br/>
			<c:choose>
                <c:when test="${!empty vesselRefuel}">
                    <form:form modelAttribute="vesselRefuel" method="POST" action="${actionUrl}" onsubmit="return checkValue();">
                        <table cellpadding="0" cellspacing="0" style="text-align: center;" width="100%" >
                            <tr height="50">
                                <td align="center" width="60%">
                                    Vessel Id:
                                    <input type="text" id="vesselid" name="vesselid" title="vesselid" maxlength="30" value='<c:out value="${vesselRefuel.vesselid }"></c:out>' />
                                </td>

                                <td align="left">
                                    <spring:message code="is" /><spring:message code="refuel" /> :
                                    <select id="is_refuel" name="is_refuel" title="is_refuel" >
                                        <c:choose>
                                           <c:when test="${vesselRefuel.is_refuel =='Yes' }">
                                               <option value="Yes" selected="selected" >Yes</option>
                                               <option value="No">No</option>
                                           </c:when>
                                           <c:otherwise>
                                                <option value="Yes">Yes</option>
                                                <option value="No" selected="selected">No</option>
                                           </c:otherwise>
                                        </c:choose>
                                    </select>
                                </td>
                            </tr>

                            <tr>
                                <td><input type="hidden" id="id" name="id" value='<c:out value="${vesselRefuel.id }"></c:out>'/></td>
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
                            <tr id="message" style="display:none;"><td id="show" style="color:red;font-size:15px;" colspan="3" align="center"></td></tr>
                        </table>
                    </form:form>
			    </c:when>
                <c:otherwise>
                    <form:form modelAttribute="vesselRefuel" method="POST" action="${actionUrl}" onsubmit="return checkValue();">
                        <table cellpadding="0" cellspacing="0" style="text-align: center;" width="100%" >
                            <tr height="50">
                                <td align="center" width="60%">
                                    Vessel Id:
                                    <input type="text" id="vesselid" name="vesselid" title="vesselid" maxlength="30" />
                                </td>

                                <td align="left">
                                    <spring:message code="is" /><spring:message code="refuel" /> :
                                    <select id="is_refuel" name="is_refuel" title="is_refuel" >
                                        <option value="Yes">Yes</option>
                                        <option value="No">No</option>
                                    </select>
                                </td>
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
                            <tr id="message" style="display:none;"><td id="show" style="color:red;font-size:15px;" colspan="3" align="center"></td></tr>
                        </table>
                    </form:form>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

</body>
</html>