/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx.sql;


import com.liquid.Callback;
import com.liquid.db;
import com.liquid.utility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author root
 */
public class sqlExecuter {

    static String glSourceFile = "";
    static String glTtargetFile = "";
    static long glFileSize = 0;
    static float maxSpeed = 0.0f;
    static float minSpeed = 0.0f;

    static public Object execute(Object tbl_wrk, Object params, Object clientData, Object requestParam) throws JSONException, InterruptedException, Exception {
        try {
            if (params != null) {
                // "{"params":[{"form":"formSQL","data":{"sql":"aaa","confirm":"true"}}]}"
                // JSONArray rowsData = com.liquid.event.getJSONArray(params, "name");
                String allSQL = "";
                String allHBM = "";
                String allJAVA = "";
                String allJAVAVar = "";
                String allXML = "";
                String hibernateHBMFile = "";
                String hibernatJavaFile = "";
                String newLine = "<br/>";
                
                JSONObject sSQLSON = com.liquid.event.getJSONObject(params, "data", "sql");
                String sSQL = sSQLSON.getString("sql");

                JSONObject executeSQLSON = com.liquid.event.getJSONObject(params, "data", "congirm");
                boolean bExecuteSQL = "true".equalsIgnoreCase( executeSQLSON.getString("confirm")) ? true : false;
                
                if (sSQLSON != null) {

                    Callback.send("Processing sql ...");

                    {
                        {
                            ArrayList<Object> machinesBean = (ArrayList<Object>)db.load_beans((HttpServletRequest) requestParam, "sql_machine_schema", null, "*", "where 1=1", 0);
                            if (machinesBean != null) {
                                for(int im=0; im<machinesBean.size(); im++) {
                                    Object mBean = machinesBean.get(im);
                                    String machineId = (String)utility.get(mBean, "machine_id");
                                    String schema = (String)utility.get(mBean, "schema");

                                    Object machineBean = (Object)db.load_bean((HttpServletRequest) requestParam, "LiquidX.liquidx.machines", "*", machineId);

                                    String engine = (String)utility.get(machineBean, "engine");
                                    String ip = (String)utility.get(machineBean, "ip");
                                    String port = (String)utility.get(machineBean, "port");
                                    String database = (String)utility.get(machineBean, "database");
                                    String user = (String)utility.get(machineBean, "user");
                                    String password = (String)utility.get(machineBean, "password");
                                    String service = (String)utility.get(machineBean, "service");


                                            
                                    //
                                    // Connect
                                    //
                                    Connection conn = null;

                                    if(bExecuteSQL) {
                                        Callback.send("Connectiong to " + ip + " ("+engine+") ...");
                                        System.out.println("Connecting to " + ip + " ("+engine+") ...");
                                        try {
                                            Object [] connResult = com.liquid.connection.getLiquidDBConnection(null, engine, ip, port, database, user, password, service);
                                            conn = (Connection)connResult[0];
                                            String connError = (String)connResult[1];                                            
                                        } catch (Throwable th) {
                                            String err = "Error:" + th.getLocalizedMessage();
                                            System.out.println("Error connectiong to " + ip + "("+engine+") : "+th.getLocalizedMessage());
                                            Callback.send("<span style=\"color:red\">Error  to " + ip + "("+engine+") : "+th.getLocalizedMessage()+"</span>");
                                            Thread.sleep(5000);
                                        }
                                    }
                                    
                                    try {
                                        
                                        if(conn != null) {
                                            conn.setAutoCommit(false);
                                        }
                                    
 
                                        //
                                        //  Process the sql for the schema
                                        //
                                        if(schema != null && !schema.isEmpty()) {

                                            allSQL += newLine;
                                            allSQL += newLine;
                                            allSQL += "<span style=\"font-size:22px\">"+"-- Schema <b>"+schema+"</b></span>";
                                            allSQL += "<span style=\"font-size:15px\">"+" - Machine "+ip+" ("+engine+")</span>";                                            
                                            allSQL += newLine;
                                            
                                           
                                            

                                            {
                                                // execute sql
                                                if(bExecuteSQL) {
                                                    if(conn != null) {
                                                        
                                                        if(!db.setSchema(conn, engine, schema)) {
                                                            String msg = "Error setting schema '"+schema+"' on machine:"+ip+" engine:"+engine;
                                                            Callback.send("Process failed, <span style=\"color:red\">"+msg+"<span>");
                                                            allSQL += "<span style=\"font-size:22px; color:red\">"+"***<b>"+msg+"</b></span>";
                                                            
                                                        } else {
                                                        
                                                            Statement stmt = conn.createStatement();
                                                            boolean res = stmt.execute(sSQL);
                                                            if(!res) {
                                                                ResultSet rs = stmt.getResultSet();
                                                                if(rs != null) {
                                                                    if(rs.next()) {
                                                                        String result = rs.getString(1);
                                                                        if(result != null) {
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        
                                        if(conn != null) {
                                            conn.commit();
                                        }
                                    } catch (Throwable th) {
                                        if(conn != null) {
                                            conn.rollback();
                                        }
                                    } finally {
                                        if(conn != null) {
                                            conn.close();
                                        }
                                    }                                    
                                }


                                String result = "<div>"
                                        +"<span style=\"font-size:30px\">"
                                        +"Report:"
                                        +"</span>"
                                        +"</br></br></br>"
                                        +"</div>"
                                        ;
                                return (Object) "{ \"client\":\"onExecuted\", \"result\":1, \"data\":\"" + utility.base64Encode(result) + "\" }";
                                
                            } else {
                                Callback.send("Process failed, <span style=\"color:red\">read machine bean error<span>");
                                return (Object) "{ \"result\":-2, \"error\":\"" + utility.base64Encode("read machine bean error") + "\" }";
                            }
                        }
                    }
                }
            } else {
                Callback.send("Process failed, <span style=\"color:red\">params not defined<span>");
                return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode("primaryKey not found") + "\" }";
            }
        } catch (Throwable th) {
            String err = "Error:" + th.getLocalizedMessage();
            Callback.send("Deploy failed, <span style=\"color:red\">" + err + "<span>");
            return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\" }";
        }
        
        return null;
    }
}