<%@ page import="com.liquid.bean" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="app.liquidx.deploy.deployManager" %>
<%@ page import="com.liquid.ThreadSession" %>
<%@ page import="static com.liquid.ThreadSession.removeThreadSessionInfo" %>
<%@ page import="com.liquid.utility" %>
<%@ page import="com.liquid.Callback" %>
<%!

%>
<%@include file="../connection_setup.jsp"%>
<%
    String openScript = "";
    String deployFile = "";
    String deployName = request.getParameter("deployName");
    String deployBackup = request.getParameter("deployBackup");
    if("S".equalsIgnoreCase(deployBackup)
            || "ON".equalsIgnoreCase(deployBackup)
            || "Y".equalsIgnoreCase(deployBackup)
            || "1".equalsIgnoreCase(deployBackup)
            || "TRUE".equalsIgnoreCase(deployBackup)
            || "T".equalsIgnoreCase(deployBackup)
    ) {
        deployBackup = "checked";
    } else {
        deployBackup = "";
    }


    if(deployName != null && !deployName.isEmpty()) {

        try {

            Object cfgId = bean.load_bean(request, "select id from liquidx.\"deploysCfg\" where name=?", new Object [] { deployName });

            if(cfgId != null) {

                Object deplpoyBean = bean.load_bean((HttpServletRequest) request, "LiquidX.liquidx.deploysCfg", "*", "id=" + com.liquid.utility.getString(cfgId, "id"));
                if (deplpoyBean != null) {

                    // Registra la sessione
                    ThreadSession.saveThreadSessionInfoEx ( "Liquid", request, response, out, null, null, "DIRECT" );


                    // N.B.: Il backup viene fatto solo se il fila da copiare è recente
                    boolean doBackup = true;
                    boolean askConfirmation = false;
                    boolean openURL = true;

                    deployFile = com.liquid.utility.getString(deplpoyBean, "sourceFile");

                    String webAppURL = utility.decodeHtml((String) utility.get(deplpoyBean, "webAppURL"));
                    openScript = "window.open(\"" + webAppURL + "\")";

                    String retVal = (String) deployManager.do_deploy(
                            deplpoyBean,
                            deployName,
                            null,
                            doBackup,
                            askConfirmation,
                            openURL,
                            true,
                            null,
                            request
                    );

                    if (retVal != null) {
                    }

                    Callback.send("<br/><br/><a href='javascript:void(0)' onclick='"+openScript+"' style=\"font-size:140%; color:navy\">"+webAppURL+"</a><br/>");
                    removeThreadSessionInfo();
                }
            } else {
                out.println("Internal error: configurazionr '"+deployName+"' NOT found");
            }
        } catch (Throwable th) {
            out.println("Internal error:"+th.getLocalizedMessage());
        }
    }
%>
<html>
<header>
    <script>
        function setup() {
            <%
                // Esecuzione dello script in uscita...
                out.println(openScript);
            %>
        }
    </script>
</header>
<style>
    .myForm {
        width: 100%;
        table-layout: fixed;
        font-family: verdana;
        font-size: 12px;
        padding: 10px;
    }

    .myForm td {
        margin: 10px;
        padding: 10px;
    }

    .myForm td:first-child {
        text-align: right;
    }

    .myFileInput {
        width: 100%;
        height: 30px;
    }

    .myInput {
        border-bottom-left-radius: 5px;
        border-top-right-radius: 5px;
        padding-left: 15px;
        height: 30px;
        background-color: rgba(250, 250, 210, 0.17);
        border: 1px solid burlywood;
        -webkit-box-shadow: 3px 4px 7px #636363;
    }

    .myCheck {
        width: 30px;
        height: 30px;
    }

    .singleTd {
        text-align: left !important;
    }

    .leftDiv {
        text-align: right;
        border-right: 1px solid lightblue;
    }

    .rightDiv {
        text-align: left;
    }

    .titleDiv {
        text-align: center;
        font-size: 200%;
    }

    .divider {
        border-top: 2px solid lightgray;
        height: 2px;
    }
</style>
<body onload="setup()">
<center>
    <table class="myForm">
        <tr>
            <td colspan="2">
                <div class="titleDiv">WAR Deploy</div>
            </td>
        </tr>
        <tr>
            <td colspan="2" class="divider"></td>
        </tr>
        <tr>
            <td>
                <div class="leftDiv">Deploy configuration</div>
            </td>
            <td>
                <div class="rightDiv">
                    <input class="myInput" type="text" id="@{cfg}" value="<%=deployName%>"/>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                <div class="leftDiv">War just deployed</div>
            </td>
            <td>
                <div class="rightDiv">
                    <input class="myFileInput" type="text" readonly disabled id="file" value="<%=deployFile%>" />
                </div>
            </td>
        </tr>
        <tr>
            <td>
                <div class="leftDiv"></div>
            </td>
            <td>
                <div class="rightDiv"></div>
            </td>
        </tr>
    </table>
</center>
</body>
</html>