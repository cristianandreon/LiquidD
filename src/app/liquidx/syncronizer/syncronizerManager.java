/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx.syncronizer;


import app.liquidx.sql.*;
import com.liquid.Callback;
import com.liquid.connection;
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
public class syncronizerManager {


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
                String sReport = "";
                
                // JSONObject sSQLSON = com.liquid.event.getJSONObject(params, "data", "sql");
                // String sSQL = sSQLSON.getString("sql");

                JSONObject executeSQLSON = com.liquid.event.getJSONObject(params, "data", "congirm");
                boolean bExecuteSQL = "true".equalsIgnoreCase( executeSQLSON.getString("confirm")) ? true : false;
                
                {

                    Callback.send("Syncronization ...");

                    {
                        {
                            ArrayList<Object> syncronizeBean = (ArrayList<Object>)db.load_beans((HttpServletRequest) requestParam, "syncronize_data", null, "*", "where 1=1", 0);
                            if (syncronizeBean != null) {
                                for(int im=0; im<syncronizeBean.size(); im++) {
                                    Object mBean = syncronizeBean.get(im);
                                    String machineId = (String)utility.get(mBean, "machine_id");
                                    String schema = (String)utility.get(mBean, "schema");
                                    String table = (String)utility.get(mBean, "table");

                                    Object machineBean = (Object)db.load_bean((HttpServletRequest) requestParam, "LiquidX.liquidx.machines", "*", machineId);

                                    String engine = (String)utility.get(machineBean, "engine");
                                    String ip = (String)utility.get(machineBean, "ip");
                                    String port = (String)utility.get(machineBean, "port");
                                    String database = (String)utility.get(machineBean, "database");
                                    String user = (String)utility.get(machineBean, "user");
                                    String password = (String)utility.get(machineBean, "password");
                                    String service = (String)utility.get(machineBean, "service");

                                    String targetMachineId = (String)utility.get(mBean, "target_machine_id");
                                    String targetSchema = (String)utility.get(mBean, "target_schema");
                                    String targetTable = (String)utility.get(mBean, "target_table");

                                    Object targetMachineBean = (Object)db.load_bean((HttpServletRequest) requestParam, "LiquidX.liquidx.machines", "*", targetMachineId);

                                    String targetEngine = (String)utility.get(targetMachineBean, "engine");
                                    String targetIp = (String)utility.get(targetMachineBean, "ip");
                                    String targetPort = (String)utility.get(targetMachineBean, "port");
                                    String targetDatabase = (String)utility.get(targetMachineBean, "database");
                                    String targetUser = (String)utility.get(targetMachineBean, "user");
                                    String targetPassword = (String)utility.get(targetMachineBean, "password");
                                    String targetService = (String)utility.get(targetMachineBean, "service");

                                    
                                            
                                    //
                                    // Connect
                                    //
                                    Connection sconn = null;
                                    Connection tconn = null;

                                    Callback.send("Connecting to " + ip + " ("+engine+") ...");
                                    System.out.println("Connecting to " + ip + " ("+engine+") ...");
                                    try {
                                        Object [] connResult = com.liquid.connection.getLiquidDBConnection(null, engine, ip, port, database, user, password, service);
                                        sconn = (Connection)connResult[0];
                                        String connError = (String)connResult[1];                                            
                                    } catch (Throwable th) {
                                        String err = "Error:" + th.getLocalizedMessage();
                                        System.out.println("Error connecting to " + ip + "("+engine+") : "+th.getLocalizedMessage());
                                        Callback.send("<span style=\"color:red\">Error connecting to " + ip + "("+engine+") : "+th.getLocalizedMessage()+"</span>");
                                        Thread.sleep(5000);
                                    }
                                    
                                    
                                    Callback.send("Connecting to " + targetIp + " ("+targetEngine+") ...");
                                    System.out.println("Connecting to " + targetIp + " ("+targetEngine+") ...");
                                    try {
                                        Object [] connResult = com.liquid.connection.getLiquidDBConnection(null, targetEngine, targetIp, targetPort, targetDatabase, targetUser, targetPassword, targetService);
                                        sconn = (Connection)connResult[0];
                                        String connError = (String)connResult[1];                                            
                                    } catch (Throwable th) {
                                        String err = "Error:" + th.getLocalizedMessage();
                                        System.out.println("Error connecting to " + ip + "("+engine+") : "+th.getLocalizedMessage());
                                        Callback.send("<span style=\"color:red\">Error connecting to " + ip + "("+engine+") : "+th.getLocalizedMessage()+"</span>");
                                        Thread.sleep(5000);
                                    }
                                                                        
                                    try {
                                        
                                        if(tconn != null) {
                                            tconn.setAutoCommit(false);
                                        }
                                    
 
                                        //
                                        //  Process the sql for the schema
                                        //
                                        if(schema != null && !schema.isEmpty()) {
                                            

                                            // execute sql
                                            if(bExecuteSQL) {
                                                if(sconn != null && tconn != null) {

                                                    if(!db.setSchema(sconn, engine, schema)) {
                                                        String msg = "Error setting schema '"+schema+"' on machine:"+ip+" engine:"+engine;
                                                        Callback.send("Process failed, <span style=\"color:red\">"+msg+"<span>");

                                                    } else {

                                                        if(!db.setSchema(tconn, targetEngine, targetSchema)) {
                                                            String msg = "Error setting schema '"+targetSchema+"' on machine:"+ip+" engine:"+engine;
                                                            Callback.send("Process failed, <span style=\"color:red\">"+msg+"<span>");
                                                            
                                                        } else {
                                                            
                                                            String syncRes = db.syncronizeTableMetadata( database+"."+schema+"."+table, targetDatabase+"."+targetSchema+"."+targetTable, sconn, tconn, "mirror");
                                                            
                                                            JSONObject syncJSON = new JSONObject(syncRes);
                                                                    
                                                            sReport += "<span style=\"font-size:20px\">"
                                                                    +"Deleting columns:"+syncJSON.getJSONArray("deletingColumns")
                                                                    +"Adding columns:"+syncJSON.getJSONArray("addingColumns")
                                                                    +"</span>";
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        
                                        if(sconn != null) {
                                            sconn.commit();
                                        }
                                        if(tconn != null) {
                                            tconn.commit();
                                        }
                                        
                                    } catch (Throwable th) {
                                        if(sconn != null) {
                                            sconn.rollback();
                                        }
                                        if(tconn != null) {
                                            tconn.rollback();
                                        }
                                    } finally {
                                        if(sconn != null) {
                                            sconn.close();
                                        }
                                        if(tconn != null) {
                                            tconn.close();
                                        }
                                    }                                    
                                }


                                String result = "<div>"
                                        +"<span style=\"font-size:30px\">"
                                        +"Report:"
                                        +sReport
                                        +"</span>"
                                        +"</br></br></br>"
                                        +"</div>"
                                        ;
                                return (Object) "{ \"client\":\"onSyncronizerExecuted\", \"result\":1, \"data\":\"" + utility.base64Encode(result) + "\" }";
                                
                            } else {
                                Callback.send("Process failed, <span style=\"color:red\">read machine bean error<span>");
                                return (Object) "{ \"result\":-2, \"error\":\"" + utility.base64Encode("read machine bean error") + "\" }";
                            }
                        }
                    }
                }
            } else {
                Callback.send("Syncronization failed, <span style=\"color:red\">params not defined<span>");
                return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode("primaryKey not found") + "\" }";
            }
        } catch (Throwable th) {
            String err = "Error:" + th.getLocalizedMessage();
            Callback.send("Syncronization failed, <span style=\"color:red\">" + err + "<span>");
            return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\" }";
        }
    }
}