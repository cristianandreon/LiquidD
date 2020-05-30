<%@ page
    language="java" 
    import="javax.servlet.*"
    import="javax.servlet.http.*"
    import="javax.servlet.jsp.*"
    import="com.liquid.workspace"
    import="com.liquid.connection"
    import="com.liquid.db"
    import="com.liquid.login"
    import="com.liquid.emailer"
    import="com.liquid.utility"
    errorPage="" 
%><%!

%><%

%>
<%-- 
    Document   : index
    Created on : Mar 20, 2020, 12:19:08 PM
    Author     : Cristitan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
    <head>
        <title>Liquid - WAR Deploy</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="shortcut icon" href="../images/favicon.ico" type="image/x-icon">
        <link rel="icon" href="../images/favicon.ico" type="image/x-icon">
        <link rel="stylesheet" accesskey="" href="./liquidx.css">

        
        <!-- Start Liquid Engine -->
        <%@ include file="/liquid/liquidXHeader.jsp" %>
        <%@ include file="/liquid/liquidSelector.jsp" %>


        <link rel="stylesheet" accesskey="" href="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.18.1/styles/default.min.css">
        <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/8.9.1/highlight.min.js"></script>
        <script>hljs.initHighlightingOnLoad();</script>


        <%  // Sorgente connessioni
            // predefintia : app.liquid.dbx.getDBConnection
            // workspace.default_connection = cnc_db.class.getMethod("getConnectionFromPool");
            // N. max records
            workspace.maxRows = 10000;

        %>

        
        <script src="./zooming.min.js"></script>        
        <script>


            function onLoad() {
                // Start top menu
                // Liquid.startMenuX(document.getElementById('menuXTop'), '<%=workspace.get_file_content(request, "/liquidMenuJSONs/mainMenuXTop.json", true)%>');
                
    
                // Scroll event
                document.body.addEventListener("scroll", scrollListner);
                $( "#title" ).data("small", 0);
                
                // highlight js
                hljs.configure({ tabReplace: '  ', classPrefix: '' });
                hljs.initHighlightingOnLoad();                
                $('pre code').each(function(i, block) {
                   hljs.highlightBlock(block);
                 });
                 
                // Image zoomer
                new Zooming().listen('.img-zoomable');
            }
            
            function scrollListner(e) {
                if(document.body.scrollTop > 70) {
                    if($( "#title" ).data("small") !== 1) {
                        $( "#title" ).animate({ fontSize: 17 }, 300, function(){ });
                        $( "#title" ).data("small", 1);
                    }
                } else {
                    if($( "#title" ).data("small") === 1) {
                        $( "#title" ).animate({ fontSize: 48 }, 300, function(){ });
                        $( "#title" ).data("small", 0);
                    }
                }
            }
            
            //Modalita' progettazione
            var glLiquidGenesisToken = '<%= workspace.enableProjectMode() %>';
            

            function startDeploysCfg() {
                Liquid.startControl('deploysCfg', '<%=workspace.get_file_content(request, "/deploy/deploysCfg.json")%>');
                Liquid.projectMode = true;
            }

            function startFormX() {
                // Liquid.startPopup('deploy', '<%=workspace.get_file_content(request, "/deploy/deploy.json")%>');
            }
            
            function closeFormX() {
                setTimeout( function() { startFormX() }, 1000 );
                $( '#deploy' ).fadeOut( "fast", function() { });
            }

            function deployDownloading(liquid, data, clientData, parameter, event) {
                document.getElementById("outDiv").innerHTML = ""+data;
            }                

        </script>
    </head>
    
    <body onload="onLoad(); startDeploysCfg(); startFormX(); ">
        <div id="bg" style="width:100%; height:100%;"><img src="./images/kupka3.jpg" style="width:100%; height:100%; -webkit-filter:opacity(7);opacity:0.07; -ms-filter: 'progid:DXImageTransform.Microsoft.Alpha(Opacity=7)'; filter: alpha(opacity=7); -khtml-opacity: 0.07;"/></div>
        <div id="feedbacksFrame" style="display:block" class="demoContent">
            <br/>
            <br/>
            <br/>
            <div class="title1">WAR Deploy - workInProgress</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <center>
                <table border=0 cellspacing=0 cellpadding=0 style="width:900px; height:360px; font-size:9pt; table-layout:auto; ">
                    <tr>
                        <td colspan="1" style="width:0%">
                            <div id="deploysCfg" style="height:100%; width:100%; background-color: rgba(213, 225, 232, 0.45">
                            </div>
                         </td>
                    </tr>
                </table>
            </center>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <center>
                <table border=0 cellspacing=0 cellpadding=0 style="display:none; width:600px; height:360px; font-size:9pt; table-layout:auto; ">
                    <tr>
                        <td colspan="1" style="width:0%">
                            <div id="deploy" style="height:100%; width:100%; background-color: rgba(213, 225, 232, 0.45">
                            </div>
                         </td>
                    </tr>
                </table>
            </center>
            <br/>
            <br/>
            <br/>            
			<br/>
            <br/>            
            <br/>
			<h1>Double click in a row to execute the war file deploy :</h1></br></br>
            <br/>            
            <br/>            
			Phases :</br></br>
			<ul> 
				<li>1° <b>upload</b> (if local file is newer or remote is missing)</li>
				<li>2° <b>backup</b> (only if remote file was uploaded and existing backup newer of missing)</li>
				<li>3° <b>remove</b> current .war</li>
		   		<li>4° <b>deploy</b> new .war</li>
		   		<li>5° <b>check</b> web app (http status 200)</li>
	   		</ul>
            <br/>
            <br/>            
            <br/>
            <h1>How does it work ?</h1><br/>
            <br/>
            <br/>
            LiquidD need database connection in order to store persistent data (the deploy's cpnfigurationas)
            <br/>
            <br/>
            Under the package <b>app.liquid.dbx</b> in the <b>public class connection </b> you should define :
            <br/>
            <br/>    
			   static String driver = "postgres" or "oracle" or "mysql" or "sqlserver"; <br/>
			   static String host = "your host name";<br/>
			   static String database = "your database";<br/>
			   static String user = "your user name";<br/>
			   static String password = "your password";<br/>
			<br/>
			<br/>
			<br/>
            
            <br/>            
			<h1>TODO :<h2></br></br>
			<ul> 
				<li>multiple process</li>
				<li>upload resume</li>
				<li>notifications</li>
	   		</ul>
        </div>
    </body>
</html>