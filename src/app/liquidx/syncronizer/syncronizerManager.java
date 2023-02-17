/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx.syncronizer;


import app.liquidx.sql.*;
import com.liquid.Callback;
import com.liquid.Messagebox;
import com.liquid.bean;
import com.liquid.connection;
import com.liquid.db;
import com.liquid.metadata;
import com.liquid.utility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
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

                JSONObject previewSyncronizerSON = com.liquid.event.getJSONObject(params, "form", "previewSyncronizer");
                boolean bPreviewSyncronizer = "true".equalsIgnoreCase( previewSyncronizerSON.getString("data")) ? true : false;
                
                
                JSONObject deepModeSON = com.liquid.event.getJSONObject(params, "form", "deepMode");
                boolean bDeepMode = "true".equalsIgnoreCase( deepModeSON.getString("data")) ? true : false;
                
                {

                    Callback.send("Syncronization ...");

                    {
                        {
                            ArrayList<Object> syncronizeBeans = null;
                            
                            long selCount = db.getSelectionCount(tbl_wrk, params);
                            if(selCount == 0) {
                                // all rows
                                syncronizeBeans = (ArrayList<Object>)bean.load_beans((HttpServletRequest) requestParam, "syncronizer_data", null, "*", "where 1=1", 0);
                            } else {
                                // selected rows
                                syncronizeBeans = (ArrayList<Object>) bean.get_bean((HttpServletRequest) requestParam, db.getSelection(tbl_wrk, params), "bean", "*", 0);
                            }

                
                            if (syncronizeBeans != null) {
                                for(int im=0; im<syncronizeBeans.size(); im++) {
                                    Object mBean = syncronizeBeans.get(im);
                                    String machine = (String)utility.get(mBean, "machine");
                                    String machineId = (String)utility.get(mBean, "machine_id");
                                    String schema = (String)utility.get(mBean, "schema");
                                    String table = (String)utility.get(mBean, "table");

                                    Object machineBean = (Object)bean.load_bean((HttpServletRequest) requestParam, "LiquidX.liquidx.syncronizer_machines", "*", new StringBuffer(machineId));

                                    String engine = (String)utility.get(machineBean, "engine");
                                    String ip = (String)utility.get(machineBean, "ip");
                                    String port = (String)utility.get(machineBean, "port");
                                    String database = (String)utility.get(machineBean, "database");
                                    String user = (String)utility.get(machineBean, "user");
                                    String password = (String)utility.get(machineBean, "password");
                                    String service = (String)utility.get(machineBean, "service");

                                    String targetMachine = (String)utility.get(mBean, "target_machine");
                                    String targetMachineId = (String)utility.get(mBean, "target_machine_id");
                                    String targetSchema = (String)utility.get(mBean, "target_schema");
                                    String targetTable = (String)utility.get(mBean, "target_table");

                                    Object targetMachineBean = (Object)bean.load_bean((HttpServletRequest) requestParam, "LiquidX.liquidx.syncronizer_machines", "*", new StringBuffer(targetMachineId));

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

                                    Callback.send("Connecting to " + ip + " ("+engine+" @ "+machine+") ...");
                                    System.out.println("Connecting to " + ip + " ("+engine+" @ "+machine+") ...");
                                    try {
                                        Object [] connResult = com.liquid.connection.getLiquidDBConnection(null, engine, ip, port, database, user, password, service);
                                        sconn = (Connection)connResult[0];
                                        String connError = (String)connResult[1];                                            
                                        if(sconn != null) {
                                            sconn.setAutoCommit(false);
                                        } else {
                                            System.out.println("Error connecting to " + ip + "("+engine+") : "+connError);
                                            Callback.send("<span style=\"color:red\">Error connecting to " + ip + "("+engine+") : "+connError+"</span>");                                            
                                        }
                                    } catch (Throwable th) {
                                        String err = "Error:" + th.getLocalizedMessage();
                                        sReport += "Error connection to source .. " + err;
                                        System.out.println("Error connecting to " + ip + "("+engine+") : "+th.getLocalizedMessage());
                                        Callback.send("<span style=\"color:red\">Error connecting to " + ip + "("+engine+") : "+th.getLocalizedMessage()+"</span>");
                                        Thread.sleep(5000);
                                    }
                                    
                                    
                                    Callback.send("Connecting to " + targetIp + " ("+targetEngine+" @ "+targetMachine+") ...");
                                    System.out.println("Connecting to " + targetIp + " ("+targetEngine+" @ "+targetMachine+") ...");
                                    try {
                                        Object [] connResult = com.liquid.connection.getLiquidDBConnection(null, targetEngine, targetIp, targetPort, targetDatabase, targetUser, targetPassword, targetService);
                                        tconn = (Connection)connResult[0];
                                        String connError = (String)connResult[1];                                            
                                        if(tconn != null) {
                                            tconn.setAutoCommit(false);
                                        } else {
                                            System.out.println("Error connecting to " + targetIp + "("+targetEngine+") : "+connError);
                                            Callback.send("<span style=\"color:red\">Error connecting to " + targetIp + "("+targetEngine+") : "+connError+"</span>");                                            
                                        }
                                    } catch (Throwable th) {
                                        String err = "Error:" + th.getLocalizedMessage();
                                        sReport += "Error connection to target .. " + err;
                                        System.out.println("Error connecting to " + targetIp + "("+targetEngine+") : "+th.getLocalizedMessage());
                                        Callback.send("<span style=\"color:red\">Error connecting to " + targetIp + "("+targetEngine+") : "+th.getLocalizedMessage()+"</span>");
                                        Thread.sleep(5000);
                                    }
                                                                        
                                    try {
                                        
                                        //
                                        //  Process the sql for the schema
                                        //
                                        if(schema != null && !schema.isEmpty()) {
                                            

                                            // execute sql
                                            {
                                                if(sconn != null && tconn != null) {

                                                    Callback.send("setting schema to " + schema + "...");
                                                    
                                                    if(!db.setSchema(sconn, engine, schema)) {
                                                        String msg = "Error setting schema '"+schema+"' on machine:"+ip+" engine:"+engine;
                                                        Callback.send("Process failed, <span style=\"color:red\">"+msg+"<span>");
                                                        return (Object) "{ \"client\":\"onSyncronizerExecuted\", \"result\":1, \"error\":\"" + utility.base64Encode(msg) + "\" }";

                                                    } else {

                                                        Callback.send("setting schema to " + targetSchema + "...");
                                                        
                                                        if(!db.setSchema(tconn, targetEngine, targetSchema)) {
                                                            String msg = "Error setting schema '"+targetSchema+"' on machine:"+ip+" engine:"+engine;
                                                            Callback.send("Process failed, <span style=\"color:red\">"+msg+"<span>");
                                                            return (Object) "{ \"client\":\"onSyncronizerExecuted\", \"result\":1, \"error\":\"" + utility.base64Encode(msg) + "\" }";
                                                            
                                                        } else {
                                                            int iTable, nTables = 0;
                                                            ArrayList<String> tables = null, targetTables = null;
                                                            
                                                            if(table.indexOf("*") != -1) {
                                                                Callback.send("Reading multiple tables <span style=\"color:darkred\">"+table+"<span>");
                                                                Object[] result = metadata.getAllTables(database, schema, table, null, sconn, false);
                                                                if(result != null) {
                                                                    if(result[0] != null) {
                                                                        JSONObject tablesJSON = new JSONObject("{\"tables\":"+(String)result[0]+"}");
                                                                        if(tablesJSON != null) {
                                                                            tables = new ArrayList<String>();
                                                                            targetTables = new ArrayList<String>();
                                                                            JSONArray tablesJSONarray = tablesJSON.getJSONArray("tables");
                                                                            String patternString = utility.createRegexFromGlob(table);
                                                                            for (int ct = 0; ct < tablesJSONarray.length(); ct++) {
                                                                                table = tablesJSONarray.getJSONObject(ct).getString("TABLE");
                                                                                if(table.matches(patternString)) {
                                                                                    tables.add(table);
                                                                                    targetTables.add(table);
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                tables = new ArrayList<String>();
                                                                tables.add(table);
                                                                targetTables = new ArrayList<String>();
                                                                targetTables.add(targetTable != null && !targetTable.isEmpty() ? targetTable : table);
                                                            }
                                                            
                                                            nTables = tables.size();
                                                            if(nTables > 0) {
                                                                for(iTable=0; iTable<nTables; iTable++) {
                                                                    table = tables.get(iTable);
                                                                    targetTable = targetTables.get(iTable);
                                                                    
                                                                    boolean proceed = true;

                                                                    if(!table.equalsIgnoreCase(targetTable)) {
                                                                        String message = " Table name mismatch</br>"
                                                                                + "</br>"
                                                                                + "</br>"
                                                                                + "<span style=\"font-size:110%; left:50px; position: relative;\">"
                                                                                +" Compare table <b>" + table + "</b> with <b>"+targetTable+"</b></br>"
                                                                                + "</br>"
                                                                                + "</span>";
                                                                        if (Messagebox.show(message, "LiquidD", Messagebox.WARNING + Messagebox.YES + Messagebox.NO) == Messagebox.YES) {
                                                                        } else {
                                                                            proceed = false;
                                                                        }
                                                                    
                                                                    }
                                                                    
                                                                    if(proceed) {
                                                                    
                                                                        Callback.send("Resetting metadata cache...");
                                                                        metadata.resetTableMetadata(database, schema, table);
                                                                        metadata.resetTableMetadata(targetDatabase, targetSchema, targetTable);

                                                                        Callback.send("analyzing table " +(iTable+1)+"/"+nTables+" " + table + "...");

                                                                        String syncRes = db.syncronizeTableMetadata( 
                                                                                database+"."+schema+"."+table, targetDatabase+"."+targetSchema+"."+targetTable, 
                                                                                sconn, tconn, 
                                                                                (bPreviewSyncronizer ? "preview" : "mirror") + (bDeepMode ? " deepMode" : "") + " callback"
                                                                        );

                                                                        JSONObject syncJSON = new JSONObject(syncRes);

                                                                        String preview = syncJSON.has("preview") ? utility.base64Decode(syncJSON.getString("preview")) : "";

                                                                        String error = syncJSON.has("error") ? utility.base64Decode(syncJSON.getString("error")) : "";
                                                                        
                                                                        if(error != null && !error.isEmpty()) {
                                                                            
                                                                        sReport += 
                                                                                "<span style=\"font-size:20px; color:red\">"
                                                                                + error
                                                                                +"</span>"
                                                                                +"<br/>"
                                                                                ;
                                                                        }

                                                                        
                                                                        sReport += 
                                                                                "<span style=\"font-size:20px\">"
                                                                                +"From <b>"+database+"."+schema+"."+table+"@"+ip+"</b>"+"<br/>to <b>"+database+"."+targetSchema+"."+targetTable+"@"+targetIp+"</b>"
                                                                                +"<br/>"
                                                                                +"<br/>"
                                                                                +"<span style=\"font-size:17px\">"
                                                                                + (preview != null && preview.length() > 0 ? "<span style=\"color:darkGray\">"+preview.replace("\n", "<br/>")+"</span>" : "")
                                                                                +"</span>"
                                                                                +"<br/>"
                                                                                +"<span style=\"font-size:15px\">"
                                                                                + (syncJSON.getJSONArray("deletingColumns").length() > 0 ? "<span style=\"color:darkRed\">"+"Deleting columns from "+targetIp+" ("+syncJSON.getJSONArray("deletingColumns").length()+") : "+syncJSON.getJSONArray("deletingColumns")+"</span>" : "<span style=\"color:darkGreen\">"+"No missing column in "+ip+"</span>")
                                                                                +"<br/>"
                                                                                +"<br/>"
                                                                                + (syncJSON.getJSONArray("addingColumns").length() > 0 ? "<span style=\"color:darkRed\">"+"Missing columns in "+targetIp+" ("+syncJSON.getJSONArray("addingColumns").length()+") : "+syncJSON.getJSONArray("addingColumns")+"</span>" : "<span style=\"color:darkGreen\">"+"No missing columns in "+targetIp+"</span>")
                                                                                +"</span>"
                                                                                +"<br/>"
                                                                                +"<br/>"
                                                                                +"<br/>"
                                                                                ;
                                                                    }
                                                                }
                                                            } else {
                                                                Callback.send("No table to process...");
                                                            }
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
                                        sReport += "internal error:"+th.getMessage();
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

                                Callback.send("Done...");

                                String result = "<div>"
                                        +"<span style=\"font-size:30px\">"
                                        +"Report:"
                                        +"<br/>"
                                        +"<br/>"
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