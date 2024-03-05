<%@ page
    language="java" 
    import="java.net.InetAddress"
    errorPage=""
%>
<%@ page import="java.net.Socket" %>
<%@ page import="java.net.InetSocketAddress" %>
<%@ page import="java.io.IOException" %><%!

    private static boolean isReachable(String addr, int openPort, int timeOutMillis) {
        try (Socket soc = new Socket()) {
            soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            return true;
        } catch (IOException ex) {
            System.out.println(ex);
            return false;
        }
    }

%><%

    String driver = "postgres";
    String host = "database.liquidd.com";
    String database = "LiquidX";
    String user = "liquid";
    String password = "liquid";
    String port = "";
    String service = "";

    String hostName = InetAddress.getLocalHost().getHostName();
    String setupMessage = "nothing went wrong";
    
    String overloadDriver = request.getParameter("driver");
    String overloadHost = request.getParameter("host");
    String overloadPort = request.getParameter("port");
    String overloadUser = request.getParameter("user");
    String overloadPassword = request.getParameter("password");
    String overloadDatabase = request.getParameter("database");
    String overloadService = request.getParameter("service");
    

    if(overloadDriver != null && !overloadDriver.isEmpty()) {
        driver = overloadDriver;
        session.setAttribute("driver", driver);
    } else {
        if(session.getAttribute("driver") != null) 
            driver = (String)session.getAttribute("driver");
    }
    if(overloadHost != null && !overloadHost.isEmpty()) {
        host = overloadHost;
        session.setAttribute("host", host);
    } else {
        if(session.getAttribute("host") != null) 
            host = (String)session.getAttribute("host");
    }
    if(overloadUser != null && !overloadUser.isEmpty()) {
        user = overloadUser;
        session.setAttribute("user", user);
    } else {
        if(session.getAttribute("user") != null) 
            user = (String)session.getAttribute("user");
    }
    if(overloadPassword != null && !overloadPassword.isEmpty()) {
        password = overloadPassword;
        session.setAttribute("password", password);
    } else {
        if(session.getAttribute("password") != null) 
            password = (String)session.getAttribute("password");
    }
    if(overloadDatabase != null && !overloadDatabase.isEmpty()) {
        database = overloadDatabase;
        session.setAttribute("database", database);
    } else {
        if(session.getAttribute("database") != null) 
            database = (String)session.getAttribute("database");
    }
    if(overloadPort != null && !overloadPort.isEmpty()) {
        port = overloadPort;
        session.setAttribute("port", port);
    } else {
        if(session.getAttribute("port") != null) 
            port = (String)session.getAttribute("port");
    }
    if(overloadService != null && !overloadService.isEmpty()) {
        service = overloadService;
        session.setAttribute("service", service);
    } else {
        if(session.getAttribute("service") != null) 
            service = (String)session.getAttribute("service");
    }
    if(port != null && !port.isEmpty()) {
    } else {
        port = "5432";
    }
 
    if(host != null && !host.isEmpty()) {
        try {
            InetAddress addr = InetAddress.getByName(host);
            // for(InetAddress addr: address) {
            boolean reachable = addr.isReachable(Integer.parseInt(port)) ;
            if (!reachable) {
                if(!isReachable(addr.getHostAddress(), Integer.parseInt(port), 3000)) {
                    setupMessage = "host " + host + " : cannot reach at port " + port;
                } else {
                    setupMessage = "";
                    // break;
                }
            }
        } catch (Exception e){
            setupMessage = "error reaching host "+host+" at port "+port+" : "+e;
        }
    }
    
    try {
        com.liquid.connection.resetLiquidDBConnection( );
        com.liquid.connection.addLiquidDBConnection( driver, host, port, database, user, password, service, true );
    } catch (Exception e){
        e.printStackTrace();
    }
    
    // Test locale VPS GS
    // http://localhost:90/LiquidD/?host=192.168.0.90&port=5432&user=postgres&password=postgres

%>