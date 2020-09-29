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
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;

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
                String allSQL = "";
                String allHBM = "";
                String allJAVA = "";
                String allJAVAVar = "";
                String allXML = "";
                
                
                ArrayList<Object> fieldsBean = (ArrayList<Object>)db.load_beans((HttpServletRequest) requestParam, "fields", null, "*", null, 0);
                if (fieldsBean != null) {
                    projectId = (String)utility.get(fieldsBean.get(0),"project_id");

                    Callback.send("Processing " + cfgName + " ...");

                    // Lettura del bean macchine
                    if (projectId != null && !projectId.isEmpty()) {
                        Object projectBean = db.load_bean((HttpServletRequest) requestParam, "LiquidX.liquidx.projects", "*", projectId);
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

                                    
                                    //
                                    // Connect
                                    //
                                    Connection conn = null;
                                    try {
                                        conn = com.liquid.connection.getLiquidDBConnection(null, engine, ip, port, database, user, password);
                                    } catch (Throwable th) {
                                        String err = "Error:" + th.getLocalizedMessage();
                                    }
                                    
 
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
                                        String newLine = "<br/>";
                                
                                        

                                        //
                                        //  Process the sql for every schema
                                        //
                                        ArrayList<Object>  schemasBean = (ArrayList<Object>)db.load_beans((HttpServletRequest) requestParam, "machine_schema", null, "*", "where machine_id='"+machineId+"'", 0);
                                        for(int is=0; is<schemasBean.size(); is++) {
                                            Object schemaBean = schemasBean.get(is);
                                            String schema = (String)utility.get(schemaBean, "name");

                                            String sqlCode = com.liquid.metadata.getAddColumnSQL(engine, database, schema, fieldTable, fieldName, fieldType, fieldSize, fieldNullable, fieldAutoincrement, fieldDefault, fieldRemarks);

                                            // execute sql
                                            if(sqlCode != null && !sqlCode.isEmpty()) {
                                                allSQL += newLine+newLine;
                                                allSQL += sqlCode.replace("\n", newLine);
                                                if(conn != null) {
                                                }
                                            }
                                        }
                                        
                                        //
                                        // Generate Hibernate .hbm
                                        //
                                        String hbmCode = ""
                                                +utility.htmlEncode("<property name=\""+hibFieldName+"\" type=\"java.lang.String\">", true)+newLine
                                                +utility.htmlEncode("<column name=\""+fieldName+"\" length=\"100\" />", true)+newLine
                                                +utility.htmlEncode("</property>", true)+newLine
                                                +"";
                                        allHBM += newLine+newLine;
                                        allHBM += "// "+fieldName+" / " + label+newLine;
                                        allHBM += hbmCode;

                                        //
                                        // Generate Hibernate .java
                                        //
                                        String javaVarCode = 
                                                "private String "+hibFieldName+";"+newLine;
                                        // allJAVAVar += "<br/><br/>";
                                        // allJAVAVar += "// "+fieldName+" / " + label;
                                        allJAVAVar += javaVarCode;

                                        String javaCode = 
                                                "private String "+hibFieldName+";"+newLine
                                                +"public String getTelleg() {"+newLine
                                                +"   return this."+hibFieldName+";"+newLine
                                                +"}"+newLine
                                                +""+newLine
                                                +"public void setTelleg(String telleg) {"+newLine
                                                +"this.telleg = "+hibFieldName+";"+newLine
                                                +"}"+newLine
                                                +""+newLine;
                                        allJAVA += "<br/><br/>";
                                        allJAVA += "// "+fieldName+" / " + label+newLine;
                                        allJAVA += javaCode;

                                        
                                        //
                                        // Generate zk panels .xml
                                        //
                                        String zkCode = "" +
                                                utility.htmlEncode("<!-- "+hibFieldName+" -->", true) +newLine+
                                                utility.htmlEncode("<property name=\""+hibFieldName+"\">", true) +newLine+
						utility.htmlEncode("<etichetta>"+label+"</etichetta>", true) +newLine+
						utility.htmlEncode("<tipoControllo>"+controlType+"</tipoControllo>", true) +newLine+
						utility.htmlEncode("<elencoValori>"+valuesList+"</elencoValori>", true) +newLine+
						utility.htmlEncode("<posX>0</posX>", true) +newLine+
						utility.htmlEncode("<posY>0</posY>", true) +newLine+
						utility.htmlEncode("</property>", true) +newLine+
                                                "";
                                        allXML += "<br/><br/>";
                                        allXML += "// "+fieldName+" / " + label;
                                        allXML += zkCode;
                                        
                                        // Generale Liquid .json     
                                    }
                                }
                                
                                String result = "<div>"
                                        +"<span style=\"font-size:20px\">"+"SQL"+"</span>"
                                        +allSQL
                                        +"</br></br></br>"
                                        +"<span style=\"font-size:20px\">"+"Hibernate .HBM"+"</span>"
                                        +allHBM
                                        +"</br></br></br>"
                                        +"<span style=\"font-size:20px\">"+"Hibernat .JAVA var"+"</span>"
                                        +allJAVAVar
                                        +"</br></br></br>"
                                        +"<span style=\"font-size:20px\">"+"Hibernat .JAVA getter/setter"+"</span>"
                                        +allJAVA
                                        +"</br></br></br>"
                                        +"<span style=\"font-size:20px\">"+"ZK Panel .XMLL"+"</span>"
                                        +allXML
                                        +"</div>"
                                        ;
                                return (Object) "{ \"client\":\"onExecuted\", \"result\":1, \"data\":\"" + utility.base64Encode(result) + "\" }";
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