package app.liquid.dms;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Cristitan Andreon cristianandreon.eu
 */
public class connection {
    
    static String dmsSchema = "liquid";
    static String dmsTable = "documents";
    
    public static String getDocuments( Object tbl_wrk, Object params, Object clientData, Object freeParam ) {
        StringBuilder resultSet = new StringBuilder("{\"resultSet\":[");
        Connection conn = null;
        PreparedStatement psdo = null;
        ResultSet rsdo = null;
        String sQuery = null;
        String sWhere = "";
        int nRecs = 0;
        try {                
            // JSONObject paramJson = new JSONObject((String)params);
            // { database:liquid.tableJson.database, schema:liquid.tableJson.schema, table:liquid.tableJson.table, ids:nodeKeys };
            if(freeParam != null) {
                conn = app.liquid.dbx.connection.getDBConnection();
                if(conn != null) {
                    ArrayList<String> keyList = (ArrayList<String>)freeParam;
                    for(int ik=0; ik<keyList.size(); ik++) {
                        sWhere += sWhere.length()>0?" OR ":"" + "link='"+keyList.get(ik)+"'";
                    }
                    sQuery = "SELECT * from "+dmsSchema+"."+dmsTable+" WHERE ("+sWhere+")";
                    psdo = conn.prepareStatement(sQuery);
                    rsdo = psdo.executeQuery();
                    if(rsdo != null) {
                        while(rsdo.next()) {
                            String file = rsdo.getString("file");
                            // N.B.: Protocollo JSON : nella risposta JSON il caratere "->\" è a carico del server, e di conseguenza \->\\
                            file = file != null ? file.replace("\\", "\\\\").replace("\"", "\\\"") : "";
                            int size = rsdo.getInt("size");
                            String date = rsdo.getString("date");
                            String note = rsdo.getString("note");
                            String index = rsdo.getString("id");
                            String options = "";
                            String fieldSet = "{" + "\"file\":\""+(file!=null?file:"")+"\", \"size\":"+size+",\"note\":\""+note+"\""+",\"index\":\""+index+"\"" + "}";
                            resultSet.append( (nRecs>0?",":"") + fieldSet);
                            nRecs++;
                        }
                    }
                    if(rsdo != null) rsdo.close();
                    if(psdo != null) psdo.close();            
                }
            }
        } catch (Throwable e) {
            System.err.println("Query Error:" + e.getLocalizedMessage() + sQuery);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        resultSet.append("]}");
        return resultSet.toString();
    }   
    
    static public String uploadDocument( Object tbl_wrk, Object params, Object clientData, Object freeParam ) {
        StringBuilder resultSet = new StringBuilder("{\"resultSet\":[");
        Connection conn = null;
        PreparedStatement psdo = null;
        String sQuery = null;
        String sWhere = "";
        int nRecs = 0;
        try {                
            JSONObject paramsJson = new JSONObject((String)params);
            JSONObject paramJson = paramsJson.getJSONObject("params");
            // paramJson : { database: ... , schema: ..., table: ..., name: ..., ids:nodesKey, file:"", size:"", note:"", fileContent:"" mimeType:""};
            if(freeParam != null) {
                conn = app.liquid.dbx.connection.getDBConnection();
                if(conn != null) {
                    // N.B.: one document can refers to multiple rows in table, if rowSelect is "multiple"
                    // JSONArray ids = paramJson.getJSONArray("ids");
                    ArrayList<String> keyList = null;
                    if(freeParam != null) {
                        keyList = (ArrayList<String>)freeParam;
                    }                    
                    for (int i=0; i<keyList.size(); i++) {
                        String fileAbsolutePath = "/MDSRoot/"+paramJson.getString("file");
                        sQuery = "INSERT INTO "+dmsSchema+"."+dmsTable+" (file,size,note,link) VALUES ('"+fileAbsolutePath+"','"+paramJson.getInt("size")+"','"+paramJson.getString("note")+"','"+keyList.get(i)+"')";
                        psdo = conn.prepareStatement(sQuery);
                        int res = psdo.executeUpdate();

                        String fieldSet = "{" + "\"file\":\""+(paramJson.getString("file"))+"\", \"size\":"+paramJson.getInt("size")+",\"note:\":\""+paramJson.getString("note")+"\""+",\"options:\":\""+""+"\"" + "}";
                        resultSet.append( nRecs>0?",":"" + fieldSet);
                        nRecs++;
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println("Query Error:" + e.getLocalizedMessage() + sQuery);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        resultSet.append("]}");
        return resultSet.toString();
    }
    
    static public Object [] downloadDocument( Object tbl_wrk, Object params, Object clientData, Object freeParam ) {
        byte [] fileContent = null;
        String fileName = "";
        String fileMimeType = "";
        Connection conn = null;
        PreparedStatement psdo = null;
        ResultSet rsdo = null;
        String sQuery = null;
        String sWhere = "";
        int nRecs = 0;
        try {                
            JSONObject paramsJson = new JSONObject((String)params);
            JSONObject paramJson = paramsJson.getJSONObject("params");
            // { paramJson:..., schema:..., table:..., ids:..., index: ... };
            if(paramJson != null) {
                conn = app.liquid.dbx.connection.getDBConnection();
                if(conn != null) {
                    sQuery = "SELECT * from "+dmsSchema+"."+dmsTable+" WHERE (id='"+paramJson.getString("index")+"')";
                    psdo = conn.prepareStatement(sQuery);
                    rsdo = psdo.executeQuery();
                    if(rsdo != null) {
                        while(rsdo.next()) {
                            String file = rsdo.getString("file");
                            int size = rsdo.getInt("size");
                            String date = rsdo.getString("date");
                            String note = rsdo.getString("note");
                            String index = rsdo.getString("id");
                            String options = "";
                            Path path = new File(file).toPath();
                            if(path != null) {
                                fileName = file ;
                                fileMimeType = Files.probeContentType(path);
                                fileContent = Files.readAllBytes( path ) ;
                            } else {
                                System.err.println("ERROR : File \"" + file+"\" not found");
                            }
                            nRecs++;
                        }
                    }
                    if(rsdo != null) rsdo.close();
                    if(psdo != null) psdo.close();            
                }
            }
        } catch (Throwable e) {
            System.err.println("Query Error:" + e.getLocalizedMessage() + sQuery);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new Object[] { (Object)fileName, (Object)fileMimeType, (Object)fileContent };
    }
    
    static public String deleteDocument( Object tbl_wrk, Object params, Object clientData, Object freeParam ) {
        StringBuilder resultSet = new StringBuilder("{\"resultSet\":[");
        Connection conn = null;
        PreparedStatement psdo = null;
        String sQuery = null;
        String sWhere = "";
        int nRecs = 0;
        try {                
            JSONObject paramsJson = new JSONObject((String)params);
            JSONObject paramJson = paramsJson.getJSONObject("params");
            // { paramJson:..., schema:..., table:..., ids:..., index: ... };
            if(paramJson != null) {
                conn = app.liquid.dbx.connection.getDBConnection();
                if(conn != null) {
                    sQuery = "DELETE FROM "+dmsSchema+"."+dmsTable+" WHERE (id='"+paramJson.getString("index")+"')";
                    psdo = conn.prepareStatement(sQuery);
                    int res = psdo.executeUpdate();
                    if(res >= 0) {
                        String fieldSet = "{" + "\"id\":\""+(paramJson.getString("index")) + "}";
                        resultSet.append( (nRecs>0?",":"") + fieldSet);
                        nRecs++;
                    }
                    if(psdo != null) psdo.close();            
                }
            }
        } catch (Throwable e) {
            System.err.println("Query Error:" + e.getLocalizedMessage() + sQuery);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        resultSet.append("]}");
        return resultSet.toString();
    }
    
    static public String updateDocument( Object tbl_wrk, Object params, Object clientData, Object freeParam ) {
    	try {
        StringBuilder resultSet = new StringBuilder("{\"resultSet\":[");
        Connection conn = null;
        PreparedStatement psdo = null;
        String sQuery = null;
        String sWhere = "";
        int nRecs = 0;
        try {                
            JSONObject paramsJson = new JSONObject((String)params);
            JSONObject paramJson = paramsJson.getJSONObject("params");
            // { paramJson:..., schema:..., table:..., ids:..., index: ... };
            if(paramJson != null) {
                conn = app.liquid.dbx.connection.getDBConnection();
                if(conn != null) {
                    ArrayList<String> keyList = (ArrayList<String>)freeParam;
                    for(int ik=0; ik<keyList.size(); ik++) {
                        sQuery = "UPDATE "+dmsSchema+"."+dmsTable+" SET " +"note='"+paramJson.getString("note")+"'"+ " WHERE (id='"+paramJson.getString("index")+"')";
                        psdo = conn.prepareStatement(sQuery);
                        int res = psdo.executeUpdate();
                        if(res >= 0) {
                            String fieldSet = "{" + "\"id\":\""+(paramJson.getString("index")) + "}";
                            resultSet.append( (nRecs>0?",":"") + fieldSet);
                            nRecs++;
                        }
                        if(psdo != null) psdo.close();            
                    }
                }
            }
        } catch (Throwable e) {
            System.err.println("Query Error:" + e.getLocalizedMessage() + sQuery);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        resultSet.append("]}");
        return resultSet.toString();
    	} catch(Throwable th) {
    	}
        return null;    
    }   
}