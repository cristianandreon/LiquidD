package app.liquid.dbx_old;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.liquid.db;

/**
 *
 * @author Cristitan Andreon cristianandreon.eu
 */
public class connection {
    
    static String driver = "postgres";
    static String host = "";

    static String database = "LiquidX";
    static String user = "liquid";
    static String password = "liquid";
    static Class driverClass = null;
    private static boolean hasSetup;
    private static boolean hasSetupDB;
    
    
    static public Connection getDBConnection() throws Exception, Throwable {        
    	try {
            if("oracle".equalsIgnoreCase(driver)) {
                if(driverClass == null) driverClass = Class.forName("oracle.jdbc.driver.OracleDriver");
                user = "system";
                password = "oracle";
                return DriverManager.getConnection("jdbc:oracle:thin:@"+host+":1521:xe",user,password);
            } else if("postgres".equalsIgnoreCase(driver)) {
                if(driverClass == null) driverClass = Class.forName("org.postgresql.Driver");
                return DriverManager.getConnection("jdbc:postgresql://"+host+":5432/"+database,user,password);
            } else if("mysql".equalsIgnoreCase(driver)) {
                if(driverClass == null) driverClass = Class.forName("com.mysql.jdbc.Driver");
                return DriverManager.getConnection("jdbc:mysql://"+host+":3306/"+database,user,password);
            } else if("sqlserver".equalsIgnoreCase(driver)) {
                if(driverClass == null) driverClass = Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                user = "SA";
                password = ""; // Password123 // save ad administrator in VDS provisioned
                return DriverManager.getConnection("jdbc:sqlserver://"+host+":1433;databaseName="+database,user,password);
            } else {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, "drive not recognized");
            }            
    	} catch(Throwable th) {
    		hasSetup = false;
        	if(!hasSetup) {
        		hasSetup = true;
        		if(db.create_database_schema(driver, "localhost", database, "liquidx", user, password)) {
        			return getDBConnection();
        		}
        	}
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, th);
            throw th;
    	}
        return null;    
    }

    static public Connection getDBConnection(String database) throws Exception, Throwable {        
    	try {            
            if("oracle".equalsIgnoreCase(driver)) {
                if(driverClass == null) driverClass = Class.forName("oracle.jdbc.driver.OracleDriver");
                user = "system";
                password = "oracle";
                return DriverManager.getConnection("jdbc:oracle:thin:@"+host+":1521:xe",user,password);
            } else if("postgres".equalsIgnoreCase(driver)) {
                if(driverClass == null) driverClass = Class.forName("org.postgresql.Driver");
                return DriverManager.getConnection("jdbc:postgresql://"+host+":5432/"+database,user,password);
            } else if("mysql".equalsIgnoreCase(driver)) {
                if(driverClass == null) driverClass = Class.forName("com.mysql.jdbc.Driver");
                return DriverManager.getConnection("jdbc:mysql://"+host+":3306/"+database,user,password);
            } else if("sqlserver".equalsIgnoreCase(driver)) {
                if(driverClass == null) driverClass = Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                user = "SA";
                password = ""; // Password123 // save ad administrator in VDS provisioned
                return DriverManager.getConnection("jdbc:sqlserver://"+host+":1433;databaseName="+database,user,password);
            } else {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, "drive not recognized");
            }            
    	} catch(Throwable th) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, th);
            throw th;
    	}
        return null;    
    }
    
    static public String getConnectionDesc() {
    	try {            
            return "[ *** LiquidX : "+driver+" @"+host+" database:"+database+" user:"+user+ " *** ]";
    	} catch(Throwable th) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, th);
    	}
        return null;    
    }       
}