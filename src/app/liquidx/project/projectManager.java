/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx.project;


import com.liquid.Callback;
import com.liquid.Messagebox;
import com.liquid.db;
import com.liquid.emailer;
import com.liquid.utility;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author root
 */
public class projectManager {

    static String glSourceFile = "";
    static String glTtargetFile = "";
    static long glFileSize = 0;
    static float maxSpeed = 0.0f;
    static float minSpeed = 0.0f;

    static public Object execute(Object tbl_wrk, Object params, Object clientData, Object requestParam) throws JSONException, InterruptedException, Exception {
        try {
            if (params != null) {
                // {"params":[{"formX":[{"1":"","2":"","3":""}]},{"name":"deploy"}]}
                // JSONArray rowsData = com.liquid.event.getJSONArray(params, "name");
                String projectId = null, cfgName = "";
                
                ArrayList<Object> fieldsBean = (ArrayList<Object>)db.load_beans((HttpServletRequest) requestParam, "fields", null, "*", null, 0);
                if (fieldsBean != null) {

                    Callback.send("Processing " + cfgName + " ...");

                    // Lettura del bean macchine
                    if (projectId != null && !projectId.isEmpty()) {
                        Object projectBean = db.load_bean((HttpServletRequest) requestParam, "LiquidX.liquidx.project", "*", projectId);
                        if (projectBean != null) {
                            ArrayList<Object>  machinesBean = (ArrayList<Object>)db.load_beans((HttpServletRequest) requestParam, "machines", null, "*", "where projectId="+projectId, 0);
                            if (machinesBean != null) {
                                for(int im=0; im<machinesBean.size(); im++) {
                                    Object machineBean = machinesBean.get(im);
                                    String machineId = (String)utility.get(machineBean, "machineId");
                                    ArrayList<Object>  schemasBean = (ArrayList<Object>)db.load_beans((HttpServletRequest) requestParam, "schemas", null, "*", "where machineId="+machineId, 0);

                                    String driver = (String)utility.get(machineBean, "driver");
                                    String host = (String)utility.get(machineBean, "host");
                                    String port = (String)utility.get(machineBean, "port");
                                    String database = (String)utility.get(machineBean, "database");
                                    String user = (String)utility.get(machineBean, "user");
                                    String password = (String)utility.get(machineBean, "password");

                                    // Connect                                        
                                    Connection conn = com.liquid.connection.getLiquidDBConnection(null, driver, host, port, database, user, password);
                                    

                                    if(conn != null) {
                                        //
                                        //  Process the sql for every schema
                                        //
                                        for(int is=0; is<schemasBean.size(); is++) {
                                            Object schemaBean = schemasBean.get(is);
                                            String schema = (String)utility.get(schemaBean, "name");

                                        
                                        
                                            // execute sql

                                            for(int iF=0; iF<fieldsBean.size(); iF++) {
                                                Object fieldBean = fieldsBean.get(iF);

                                            }
                                        }
                                    }
                                    
 
                                    for(int iF=0; iF<fieldsBean.size(); iF++) {
                                        Object fieldBean = fieldsBean.get(iF); 
                                        String fieldName = (String)utility.get(fieldBean, "field");
                                        String label = (String)utility.get(fieldBean, "label");
                                        String hibFieldName = nameSpacer.DB2Hibernate(fieldName);
                                        String controlType = "TEXTBOX"; // LISTBOX";
                                        String valuesList = ""; // "S=Si,N=No"
                                
                                        // Generate Hibernate .hbm
                                        String hbmCode = "" +
                                                "<property name=\""+hibFieldName+"\" type=\"java.lang.String\">"+
                                                "<column name=\""+fieldName+"\" length=\"100\" />"+
                                                "</property>"+
                                                "";

                                        // Generate Hibernate .java
                                        String javaCode = 
                                                "private String "+hibFieldName+";"+
                                                "public String getTelleg() {"+
                                                "   return this."+hibFieldName+";"+
                                                "}"+
                                                ""+
                                                "public void setTelleg(String telleg) {"+
                                                "this.telleg = "+hibFieldName+";"+
                                                "}"+
                                                "";

                                        // Generate zk panels .xml
                                        String zkCode = "" +
                                                "<!-- -->\n" +
                                                "<property name=\""+hibFieldName+"\">\n" +
						"<etichetta>"+label+"</etichetta>\n" +
						"<tipoControllo>"+controlType+"</tipoControllo>\n" +
						"<elencoValori>"+valuesList+"</elencoValori>\n" +
						"<posX>0</posX>\n" +
						"<posY>0</posY>\n" +
						"</property>"+
                                                "";
                    
                                        
                                        // Generale Liquid .json     
                                    }
                                }
                            }
                        }


                    } else {
                        Callback.send("Process of " + cfgName + "failed, <span style=\"color:red\">read bean error<span>");
                        return (Object) "{ \"result\":-2, \"error\":\"" + utility.base64Encode("read bean error") + "\" }";
                    }
                } else {
                    Callback.send("Process of " + cfgName + " failed, <span style=\"color:red\">primaryKey not found<span>");
                    return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode("primaryKey not found") + "\" }";
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