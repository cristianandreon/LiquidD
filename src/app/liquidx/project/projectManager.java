/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx.project;


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
                String exepts = null;
                String projectId = null;
                String allSQL = "";
                String allHBM = "";
                String allJAVA = "";
                String allJAVAVar = "";
                String allXML = "";
                String hibernateHBMFile = "";
                String hibernatJavaFile = "";
                String newLine = "<br/>";
                ArrayList<Object> fieldsBean = null;
                long selCount = db.getSelectionCount(tbl_wrk, params);
                if(selCount == 0) {
                    // all rows
                    fieldsBean = (ArrayList<Object>) db.load_beans((HttpServletRequest) requestParam, "fields", null, "*", null, 0);
                } else {
                    // selected rows
                    fieldsBean = (ArrayList<Object>) db.get_bean((HttpServletRequest)requestParam, db.getSelection(tbl_wrk, params), "bean", "*", 0);
                }
                
                JSONObject executeSQLSON = com.liquid.event.getJSONObject(params, "data", "executeSQL");
                boolean bExecuteSQL = "true".equalsIgnoreCase( executeSQLSON.getString("data")) ? true : false;
                
                if (fieldsBean != null) {
                    projectId = (String)utility.get(fieldsBean.get(0),"project_id");

                    Callback.send("Processing " + projectId + " ...");

                    // Lettura del bean macchine
                    if (projectId != null && !projectId.isEmpty()) {
                        Object projectBean = db.load_bean((HttpServletRequest) requestParam, "LiquidX.liquidx.projects", "*", projectId);
                        String folder = (String)utility.get(projectBean, "folder");
                        String project = (String)utility.get(projectBean, "name");
                        if (projectBean != null) {
                            ArrayList<Object>  machinesBean = (ArrayList<Object>)db.load_beans((HttpServletRequest) requestParam, "project_machine", null, "*", "where project_id='"+projectId+"'", 0);
                            if (machinesBean != null) {
                                for(int im=0; im<machinesBean.size(); im++) {
                                    Object mBean = machinesBean.get(im);
                                    String machineId = (String)utility.get(mBean, "machine_id");

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
                                        try {
                                            Object [] connResult = com.liquid.connection.getLiquidDBConnection(null, engine, ip, port, database, user, password, service);
                                            conn = (Connection)connResult[0];
                                            String connError = (String)connResult[1];
                                        } catch (Throwable th) {
                                            String err = "Error:" + th.getLocalizedMessage();
                                            Callback.send("<span style=\"color:red\">Error connectiong to " + ip + "("+engine+") : "+th.getLocalizedMessage()+"</span>");
                                            Thread.sleep(5000);
                                        }
                                    }
                                    
                                    try {
                                        
                                        if(conn != null) {
                                            conn.setAutoCommit(false);
                                        }
                                    
 
                                        //
                                        //  Process the sql for every schema
                                        //
                                        ArrayList<Object>  schemasBean = (ArrayList<Object>)db.load_beans((HttpServletRequest) requestParam, "machine_schema", null, "*", "where machine_id='"+machineId+"' and project_id='"+projectId+"'", 0);
                                        for(int is=0; is<schemasBean.size(); is++) {
                                            Object schemaBean = schemasBean.get(is);
                                            String schema = (String)utility.get(schemaBean, "schema");

                                            allSQL += newLine;
                                            allSQL += newLine;
                                            allSQL += "<span style=\"font-size:22px\">"+"-- Schema <b>"+schema+"</b></span>";
                                            allSQL += "<span style=\"font-size:15px\">"+" - Machine "+ip+" ("+engine+")</span>";                                            
                                            allSQL += newLine;

                                            for(int iF=0; iF<fieldsBean.size(); iF++) {
                                                Object fieldBean = fieldsBean.get(iF); 
                                                String fieldTable = (String)utility.get(fieldBean, "table");
                                                String fieldName = (String)utility.get(fieldBean, "field");
                                                String fieldType = (String)utility.get(fieldBean, "type");
                                                String fieldSize = (String)utility.get(fieldBean, "size");
                                                String fieldNullable = (String)utility.get(fieldBean, "nullable");
                                                String fieldDefault = (String)utility.get(fieldBean, "def");
                                                String fieldRemarks = (String)utility.get(fieldBean, "remarks");
                                                String fieldAutoincrement = null;
                                                String label = (String)utility.get(fieldBean, "label");
                                                String hibFieldName = nameSpacer.DB2Hibernate(fieldName);
                                                String controlType = "TEXTBOX"; // LISTBOX";
                                                String valuesList = ""; // "S=Si,N=No"
                                                int ifieldSize = 0;
                                                String whCode = "";
                                                String htCode = "";

                                                
                                                try {
                                                    ifieldSize = Integer.parseInt(fieldSize);
                                                } catch(Exception e) {}
                                                        
                                                if("DATE".equalsIgnoreCase(fieldType)) {
                                                    controlType = "DATEBOX";
                                                } else if("NUMBER".equalsIgnoreCase(fieldType)) {
                                                    controlType = "NMERICBOX";
                                                } else {
                                                    if(ifieldSize >= 2000) {
                                                        controlType = "TEXTAREA";
                                                        whCode = "<widthControllo>400px</widthControllo>";
                                                        htCode = "<heightControllo>200px</heightControllo>";
                                                    } else if(ifieldSize == 1) {
                                                        valuesList = "S=Si,N=No";
                                                    }
                                                }



                                                String sqlCode = com.liquid.metadata.getAddColumnSQL(engine, database, schema, fieldTable, fieldName, fieldType, fieldSize, fieldNullable, fieldAutoincrement, fieldDefault, fieldRemarks);

                                                // execute sql
                                                if(bExecuteSQL) {
                                                    if(conn != null) {
                                                        try {
                                                            Statement stmt = conn.createStatement();
                                                            boolean res = stmt.execute(sqlCode);
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
                                                        } catch (Exception ex) {
                                                            exepts += "[ SQL:"+sqlCode+"<br/>Error:"+ex.getMessage()+"]";
                                                        }
                                                    }
                                                }
                                                
                                                if(sqlCode != null && !sqlCode.isEmpty()) {
                                                    allSQL += newLine+newLine;
                                                    allSQL += sqlCode.replace("\n", newLine);
                                                    if(conn != null) {
                                                    }
                                                }


                                                if(is == 0 && im == 0) { // only at first cycle

                                                    hibernateHBMFile = nameSpacer.DB2Hibernate(fieldTable) + ".hbm.xml";
                                                    hibernatJavaFile = nameSpacer.DB2Hibernate(fieldTable) + ".java";

                                                    //
                                                    // Generate Hibernate .hbm
                                                    //
                                                    
                                                    String sType = "java.lang.String";
                                                    String sLen = null;
                                                    
                                                    if("Timestamp".equalsIgnoreCase(fieldType)) {
                                                        sType = "java.sql.Timestamp";
                                                        sLen = "7";
                                                    } else if("Date".equalsIgnoreCase(fieldType)) {
                                                        sType = "java.sql.Date";
                                                        sLen = "7";
                                                    } else {
                                                        sLen = fieldSize;
                                                    }
                                                    
                                                            
                                                    String hbmCode = ""
                                                            +utility.htmlEncode("<property name=\""+hibFieldName+"\" type=\""+sType+"\">", true)+newLine
                                                            +utility.htmlEncode("<column name=\""+fieldName+"\" "+(sLen != null ? "length=\""+sLen+"\"" : "")+" />", true)+newLine
                                                            +utility.htmlEncode("</property>", true)+newLine
                                                            +"";

                                                    allHBM += newLine+newLine;
                                                    allHBM += utility.htmlEncode("<!-- "+fieldName+" / " + label + " -->", true) + newLine;
                                                    allHBM += hbmCode;

                                                    //
                                                    // Generate Hibernate .java
                                                    //
                                                    String javaVarCode = "private String "+hibFieldName+";"+newLine;
                                                    allJAVAVar += javaVarCode;

                                                    String getSethibFieldName = hibFieldName.substring(0, 1).toLowerCase()  + hibFieldName.substring(1, -1);
                                                    String javaCode = 
                                                            "public String get"+getSethibFieldName+"() {"+newLine
                                                            +"\treturn this."+hibFieldName+";"+newLine
                                                            +"}"+newLine
                                                            +""+newLine
                                                            +"public void set"+getSethibFieldName+"(String "+hibFieldName+") {"+newLine
                                                            +"\tthis."+hibFieldName+" = "+hibFieldName+";"+newLine
                                                            +"}"+newLine
                                                            +""+newLine;

                                                    allJAVA += newLine+newLine;
                                                    allJAVA += "// "+fieldName+" / " + label+newLine;
                                                    allJAVA += javaCode;


                                                    //
                                                    // Generate zk panels .xml
                                                    //
                                                    String zkCode = "" +
                                                            utility.htmlEncode("<!-- "+fieldName+" / " + label+" / "+hibFieldName+" -->", true) +newLine+
                                                            utility.htmlEncode("<property name=\""+hibFieldName+"\">", true) +newLine+
                                                            utility.htmlEncode("<etichetta>"+label+"</etichetta>", true) +newLine+
                                                            utility.htmlEncode("<tipoControllo>"+controlType+"</tipoControllo>", true) +newLine+
                                                            (whCode != null ? utility.htmlEncode(whCode) +newLine : "")+
                                                            (htCode != null ? utility.htmlEncode(htCode) +newLine : "")+
                                                            utility.htmlEncode("<elencoValori>"+valuesList+"</elencoValori>", true) +newLine+
                                                            utility.htmlEncode("<posX>0</posX>", true) +newLine+
                                                            utility.htmlEncode("<posY>0</posY>", true) +newLine+
                                                            utility.htmlEncode("</property>", true) +newLine+
                                                            "";

                                                    allXML += newLine+newLine;
                                                    allXML += zkCode;

                                                    // Generale Liquid .json     
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

                                String package_hib = "com.geisoft."+project.toLowerCase()+".hibernate.conf".replace(".", "/");
                                String package_vm = "com.geisoft."+project.toLowerCase()+".model".replace(".", "/");

                                String result = "<div>"
                                        +"<span style=\"font-size:30px\">"+"SQL"+"</span>"
                                        +allSQL
                                        +"</br></br></br>"
                                        +"<span style=\"font-size:30px\">"+"Hibernate .HBM<br/><span style=\"font-size:17px; padding-left:60px\"> "+folder+"/"+package_hib+"/"+hibernateHBMFile+"</span></span>"
                                        +allHBM
                                        +"</br></br></br>"
                                        +"<span style=\"font-size:30px\">"+"Hibernate .JAVA var<br/><span style=\"font-size:17px; padding-left:60px\"> "+folder+"/"+package_vm+"/"+hibernatJavaFile+"</span></span>"
                                        +newLine+newLine
                                        +allJAVAVar
                                        +"</br></br></br>"
                                        +"<span style=\"font-size:30px\">"+"Hibernate .JAVA getter/setter<br/><span style=\"font-size:17px; padding-left:60px\"> "+folder+"/"+package_vm+"/"+hibernatJavaFile+"</span></span>"
                                        +allJAVA
                                        +"</br></br></br>"
                                        +"<span style=\"font-size:20px\">"+"ZK Panel .XML"+"</span>"
                                        +allXML
                                        + ( exepts != null ? "</br></br></br>" +  "<span style=\"font-size:20px; color:darkRed\">"+"Exceptions"+"</span>"+exepts : "" )
                                        +"</div>"
                                        ;
                                return (Object) "{ \"client\":\"onExecuted\", \"result\":1, \"data\":\"" + utility.base64Encode(result) + "\" }";
                                
                            } else {
                                Callback.send("Process of " + projectId + "failed, <span style=\"color:red\">read machine bean error<span>");
                                return (Object) "{ \"result\":-2, \"error\":\"" + utility.base64Encode("read machine bean error") + "\" }";
                            }
                        } else {
                            Callback.send("Process of " + projectId + "failed, <span style=\"color:red\">read project bean error<span>");
                            return (Object) "{ \"result\":-2, \"error\":\"" + utility.base64Encode("read project bean error") + "\" }";
                        }
                    } else {
                        Callback.send("Process of " + projectId + "failed, <span style=\"color:red\">read bean error<span>");
                        return (Object) "{ \"result\":-2, \"error\":\"" + utility.base64Encode("read bean error") + "\" }";
                    }
                } else {
                    Callback.send("Process of " + projectId + " failed, <span style=\"color:red\">primaryKey not found<span>");
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
    }
}