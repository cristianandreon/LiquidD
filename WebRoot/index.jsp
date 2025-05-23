<%@ page
    language="java" 
    import="javax.servlet.*"
    import="javax.servlet.http.*"
    import="javax.servlet.jsp.*"
    import="java.net.InetAddress"
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
<%@include file="connection_setup.jsp"%>

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
        <% if("Cristian-PC".equalsIgnoreCase(hostName)) { // Case of my developing host %>
            <%@ include file="/liquid/liquidXHeader.jsp" %>
            <%@ include file="/liquid/liquidSelector.jsp" %>
            <%@ include file="/liquid/liquidStreamer.jsp" %>
        <% } else { // Normal case %>
            <%@ include file="/liquid/liquidHeader.jsp" %>
            <%@ include file="/liquid/liquidSelector.jsp" %>
        <% } %>


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
    
                // Scroll event
                document.body.addEventListener("scroll", scrollListner);
                jQ1124( "#title" ).data("small", 0);
                
                // highlight js
                hljs.configure({ tabReplace: '  ', classPrefix: '' });
                hljs.initHighlightingOnLoad();                
                jQ1124('pre code').each(function(i, block) {
                   hljs.highlightBlock(block);
                 });
                 
                // Image zoomer
                new Zooming().listen('.img-zoomable');
            }
            
            function scrollListner(e) {
                if(document.body.scrollTop > 70) {
                    if(jQ1124( "#title" ).data("small") !== 1) {
                        jQ1124( "#title" ).animate({ fontSize: 17 }, 300, function(){ });
                        jQ1124( "#title" ).data("small", 1);
                    }
                } else {
                    if(jQ1124( "#title" ).data("small") === 1) {
                        jQ1124( "#title" ).animate({ fontSize: 48 }, 300, function(){ });
                        jQ1124( "#title" ).data("small", 0);
                    }
                }
            }
            
            //Modalita' progettazione
            var glLiquidGenesisToken = '<%= workspace.enableProjectMode() %>';
            

            function startDeploysCfg() {
                Liquid.startControl('deploysCfg', '<%=workspace.get_file_content(request, "/deploy/deploysCfg.json")%>');
                Liquid.startControl('getLogsCfg', '<%=workspace.get_file_content(request, "/getLog/getLogsCfg.json")%>');
                Liquid.projectMode = true;
            }

            function startProjectHelper() {
                Liquid.startControl('projects', '<%=workspace.get_file_content(request, "/project/projects.json")%>');
                Liquid.startControl('machines', '<%=workspace.get_file_content(request, "/project/machines.json")%>');
                Liquid.startControl('schemas', '<%=workspace.get_file_content(request, "/project/schemas.json")%>');
                Liquid.startControl('project_machine_schema', '<%=workspace.get_file_content(request, "/project/project_machine_schema.json")%>');
                Liquid.startControl('fields', '<%=workspace.get_file_content(request, "/project/fields.json")%>');
            }

            function startSqlExecuter() {
                Liquid.startControl('sql_machines', '<%=workspace.get_file_content(request, "/sqlExecuter/sql_machines.json")%>');
                Liquid.startControl('sql_schemas', '<%=workspace.get_file_content(request, "/sqlExecuter/sql_schemas.json")%>');
                Liquid.startControl('sql_machine_schema', '<%=workspace.get_file_content(request, "/sqlExecuter/sql_machine_schema.json")%>');
            }

            function startSyncronyzer() {
                Liquid.startControl('syncronizer_machines', '<%=workspace.get_file_content(request, "/syncronizer/syncronizer_machines.json")%>');
                Liquid.startControl('syncronizer_data', '<%=workspace.get_file_content(request, "/syncronizer/syncronizer_data.json")%>');
            }

            function startImporter() {
                Liquid.startControl('importer', '<%=workspace.get_file_content(request, "/importer/importer.json")%>');
            }

            function startJobManager() {
                Liquid.startControl('jobManager', '<%=workspace.get_file_content(request, "/JobManager/jobManager.json")%>');
                Liquid.startControl('jobManagerCfg', '<%=workspace.get_file_content(request, "/JobManager/jobManagerCfg.json")%>');
                Liquid.startControl('jobManagerTotals', '<%=workspace.get_file_content(request, "/JobManager/jobManagerTotals.json")%>');
                Liquid.startControl('jobManagerOverviewAll', '<%=workspace.get_file_content(request, "/JobManager/jobManagerOverviewAll.json")%>');
                Liquid.startControl('jobManagerOverview', '<%=workspace.get_file_content(request, "/JobManager/jobManagerOverview.json")%>');
                Liquid.startControl('jobManagerMonthly', '<%=workspace.get_file_content(request, "/JobManager/jobManagerMonthly.json")%>');
            }


            function closeFormX() {
                setTimeout( function() { startFormX() }, 1000 );
                jQ1124( '#deploy' ).fadeOut( "fast", function() { });
            }

            function deployDownloading(liquid, data, clientData, parameter, event) {
                document.getElementById("outDiv").innerHTML = ""+data;
            }                        
            function getLogDownloading(liquid, data, clientData, parameter, event) {
                document.getElementById("outDivGetLog").innerHTML = ""+data;
            }                        
            function projectDownloading(liquid, data, clientData, parameter, event) {
                document.getElementById("outDivProject").innerHTML = ""+data;
            }
            function importerDownloading(liquid, data, clientData, parameter, event) {
                document.getElementById("outDivimporter").innerHTML = ""+data;
            }
            function sqlExecuterDownloading(liquid, data, clientData, parameter, event) {
                document.getElementById("outDivSqlexecuter").innerHTML = ""+data;
            }
            function syncronizerDownloading(liquid, data, clientData, parameter, event) {
                document.getElementById("outDivSyncronizer").innerHTML = ""+data;
            }

            function onExecuted(liquid, param) {
                if(param) {
                    if(param.command) {
                        if(param.command.response) {
                            if(param.command.response.data) {
                                var htmlResult = atob(param.command.response.data);
                                jQ1124( '#projectHelperHtmlResult' ).html(htmlResult);
                                jQ1124( '#projectHelperHtmlResult' ).slideDown();
                            }
                        }
                    }
                }
            }
            
            function execSQL(liquid, param) {
                Liquid.onButtonFromString(this, "{\"server\":\"app.liquidx.sql.sqlExecuter.execute\",\"name\":\"exec\",\"client\":\"\",\"params\":[\"formSQL\"],\"onDownloading\":\"sqlExecuterDownloading\"}");
            }
            
            function showLog(logFileName, webApp) {
                if(logFileName) {
                    window.open("file:///"+logFileName, webApp);
                }
            }


            function onSyncronizerExecuted(liquid, param) {
                if(param) {
                    if(param.command) {
                        if(param.command.response) {
                            if(param.command.response.data) {
                                var htmlResult = atob(param.command.response.data);
                                jQ1124( '#syncronizerHtmlResult' ).html(htmlResult);
                                jQ1124( '#syncronizerHtmlResult' ).slideDown();
                            }
                        }
                    }
                }
            }



            function copy_to_clipboard(text) {
                // copyText.select();
                // copyText.setSelectionRange(0, 99999); // For mobile devices

                // Copy the text inside the text field
                navigator.clipboard.writeText(text);

                // Alert the copied text
                Liquid.showToast("INFO", "Pasword copied into clipboard");
            }

        </script>
        
        
        <script type='text/javascript'>
            if(history.replaceState) history.replaceState({}, "", "/LiquidD");
        </script>

    </head>
    
    <body onload="onLoad(); startDeploysCfg(); startProjectHelper(); startImporter(); startSyncronyzer(); startSqlExecuter(); startJobManager();">
        <div id="bg" style="width:100%; height:100%;"><img src="./images/kupka3.jpg" style="width:100%; height:100%; -webkit-filter:opacity(7);opacity:0.07; -ms-filter: 'progid:DXImageTransform.Microsoft.Alpha(Opacity=7)'; filter: alpha(opacity=7); -khtml-opacity: 0.07;"/></div>

        
        <div class="spacer"></div>
        <center>
        <div class="title0  reflect">LiquidD : Developer utilities .. ver.1.10 - <%=workspace.version_string%></div>
        </center>
        <br/>
        <br/>        
        <div class="liquidForeignTables" style="border-bottom: 1px solid lightgrey" class="demoContent">
            <ul>
                <li id="welcomeFrameTab" class="liquidTabSel"><a href="javascript:void(0)" class="liquidTab liquidForeignTableEnabled" onClick="onMainTab(this)">Welcome</a></li>
                <li id="deployFrameTab" class=""><a href="javascript:void(0)" class="liquidTab liquidForeignTableEnabled" onClick="onMainTab(this)">Deployer</a></li>
                <li id="logFrameTab" class=""><a href="javascript:void(0)" class="liquidTab liquidForeignTableEnabled" onClick="onMainTab(this)">Web App Log</a></li>
                <li id="projectFrameTab" class=""><a href="javascript:void(0)" class="liquidTab liquidForeignTableEnabled" onClick="onMainTab(this)">Project Helper</a></li>
                <li id="importerFrameTab" class=""><a href="javascript:void(0)" class="liquidTab liquidForeignTableEnabled" onClick="onMainTab(this)">Importer</a></li>
                <li id="syncronizerFrameTab" class=""><a href="javascript:void(0)" class="liquidTab liquidForeignTableEnabled" onClick="onMainTab(this)">Syncronizer</a></li>
                <li id="sqlExecuterFrameTab" class=""><a href="javascript:void(0)" class="liquidTab liquidForeignTableEnabled" onClick="onMainTab(this)">SQL Exec</a></li>
                <li id="jobManagerFrameTab" class=""><a href="javascript:void(0)" class="liquidTab liquidForeignTableEnabled" onClick="onMainTab(this)">JOB Mamager</a></li>
            </ul>
        </div>                        

        <script>
            function onMainTab(obj) {
                if(obj.parentNode.id === 'welcomeFrameTab') {
                    jQ1124('#deployFrame').slideUp("fast");
                    jQ1124('#logFrame').slideUp("fast");
                    jQ1124('#projectFrame').slideUp("fast");
                    jQ1124('#importerFrame').slideUp("fast");
                    jQ1124('#sqlExecuterFrame').slideUp("fast");
                    jQ1124('#syncronizerFrame').slideUp("fast");
                    jQ1124('#welcomeFrame').slideDown("normal");
                    jQ1124('#jobManagerFrame').slideUp("fast");
                    document.getElementById('welcomeFrameTab').className = "liquidTabSel";
                    document.getElementById('logFrameTab').className = "";
                    document.getElementById('projectFrameTab').className = "";
                    document.getElementById('importerFrameTab').className = "";
                    document.getElementById('syncronizerFrameTab').className = "";
                    document.getElementById('sqlExecuterFrameTab').className = "";
                    document.getElementById('deployFrameTab').className = "";
                    document.getElementById('jobManagerFrameTab').className = "";
                } else if(obj.parentNode.id === 'deployFrameTab') {
                    jQ1124('#welcomeFrame').slideUp("fast");
                    jQ1124('#logFrame').slideUp("fast");
                    jQ1124('#projectFrame').slideUp("fast");
                    jQ1124('#importerFrame').slideUp("fast");
                    jQ1124('#syncronizerFrame').slideUp("fast");
                    jQ1124('#sqlExecuterFrame').slideUp("fast");
                    jQ1124('#deployFrame').slideDown("normal");
                    jQ1124('#jobManagerFrame').slideUp("fast");
                    document.getElementById('welcomeFrameTab').className = "";
                    document.getElementById('logFrameTab').className = "";
                    document.getElementById('projectFrameTab').className = "";
                    document.getElementById('importerFrameTab').className = "";
                    document.getElementById('syncronizerFrameTab').className = "";
                    document.getElementById('sqlExecuterFrameTab').className = "";
                    document.getElementById('deployFrameTab').className = "liquidTabSel";
                    document.getElementById('jobManagerFrameTab').className = "";
                    Liquid.onResize('deploysCfg');
                } else if(obj.parentNode.id === 'projectFrameTab') {
                    jQ1124('#welcomeFrame').slideUp("fast");
                    jQ1124('#logFrame').slideUp("fast");
                    jQ1124('#deployFrame').slideUp("fast");
                    jQ1124('#importerFrame').slideUp("fast");
                    jQ1124('#syncronizerFrame').slideUp("fast");
                    jQ1124('#sqlExecuterFrame').slideUp("fast");
                    jQ1124('#projectFrame').slideDown("normal");
                    jQ1124('#jobManagerFrame').slideUp("fast");
                    document.getElementById('welcomeFrameTab').className = "";
                    document.getElementById('projectFrameTab').className = "liquidTabSel";
                    document.getElementById('logFrameTab').className = "";
                    document.getElementById('importerFrameTab').className = "";
                    document.getElementById('deployFrameTab').className = "";
                    document.getElementById('syncronizerFrameTab').className = "";
                    document.getElementById('sqlExecuterFrameTab').className = "";
                    document.getElementById('jobManagerFrameTab').className = "";
                } else if(obj.parentNode.id === 'importerFrameTab') {
                    jQ1124('#welcomeFrame').slideUp("fast");
                    jQ1124('#logFrame').slideUp("fast");
                    jQ1124('#projectFrame').slideUp("fast");
                    jQ1124('#deployFrame').slideUp("fast");
                    jQ1124('#syncronizerFrame').slideUp("fast");
                    jQ1124('#sqlExecuterFrame').slideUp("fast");
                    jQ1124('#importerFrame').slideDown( "normal", function () { Liquid.onVisible('importerFrame') } );
                    jQ1124('#jobManagerFrame').slideUp("fast");
                    document.getElementById('welcomeFrameTab').className = "";
                    document.getElementById('projectFrameTab').className = "";
                    document.getElementById('logFrameTab').className = "";
                    document.getElementById('importerFrameTab').className = "liquidTabSel";
                    document.getElementById('deployFrameTab').className = "";
                    document.getElementById('syncronizerFrameTab').className = "";
                    document.getElementById('sqlExecuterFrameTab').className = "";
                    document.getElementById('jobManagerFrameTab').className = "";
                } else if(obj.parentNode.id === 'sqlExecuterFrameTab') {
                    jQ1124('#welcomeFrame').slideUp("fast");
                    jQ1124('#logFrame').slideUp("fast");
                    jQ1124('#projectFrame').slideUp("fast");
                    jQ1124('#deployFrame').slideUp("fast");
                    jQ1124('#importerFrame').slideUp("fast");
                    jQ1124('#syncronizerFrame').slideUp("fast");
                    jQ1124('#sqlExecuterFrame').slideDown( "normal", function () { Liquid.onVisible('sqlExecuterFrameTab') } );
                    jQ1124('#jobManagerFrame').slideUp("fast");
                    document.getElementById('welcomeFrameTab').className = "";
                    document.getElementById('projectFrameTab').className = "";
                    document.getElementById('logFrameTab').className = "";
                    document.getElementById('importerFrameTab').className = "";
                    document.getElementById('deployFrameTab').className = "";
                    document.getElementById('syncronizerFrameTab').className = "";
                    document.getElementById('sqlExecuterFrameTab').className = "liquidTabSel";
                    document.getElementById('jobManagerFrameTab').className = "";
                } else if(obj.parentNode.id === 'logFrameTab') {
                    jQ1124('#welcomeFrame').slideUp("fast");
                    jQ1124('#projectFrame').slideUp("fast");
                    jQ1124('#deployFrame').slideUp("fast");
                    jQ1124('#syncronizerFrame').slideUp("fast");
                    jQ1124('#sqlExecuterFrame').slideUp("fast");
                    jQ1124('#importerFrame').slideUp("fast");
                    jQ1124('#logFrame').slideDown( "normal", function () { Liquid.onVisible('logFrame') } );
                    jQ1124('#jobManagerFrame').slideUp("fast");
                    document.getElementById('welcomeFrameTab').className = "";
                    document.getElementById('projectFrameTab').className = "";
                    document.getElementById('importerFrameTab').className = "";
                    document.getElementById('deployFrameTab').className = "";
                    document.getElementById('syncronizerFrameTab').className = "";
                    document.getElementById('sqlExecuterFrameTab').className = "";
                    document.getElementById('logFrameTab').className = "liquidTabSel";
                    document.getElementById('jobManagerFrameTab').className = "";
                } else if(obj.parentNode.id === 'syncronizerFrameTab') {
                    jQ1124('#welcomeFrame').slideUp("fast");
                    jQ1124('#projectFrame').slideUp("fast");
                    jQ1124('#deployFrame').slideUp("fast");
                    jQ1124('#sqlExecuterFrame').slideUp("fast");
                    jQ1124('#logFrame').slideUp("fast");
                    jQ1124('#importerFrame').slideUp("fast");
                    jQ1124('#syncronizerFrame').slideDown( "normal", function () { Liquid.onVisible('jobManagerFrame') } );
                    jQ1124('#jobManagerFrame').slideUp("fast");
                    document.getElementById('welcomeFrameTab').className = "";
                    document.getElementById('projectFrameTab').className = "";
                    document.getElementById('importerFrameTab').className = "";
                    document.getElementById('deployFrameTab').className = "";
                    document.getElementById('sqlExecuterFrameTab').className = "";
                    document.getElementById('logFrameTab').className = "";
                    document.getElementById('syncronizerFrameTab').className = "liquidTabSel";
                    document.getElementById('jobManagerFrameTab').className = "";
                } else if(obj.parentNode.id === 'jobManagerFrameTab') {
                    jQ1124('#welcomeFrame').slideUp("fast");
                    jQ1124('#projectFrame').slideUp("fast");
                    jQ1124('#deployFrame').slideUp("fast");
                    jQ1124('#sqlExecuterFrame').slideUp("fast");
                    jQ1124('#logFrame').slideUp("fast");
                    jQ1124('#importerFrame').slideUp("fast");
                    jQ1124('#syncronizerFrame').slideUp("fast");
                    jQ1124('#jobManagerFrame').slideDown( "normal", function () { Liquid.onVisible('jobManagerFrame') } );
                    document.getElementById('welcomeFrameTab').className = "";
                    document.getElementById('projectFrameTab').className = "";
                    document.getElementById('importerFrameTab').className = "";
                    document.getElementById('deployFrameTab').className = "";
                    document.getElementById('sqlExecuterFrameTab').className = "";
                    document.getElementById('logFrameTab').className = "";
                    document.getElementById('syncronizerFrameTab').className = "";
                    document.getElementById('jobManagerFrameTab').className = "liquidTabSel";
                }
            }
        </script>
        


        <!-- WELCOME -->
        <div id="welcomeFrame" style="display:block" class="demoContent">
            <br/>
            <br/>
            <br/>
            <br/>            
            <br/>
            <div class="title1">LiquidD is a Web Application for the Developers .. by LiquidD you can : </div>
            <div class="spacer"></div>
            <br/>
            <div class="title1"></div>
            <br/>
            <br/>
            <ul> 
                <li>Deploy your web application</li>
                <li>Quickly download application's server log</li>
                <li>Add fields in multiple database at once</li>
                <li>Copy cascade rows from a data source to any others</li>
                <li>Syncronize table metadata from a data source to any others</li>
                <li>Execute SQL command in multiple targets</li>
                <li>Keep track of jobs</li>
            </ul>
            <br/>
            <br/>
            <br/>            
            <br/>
            <div class="title1">Geting Started :</div>
            <div class="spacer"></div>
            <br/>
            <br/>            
            <br/>            
            Startup default parameters :</br></br>
            <div class="code5" id="codeSample5">
                <pre class="code">
                    <code class="java">
    driver = <b>"postgres";</b>
    host = <b>"database.liquidd.com";</b>
    database = <b>"LiquidX";</b>
    user = <b>"liquid";</b>
    password = <b>"liquid";</b>
    service = <b>"";</b>
                    </code>
                </pre>
            </div>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            Current parameters are so defined :</br></br>
            <div class="code2" id="codeSample2">
                <pre class="code">
                    <code class="java">
    driver = <b>"<%=driver %>";</b>
    host = <b>"<%=host %>";</b>
    port = <b>"<%=port %>";</b>
    database = <b>"<%=database %>";</b>
    user = <b>"<%=user %>";</b>
    service = <b>"<%=service %>";</b>
    <br/>
    note : <b><%=setupMessage%></b>
                    </code>
                </pre>
            </div>
            <br/>
            <br/>
            These parameters define the LiquidD configuration, that is store in a database (Oracle/PostgreSQL/MySQL/SQLServer)
            <br/>
            <br/>
            Please note : if the host "database.liquidd.com" wasn't reachable it'll be replaced by "localhost"
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            You can define the host "database.liquidd.com" by setting it inside your host file
            <br/>
            <br/>
            Geting Started :
            If you want to replace any other parameters you can pass it in the url .. ex :
            <br/>
            <br/>
            <div class="code3" id="codeSample3">
                <pre class="code">
                    <code class="html">
localhost:90/LiquidD<b>?diver=oracle&host=172.1.2.110&port=1521&database=myLiquidDatabase&user=myLiquidUser&password=myLiquidPassword&service=myService</b>
                    </code>
                </pre>
            </div>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            Run docker image (in the terminal) :
            <br/>
            <div class="code4" id="codeSample4">
                <pre class="code">
                    <code class="bash">
                        
    docker pull liquidd/webapp

    docker run -p 90:8080 liquidd/webapp

                    </code>
                </pre>
            </div>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            Build docker image (in the terminal) :
            <br/>
            <div class="code6" id="codeSample6">
                <pre class="code">
                    <code class="bash">
    
    cd /Users/administrator/LiquiDDocked/web
    docker image build -t liquidd/webapp ./

                    </code>
                </pre>
            </div>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            Run web app by typing in your browser :
            <br/>
            <div class="code7" id="codeSample7">
                <pre class="code">
                    <code class="bash">
                        
    http://localhost:90/LiquidD/?host=192.168.0.90&port=5432&user=postgres&password=...

                    </code>
                </pre>
            </div>
            <br/>
            N.B.: you need to define a data source outside docker
            N.B.: once the parameters was recived they'll be stored in session and hidden from the url
            <br/>
            <br/>            
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            If you need to export/import docker image :
            <br/>
            <div class="code8" id="codeSample8">
                <pre class="code">
                    <code class="bash">
                        
    docker save liquidd/webapp > /tmp/liquidd-webapp.tar
    
    docker load < /tmp/liquidd-webapp.tar

                    </code>
                </pre>
            </div>
            <br/>
            N.B.: you need to define a data source outside docker
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            If you need to push docker image into the hub:
            <br/>
            <div class="code9" id="codeSample9">
                <pre class="code">
                    <code class="bash">
                        
    docker login -u liquidframework
    
    // FCK : Error saving credentials: error storing credentials - err: exit status 1, out: `Unable to obtain authorization for this operation.`
    
    docker push liquidd/webapp
                    </code>
                </pre>
            </div>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <div style="float:left; display: block">
                <a href="javascript:void(0)" onclick='window.open("/LiquidD/liquid.index.jsp")'>Surf insider Liquid projecting page</a>
            </div>
            <br/>

        </div>   

        

        <!-- DEPLOY MANAGER -->
        <div id="deployFrame" style="display:none" class="demoContent">
            <br/>
            <br/>
            <br/>
            <div class="title1">WAR Deployer</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <div style="float:left; display: block"><a href="javascript:void(0)" onclick='copy_to_clipboard(document.getElementById("vpnPassw").dataset.rel)'>Copy vpn password to clipboard</a></div><b><span data-rel="FshaYvShzMCrAKHn1#" id="vpnPassw"></span></b>
            <br/>
            <br/>
            <center>
                <div style="perspective:1500px;-webkit-perspective: 1500px">
                    <table border=0 cellspacing=0 cellpadding=0 class="css_transform2" style="width:1024px; height:auto; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363; ">
                        <tr>
                            <td colspan="1">
                                <div id="deploysCfg" style="height:660px; width:100%; background-color: rgba(213, 225, 232, 0.45)">
                                </div>
                             </td>
                        </tr>
                        <tr>
                            <td colspan="1">
                                <div id="outDiv" style="height:50px; width:100%; border-bottom:1px solid lightgray"></div>
                             </td>
                        </tr>
                        <tr>
                            <td colspan="1">
                                <input id="doBackup" type="checkbox" style="margin:10px; height:30px; width:30px; " checked >Do Backup</input>
                             </td>
                        </tr>
                        <tr>
                            <td colspan="1">
                                <input id="askConfirmation" type="checkbox" style="margin:10px; height:30px; width:30px; " >Ask confirmation before deploy</input>
                             </td>
                        </tr>
                        <tr>
                            <td colspan="1">
                                <input id="openURL" type="checkbox" style="margin:10px; height:30px; width:30px; " checked >Open URL when done</input>
                             </td>
                        </tr>
                        
                    </table>
                </div>
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
                            <div id="deploy" width="30px" height="30px" style="height:100%; width:100%; background-color: rgba(213, 225, 232, 0.45)">
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
            simply double click in the row ... deploy phases :</br></br>
            <ul> 
                    <li>1° <b>upload</b> (if local file is newer or remote is missing) [in the "copyFolder"]</li>
                    <li>2° <b>backup</b> (only if .war file succesfully uploaded and previous .war backup file is missing) [in the "backupFolder"]</li>
                    <li>3° <b>remove</b> current .war</li>
                    <li>4° <b>deploy</b> new .war [in the "deployFolder"]</li>
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
            Don't double click on an other row while a deploy is running ... simply open multiple tab in your browser </br></br>
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
            Please see in the <a href="javascript:void(0)" onlick='document.getElementById("welcomeFrameTab").children[0].click();'>Welcome tab</a> how to do that ...
            <br/>
            <br/>
            <br/>
            N.B.: You don't need to create any tables inside your database/schema, Liquid create its for you            
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
                    <li>Upload resume</li>
                    <li>Notifications</li>
            </ul>
        </div>


        
        
        <!-- ---------------- -->
        <!-- GET LOGs MANAGER -->
        <!-- ---------------- -->
        <div id="logFrame" style="display:none" class="demoContent">
            <br/>
            <br/>
            <br/>
            <div class="title1">Web App Log downloader</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <center>
                <div style="perspective:1500px;-webkit-perspective: 1500px">
                    <table border=0 cellspacing=0 cellpadding=0 class="css_transform2" style="width:1024px; height:auto; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363; ">
                        <tr>
                            <td colspan="1">
                                <div id="getLogsCfg" style="height:560px; width:100%; background-color: rgba(213, 225, 232, 0.45)">
                                </div>
                             </td>
                        </tr>
                        <tr>
                            <td colspan="1">
                                <div id="outDivGetLog" style="height:50px; width:100%; border-bottom:1px solid lightgray"></div>
                             </td>
                        </tr>
                    </table>
                </div>
            </center>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>            
            <br/>
            <br/>            
            <br/>
            <div class="title1">How to get a log :</div>
            <div class="spacer"></div>
            <br/>
            <br/>            
            <br/>            
            simply double click in the row ... </br></br>
            <br/>
            <br/>            
            <br/>
            <br/>
            <br/>
            Allowing the browser to open local file for mac:

                <dir>open -a Google\ Chrome --args --disable-web-security -–allow-file-access-from-files</dir>

            <br/>
            <br/>
            .. and for windows:

                <dir>"C:\PathTo\Chrome.exe" –allow-file-access-from-files -disable-web-security</dir>
            <br/>
            <br/>
            <br/>
        </div>
        


        <!-- -------------- -->
        <!-- PROJECT HELPER -->
        <!-- -------------- -->
        <div id="projectFrame" style="display:none" class="demoContent">
            <br/>
            <br/>
            <br/>
            <div class="title1">JAVA Web Project Helper - <span style="font-size:80%">adding fields in multiple target</span></div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <center>
                <div style="perspective:1500px;-webkit-perspective: 1500px">
                    <table border=0 cellspacing=0 cellpadding=0 class="css_transform2" style="width:calc(100% - 50px); height:450px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                        <tr>
                            <td colspan="1" style="height:400px; width:100%; ">
                                <div id="fields" style="height:100%; width:100%; background-color: rgba(213, 225, 232, 0.45)">
                                </div>
                             </td>
                        </tr>
                        <tr>
                            <td colspan="1" style="height:50px;">
                                <div id="outDivProject" style="height:100%; width:100%; border:1px solid lightgray"></div>
                             </td>
                        </tr>
                        <tr>
                            <td colspan="1">
                                <input id="executeSQL" type="checkbox" style="padding:10px; height:30px; width:30px; " checked >Execute SQL</input>
                             </td>
                        </tr>
                    </table>
                </div>
            </center>
            <br/>
            <div id="projectHelperHtmlResult" style="display:none; width:calc(100% - 50px); border:1px solid lightgray"></div>
            <br/>
            <br/>
            <br/>
            <br/>
            <div class="title1">Projects</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:600px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="projects" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                     </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>            
            <br/>
            <br/>
            <br/>
            <div class="title1">Machines</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:800px; font-size:9pt; table-layout:auto;  -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="machines" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                     </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>            
            <br/>
            <br/>
            <br/>
            <div class="title1">Schemas</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:600px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="schemas" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                     </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>            
            <br/>
            <br/>
            <br/>
            <br/>
            <div class="title1">Project's Machine's schemas</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:600px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="project_machine_schema" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                     </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>            
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <div id="spacer" style="height:200px; width:100%; ">
            </div>
        </div>
        
        
        
        <!-- --------------- -->
        <!-- IMPORTER HELPER -->
        <!-- --------------- -->
        <div id="importerFrame" style="display:none" class="demoContent">
            <br/>
            <br/>
            <br/>
            <div class="title1">Importer - <span style="font-size:80%">copy cascade rows from data source (jdb connection) to target</span></div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <div class="title2"><li><span style="font-size:80%">define source and target jdbc (host/port/service/user/password) connection</span></li></div>
            <br/>
            <div class="title2"><li><span style="font-size:80%">define the table</span></div>
            <br/>
            <div class="title2"><li><span style="font-size:80%">define the primary keys value to copy (comma separated string)</span></li></div>
            <br/>
            <br/>
            <center>
                <div style="perspective:1500px;-webkit-perspective: 1500px">
                    <table border=0 cellspacing=0 cellpadding=0 class="css_transform2" style="width:calc(100% - 50px); height:500px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363; ">
                        <tr>
                            <td colspan="1" style="height:450px; width:100%; ">
                                <div id="importer" style="height:100%; width:100%; background-color: rgba(213, 225, 232, 0.45)">
                                </div>
                             </td>
                        </tr>
                        <tr>
                            <td colspan="1" style="height:50px;">
                                <div id="outDivimporter" style="height:100%; width:100%; border:1px solid lightgray"></div>
                             </td>
                        </tr>
                    </table>
                </div>
            </center>
            <br/>
            <br/>
            <br/>
            <br/>            
            <br/>
            <br/>
            <br/>
        </div>
        


        <!-- -------------- -->
        <!-- SYNCRONIZER    -->
        <!-- -------------- -->
        <div id="syncronizerFrame" style="display:none" class="demoContent">
            <br/>
            <br/>
            <br/>
            <div class="title1">Syncronizer - <span style="font-size:80%">Syncronize Database Tables across Machines</span></div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <br/>
            <br/>
            <center>
                <div style="perspective:1500px;-webkit-perspective: 1500px">
                    <form id="syncronizerSQL" onsubmit="">
                        <table border=0 cellspacing=0 cellpadding=0 class="css_transform2" style="width:calc(100% - 50px); height:650px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                            <tr>
                            </tr>
                            <tr>
                                <td colspan="1" style="height:70px;">
                                    <div id="syncronizer_data" style="height:100%; width:100%; height:560px; background-color: rgba(213, 225, 232, 0.45)">
                                    </div>
                                 </td>
                            </tr>
                            <tr>
                                <td colspan="1">
                                    <input id="previewSyncronizer" type="checkbox" style="padding:10px; height:30px; width:30px; " checked >Preview</input>
                                 </td>
                            </tr>
                            <tr>
                                <td colspan="1">
                                    <input id="deepMode" type="checkbox" style="padding:10px; height:30px; width:30px; " checked >Deep mode</input>
                                 </td>
                            </tr>
                        <tr>
                            <td colspan="1" style="height:10px;">
                             </td>                             
                        </tr>
                        <tr>
                            <td colspan="1" style="height:50px;">
                                <div id="outDivSyncronizer" style="height:100%; width:100%; border:1px solid lightgray"></div>
                             </td>                             
                        </tr>
                        </table>
                    </form>
                </div>
            </center>
            <br/>
            <br/>
            <br/>
            <b>Preview</b> : show differences and the updating sql without execute any actions
            <br/>
            <b>DeepMode</b> : compare matching fields by checking data type, size, default, remarks
            <br/>
            <br/>
            <b>N.B.</b> : you can syncronize multiple tables by wilcard (es.: <b>myTable*</b> in the source table field)
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>            
            <div id="syncronizerHtmlResult" style="display:none; width:calc(100% - 50px); border:1px solid lightgray"></div>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <div class="title1">Machines</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:800px; font-size:9pt; table-layout:auto;  -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="syncronizer_machines" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                     </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>            
            <br/>
        </div>
                


        <!-- -------------- -->
        <!--  SQL EXECUTER  -->
        <!-- -------------- -->
        <div id="sqlExecuterFrame" style="display:none" class="demoContent">
            <br/>
            <br/>
            <br/>
            <div class="title1">SQL executer - <span style="font-size:80%">execute sql in multiple target</span></div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <center>
                <div style="perspective:1500px;-webkit-perspective: 1500px">
                    <form id="formSQL" onsubmit="">
                        <table border=0 cellspacing=0 cellpadding=0 class="css_transform2" style="width:calc(100% - 50px); height:450px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                            <tr>
                            </tr>
                            <tr>
                                <td colspan="1" style="height:50px;">
                                    <textarea id="sql" type="" style="padding:10px; height:300px; width:100%; " ></textarea>
                                 </td>
                            </tr>
                            <tr>
                                <td colspan="1">
                                    <input id="confirm" type="checkbox" style="padding:10px; height:30px; width:30px; " checked >Confirmation</input>
                                 </td>
                            </tr>
                            <tr>
                                <td colspan="1">
                                    <button 
                                        style="padding:10px; height:50px; width:300px; "
                                        onclick="execSQL(); return false;"
                                        >Execute</button>
                                 </td>
                            </tr>
                        <tr>
                            <td colspan="1" style="height:50px;">
                                <div id="outDivSqlexecuter" style="height:100%; width:100%; border:1px solid lightgray"></div>
                             </td>
                             
                        </tr>
                        </table>
                    </form>
                </div>
            </center>
            <br/>
            <div id="htmlResult1" style="display:none; width:calc(100% - 50px); border:1px solid lightgray"></div>
            <br/>
            <br/>
            <br/>
            <br/>
            <div class="title1">Machines</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:800px; font-size:9pt; table-layout:auto;  -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="sql_machines" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                     </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>            
            <br/>
            <br/>
            <br/>
            <div class="title1">Schemas</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:600px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="sql_schemas" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                     </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>            
            <br/>
            <br/>
            <br/>
            <div class="title1">Machine's schemas</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:600px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="sql_machine_schema" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                     </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>            
            <br/>
            <br/>
            <br/>
        </div>



        <!-- -------------- -->
        <!-- JOB MANAGER    -->
        <!-- -------------- -->
        <div id="jobManagerFrame" style="display:none" class="demoContent">
            <br/>
            <br/>
            <br/>
            <div class="title1">JOB Manager - <span style="font-size:80%">a jobs tracker</span></div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <center>
                <div style="perspective:1500px;-webkit-perspective: 1500px">
                    <table border=0 cellspacing=0 cellpadding=0 class="css_transform2" style="width:calc(100% - 50px); height:450px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                        <tr>
                            <td colspan="1" style="height:400px; width:100%; ">
                                <div id="jobManager" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="1" style="height:50px;">
                                <div id="outDivJobManager" style="height:100%; width:100%; border:1px solid lightgray"></div>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="1">
                                <!-- <input id="executeJOB" type="checkbox" style="padding:10px; height:30px; width:30px; " checked >Execute SQL</input> -->
                            </td>
                        </tr>
                    </table>
                </div>
            </center>
            <br/>
            <div id="jobManagerHtmlResult" style="display:none; width:calc(100% - 50px); border:1px solid lightgray"></div>
            <br/>
            <br/>
            <br/>
            <br/>
            <div class="title1">Parameters</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:600px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="jobManagerCfg" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                    </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>
            <br/>
            <div class="title1">Overview All</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:600px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="jobManagerOverviewAll" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                    </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>
            <br/>
            <div class="title1">Overview</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:600px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="jobManagerOverview" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                    </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>
            <br/>
            <div class="title1">Monthly</div>
            <div class="spacer"></div>
            <br/>
            <br/>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:600px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="jobManagerMonthly" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                    </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <div class="title1">Totals</div>
            <div class="spacer" style="height:200px; width:100%; ">
            </div>
            <table border=0 cellspacing=0 cellpadding=0 style="margin-left:75px; width:600px; font-size:9pt; table-layout:auto; -webkit-box-shadow: 4px 4px 8px 1px #636363;">
                <tr>
                    <td colspan="1" style="">
                        <div id="jobManagerTotals" style="height:100%; width:100%; height:360px; background-color: rgba(213, 225, 232, 0.45)">
                        </div>
                    </td>
                </tr>
            </table>
            <br/>
            <br/>
            <br/>
            <br/>
        </div>


    </body>
</html>