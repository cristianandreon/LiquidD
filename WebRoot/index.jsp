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
        <link rel="stylesheet" accesskey="" href="./liquidD.css">

        
        <!-- Start Liquid Engine -->
        <%@ include file="/liquid/liquidHeader.jsp" %>
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
        <script src="./notifications.js"></script>        
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
                <table border=0 cellspacing=0 cellpadding=0 style="width:1024px; height:360px; font-size:9pt; table-layout:auto; ">
                    <tr>
                        <td colspan="1">
                            <div id="deploysCfg" style="height:360px; width:100%; background-color: rgba(213, 225, 232, 0.45">
                            </div>
                         </td>
                    </tr>
                    <tr>
                        <td colspan="1">
                            <div id="outDiv" style="height:50px; width:100%; border:1px solid lightgray"></div>
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
            <div class="title1">How to run a deploy :</div>
            <div class="spacer"></div>
            <br/>
            <br/>            
            <br/>            
            simply double click in the row ... deploy Phases :</br></br>
            <ul> 
                    <li>1° <b>upload</b> (if local file is newer or remote is missing)</li>
                    <li>2° <b>backup</b> (only if .war file succesfully uploaded and is more recent, or previous .war file is missing)</li>
                    <li>3° <b>remove</b> current .war</li>
                    <li>4° <b>deploy</b> new .war</li>
                    <li>5° <b>check</b> web app (http status 200)</li>
            </ul>
            <br/>
            <br/>            
            <br/>
            <br/>
            <br/>
            <br/>
            <div class="title1">How to run multiple deploys at the same time :</div>
            <div class="spacer"></div>
            <br/>
            <br/>            
            <br/>            
            Don't double on other row while a deploy is running ... simply open multiple tab in your browser </br></br>
            <br/>
            <br/>            
            <br/>
            <br/>
            <br/>
            <br/>
            <div class="title1">How does it work ?</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            LiquidD need a database connection in order to store persistent data (the deploy's configuration detail)
            <br/>
            <br/>
            Under the package <b>app.liquid.dbx</b> in the <b>public class connection </b> you should define :
            <br/>
            <br/>
            <div class="code1" id="codeSample1">
                <pre class="code">
                    <code class="java">
public class connection {
&#x9;&#x9;&#x9;   static String driver = &#x22;postgres&#x22; // or &#x22;oracle&#x22; or &#x22;mysql&#x22; or &#x22;sqlserver&#x22;;
&#x9;&#x9;&#x9;   static String host = &#x22;your host name&#x22;;
&#x9;&#x9;&#x9;   static String database = &#x22;your database&#x22;;
&#x9;&#x9;&#x9;   static String user = &#x22;your user name&#x22;;
&#x9;&#x9;&#x9;   static String password = &#x22;your password&#x22;;
                    </code>
                </pre>
            </div>
            <br/>
            N.B.: You don't need to crete any tables inside your database/schema, Liquid create its for you            
            <br/>
            N.B.: May be you need to add jdbc driver to the project (I'm using postgresql-42.2.12.jre7.jar)
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>            
            <div class="title1">TODO :</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <ul> 
                    <li>Show waiters</li>
                    <li>Upload resume</li>
                    <li>Notifications</li>
            </ul>
        </div>
    </body>
</html>