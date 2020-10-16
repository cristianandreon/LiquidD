/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx.importer;

import app.liquidx.project.nameSpacer;
import com.liquid.Callback;
import com.liquid.db;
import com.liquid.utility;
import com.liquid.workspace;

import java.sql.Connection;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author root
 */
public class importerManager {

    static String glSourceFile = "";
    static String glTtargetFile = "";
    static long glFileSize = 0;
    static float maxSpeed = 0.0f;
    static float minSpeed = 0.0f;

    static public Object execute(Object tbl_wrk, Object params, Object clientData, Object requestParam) throws JSONException, InterruptedException, Exception, Throwable {
        try {
            if (params != null && tbl_wrk != null) {
                // {"params":[{"formX":[{"1":"","2":"","3":""}]},{"name":"deploy"}]}
                // JSONArray rowsData = com.liquid.event.getJSONArray(params, "name");
                Integer projectId = null;
                ArrayList<Object> importsBean = null;
                long selCount = db.getSelectionCount(tbl_wrk, params);
                if (selCount == 0) {
                    // all rows
                    importsBean = (ArrayList<Object>) db.load_beans((HttpServletRequest) requestParam, "importer", null, "*", null, 0);
                } else {
                    // selected rows
                    importsBean = (ArrayList<Object>) db.get_bean((HttpServletRequest) requestParam, db.getSelection(tbl_wrk, params), "bean", "*", 0);
                }
                if (importsBean != null) {
                    projectId = (Integer) utility.get(importsBean.get(0), "id");

                    Callback.send("Processing #" + projectId + " ...");

                    if (projectId != null) {
                        String sDriver = (String) utility.get(importsBean.get(0), "sDriver");
                        String sUser = (String) utility.get(importsBean.get(0), "sUser");
                        String sPassword = (String) utility.get(importsBean.get(0), "sPassword");
                        String sHost = (String) utility.get(importsBean.get(0), "sHost");
                        String sPort = (String) utility.get(importsBean.get(0), "sPort");
                        String sService = (String) utility.get(importsBean.get(0), "sService");
                        String sDatabase = (String) utility.get(importsBean.get(0), "sDatabase");
                        String sSchema = (String) utility.get(importsBean.get(0), "sSchema");
                        String sTable = (String) utility.get(importsBean.get(0), "sTable");

                        String tDriver = (String) utility.get(importsBean.get(0), "tDriver");
                        String tUser = (String) utility.get(importsBean.get(0), "tUser");
                        String tPassword = (String) utility.get(importsBean.get(0), "tPassword");
                        String tHost = (String) utility.get(importsBean.get(0), "tHost");
                        String tPort = (String) utility.get(importsBean.get(0), "tPort");
                        String tService = (String) utility.get(importsBean.get(0), "tService");
                        String tDatabase = (String) utility.get(importsBean.get(0), "tDatabase");
                        String tSchema = (String) utility.get(importsBean.get(0), "tSchema");
                        String tTable = (String) utility.get(importsBean.get(0), "tTable");

                        String sIds = (String) utility.get(importsBean.get(0), "ids");

                        String sConnString = "", tConnString = "";
                        if ("oracle".equalsIgnoreCase(sDriver)) {
                            // sConnString = "jdbc:oracle:thin:" + sSchema + "/" + sUser + "/" + sPassword + "@" + sHost + ":" + sPort + ":" + sService; // + ";User Id="+sUser+";Password="+sPassword+";";
                            sConnString = "jdbc:oracle:thin:" + sUser + "/" + sPassword + "@" + sHost + ":" + sPort + ":" + sService; // + ";User Id="+sUser+";Password="+sPassword+";SID="+sService+";";
                        }
                        if ("oracle".equalsIgnoreCase(sDriver)) {
                            tConnString = "jdbc:oracle:thin:" + tUser + "/" + tPassword + "@" + tHost + ":" + tPort + ":" + tService; // + ";User Id="+tUser+";Password="+tPassword+";";
                        }
                        
                        String sControlId = workspace.getControlIdFromDatabaseSchemaTable("source."+workspace.getDatabaseSchemaTable(sDatabase, sSchema, sTable));
                        String tControlId = workspace.getControlIdFromDatabaseSchemaTable("target."+workspace.getDatabaseSchemaTable(tDatabase, tSchema, tTable));

                        String sTableJson = ""
                                + "{ \"connectionDriver\":\"" + sDriver + "\""
                                + ", \"connectionURL\":\"" + sConnString + "\""
                                + ", \"database\":\"" + sDatabase + "\""
                                + ", \"schema\":\"" + sSchema + "\""
                                + ", \"table\":\"" + sTable + "\""
                                + ", \"columns\":\"" + "*" + "\""
                                + ", \"foreignTables\":\"" + "*" + "\""
                                + ", \"loadALL\":" + "true" + ""
                                + "}";

                        String tTableJson = ""
                                + "{ \"connectionDriver\":\"" + tDriver + "\""
                                + ", \"connectionURL\":\"" + tConnString + "\""
                                + ", \"database\":\"" + tDatabase + "\""
                                + ", \"schema\":\"" + tSchema + "\""
                                + ", \"table\":\"" + tTable + "\""
                                + ", \"columns\":\"" + "*" + "\""
                                + ", \"foreignTables\":\"" + "*" + "\""
                                + ", \"loadALL\":" + "true" + ""
                                + "}";

                        // create control
                        workspace sLiquid = workspace.get_tbl_manager_workspace(sControlId);
                        workspace tLiquid = workspace.get_tbl_manager_workspace(tControlId);

                        if(sLiquid == null) {
                            String sres = workspace.get_table_control_from_string((HttpServletRequest) requestParam, sControlId, sTableJson);
                            sLiquid = workspace.get_tbl_manager_workspace(sControlId);
                        }                            
                        if(tLiquid == null) {
                            String tres = workspace.get_table_control_from_string((HttpServletRequest) requestParam, tControlId, tTableJson);
                            tLiquid = workspace.get_tbl_manager_workspace(tControlId);
                        }

                        String[] ids = sIds.split(",");
                        
                        int nImpoted = 0;

                        for (int i = 0; i < ids.length; i++) {
                            // read bean
                            String rowId = ids[i];
                            if(rowId != null && !rowId.isEmpty()) {
                                Object rowBean = db.load_bean((HttpServletRequest) requestParam, sControlId, "*", rowId);
                                if(rowBean != null) {
                                    String res = db.insert(rowBean, tLiquid);
                                    if(res != null) {
                                        JSONObject resJson = new JSONObject(res);
                                        JSONArray tables = resJson.getJSONArray("tables");
                                        int nErr = 0;
                                        for(int t=0; t<tables.length(); t++) {
                                            JSONObject tJson = tables.getJSONObject(t);
                                            if(tJson.has("error")) {
                                                String err = utility.base64Decode(tJson.getString("error"));
                                                Callback.send("Importer failed on id:"+rowId+", <span style=\"color:red\">" + err + "<span>");
                                                nErr++;
                                                Thread.sleep(7000);
                                            }
                                        }
                                        if(nErr == 0) {
                                            nImpoted++;
                                        }
                                    }
                                }
                            }
                        }

                        Callback.send("Importer of " + projectId + " done, <span style=\"color:darkGreen\">"+nImpoted+" impoted<span>");
                        return (Object) "{ \"result\":1, \"error\":\"" + "" + "\" }";
          
                    } else {
                        Callback.send("Importer of " + projectId + " failed, <span style=\"color:red\">primaryKey not found<span>");
                        return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode("primaryKey not found") + "\" }";
                    }
                } else {
                    Callback.send("Importer failed, <span style=\"color:red\">params not defined<span>");
                    return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode("primaryKey not found") + "\" }";
                }
            } else {
                Callback.send("Importer failed, <span style=\"color:red\">params or workspace not defined ... please reload<span>");
                return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode("primaryKey not found") + "\" }";
            }

        } catch (Throwable th) {
            String err = "Error:" + th.getLocalizedMessage();
            Callback.send("Importer failed, <span style=\"color:red\">" + err + "<span>");
            return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\" }";
        }
    }
}
