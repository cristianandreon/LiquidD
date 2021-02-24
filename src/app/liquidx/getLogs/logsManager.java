/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx.getLogs;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.liquid.sftpManager;

import com.liquid.Callback;
import com.liquid.db;
import com.liquid.scpManager;
import com.liquid.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 *
 * @author root
 */
public class logsManager {

    static String glSourceFile = "";
    static String glTtargetFile = "";
    static long glFileSize = 0;
    static float maxSpeed = 0.0f;
    static float minSpeed = 0.0f;

    static public Object getLog(Object tbl_wrk, Object params, Object clientData, Object requestParam) throws JSONException, InterruptedException, Exception {
        String retVal = "{ \"client\":\"Liquid.stopWaiting('getLogsCfg')\" }";
        try {
            if (params != null) {
                // {"params":[{"data":{"1":"38","2":"SIA to site","3":"192.168.0.116","4":"sysadmin","5":"geiadmin01","6":"<p>/Users/administrator/Workspaces/workspacevl/deploy_site_geisoft_org/webapp-zipped/sia.war</p>","7":"/opt/applicativi/jboss-4.0.5.GA/server/all_co-1/deploy","8":"<p>function getFolderDateName() {</p><p>var date = new Date(); var d = date.getDate(); var m = date.getMonth() + 1; var y = date.getFullYear();</p><p>var dateString = String(y) + String(m &lt;= 9 ? '0' + m : m) + String(d &lt;= 9 ? '0' + d : d);</p><p>return dateString;</p><p>}</p><p>\"/home/%user%/rilasci/%webApp%/\"+getFolderDateName()</p>","9":"<p>function getFolderDateName() {</p><p>var date = new Date(); var d = date.getDate(); var m = date.getMonth() + 1; var y = date.getFullYear();</p><p>var dateString = String(y) + String(m &lt;= 9 ? '0' + m : m) + String(d &lt;= 9 ? '0' + d : d);</p><p>return dateString;</p><p>}</p><p>\"/home/%user%/rilasci/%webApp%/\"+getFolderDateName()+\"/BACKUP\"</p>","10":"<p>sia.war</p>","11":"<p>http://site.geisoft.org/sia</p>","12":"10000","13":"10000","14":"''cristian.andreon@geisoft.com,info@cristiannadreon.eu"}},{"name":"doBackup","data":"true"},{"name":"askConfirmation","data":"false"},{"name":"getLogsCfg","sel":["38"]}]}
                Object nameParam = com.liquid.event.getObject(params, "name");
                JSONArray rowsData = com.liquid.event.getJSONArray(params, "formX");
                String cfgId = null, cfgName = null, fileName = null, fileSize = null, file = null;
                if (rowsData != null) {
                    // Old way ...
                    JSONObject rowData = rowsData.getJSONObject(0);
                    cfgId = rowData.getString("2");
                    cfgName = rowData.getString("3");
                    file = rowData.getString("4");
                    try {
                        rowData.getString("file.filesName");
                    } catch (Exception e) {
                    }
                    try {
                        fileSize = rowData.getString("file.filesSize");
                    } catch (Exception e) {
                    }
                    String controlId = "getLogsCfg";
                } else {
                    // New way ...
                    // N.B.: We just want the data of the current control (which is empty)
                    JSONObject rowData = com.liquid.event.getJSONObject(params, "data", "");
                    cfgId = rowData.getString("1");
                }
                    
                JSONObject doBackupJSON = com.liquid.event.getJSONObject(params, "data", "doBackup");
                JSONObject askConfirmationJSON = com.liquid.event.getJSONObject(params, "data", "askConfirmation");
                

                    
                // Lettura del bean di configurazione
                if (cfgId != null && !cfgId.isEmpty()) {
                    // Object deplpoyBean = db.get_bean(requestParam, controlId, id, null, "*", null, 1);
                    Object deplpoyBean = db.load_bean((HttpServletRequest) requestParam, "LiquidX.liquidx.getLogsCfg", "*", cfgId);
                    if (deplpoyBean != null) {
                        cfgName = (String) utility.get(deplpoyBean, "name");
                        String host = (String) utility.get(deplpoyBean, "host");
                        String user = (String) utility.get(deplpoyBean, "user");
                        String password = (String) utility.get(deplpoyBean, "password");
                        String logFolder = (String) utility.get(deplpoyBean, "logFolder");
                        String logFile = (String) utility.get(deplpoyBean, "logFile");
                        String webAppWAR = (String) utility.get(deplpoyBean, "webAppWAR");
                        String protocol = (String) utility.get(deplpoyBean, "protocol");

                        
                        logFolder = utility.decodeHtml(logFolder);
                        webAppWAR = utility.decodeHtml(webAppWAR);
                        
                        
                        if(logFolder != null && !logFolder.isEmpty()) {
                        
                            // Nome del WAR
                            webAppWAR = webAppWAR != null && !webAppWAR.isEmpty() ? webAppWAR : fileName;

                            String webApp = webAppWAR.substring(0, webAppWAR.lastIndexOf('.'));

                            // Adattamento webAppName
                            webAppWAR = webApp + ".war";

                            // Risoluzione backupFolder
                            logFolder = solve_variable_field(logFolder, user, webApp);

                            // Risoluzione backupFolder
                            logFile = solve_variable_field(logFile, user, webApp);


                            if(!logFolder.endsWith("/"))
                                logFolder += "/";

                            String logFileName = logFolder + (logFile != null && !logFile.isEmpty() ? ""+logFile : "");
                            
                            if(logFolder != null && !logFolder.isEmpty()) {
                            
                                String home = System.getProperty("user.home");
                                String baseFolder = home+"/Downloads/LiquidD";                        
                                String localLogFileName = baseFolder + "/" + webApp + ".log";

                                Callback.send("Downloading log '"+logFileName+"' from " + cfgName + " ...");


                                utility.createFolder(baseFolder);

                                //
                                // get file from server via SFTP
                                //
                                boolean uploadFileOk = false;
                                String uploadFileError = "";

                                boolean bDataDecoded = false;
                                OutputStream targetFileOS = null;

                                if (localLogFileName != null && !localLogFileName.isEmpty()) {
                                    File f = new File(localLogFileName);
                                    if (f != null) {
                                        if (f.createNewFile()) {
                                            Callback.send("1&deg; - Downloading " + logFileName + "...");
                                        }


                                        // boolean doBackup = "true".equalsIgnoreCase( doBackupJSON.getString("data")) ? true : false;
                                        // boolean askConfirmation = "true".equalsIgnoreCase( askConfirmationJSON.getString("data")) ? true : false;

                                        if("scp".equalsIgnoreCase(protocol)) {

                                            if(!scpManager.downloadFile(user, password, host, 22, logFileName, localLogFileName)) {
                                                String err = "Error in download via scp";
                                                Callback.send("get log, <span style=\"color:red\">" + err + "<span>");
                                                return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('getLogsCfg')\" }";
                                            }

                                        } else {

                                            // 1° upload
                                            sftpManager sftp = new sftpManager();                               
                                            long lRetVal = 0;

                                            try {

                                                targetFileOS = new FileOutputStream(f);

                                                Object[] result = sftp.download(host, user, password, logFileName, targetFileOS);
                                                lRetVal = (long) result[0];
                                                if (lRetVal > 0) {
                                                    // TODO : visualizzazione del file ...
                                                }


                                            } catch (JSchException ex) {
                                                java.util.logging.Logger.getLogger(logsManager.class.getName()).log(Level.SEVERE, null, ex);
                                                String err = "Error:" + ex.getLocalizedMessage()+ " Check Network/VPN";
                                                Callback.send("get log failed, <span style=\"color:red\">" + err + "<span>");
                                                return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('getLogsCfg')\" }";
                                            } catch (SftpException ex) {
                                                java.util.logging.Logger.getLogger(logsManager.class.getName()).log(Level.SEVERE, null, ex);
                                                uploadFileError += ex.getLocalizedMessage();
                                                String err = "Error:" + ex.getLocalizedMessage()+ " Check Network/VPN";
                                                Callback.send("get log, <span style=\"color:red\">" + err + "<span>");
                                                return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('getLogsCfg')\" }";
                                            } catch (Exception e) {
                                                String err = "Error:" + e.getLocalizedMessage()+ " Check Network/VPN";
                                                Callback.send("get log, <span style=\"color:red\">" + err + "<span>");
                                                return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('getLogsCfg')\" }";
                                            } finally {
                                                targetFileOS.flush();
                                                targetFileOS.close();
                                            }
                                            // String script = "notifyMessage(\"" + msg_for_notity + "\")";
                                            // com.liquid.JSScript.script(script);
                                        }

                                        Callback.send("1&deg; - Download of  " + logFileName + " done ... look to local file : <a href=\"file:///"+localLogFileName+"\">"+localLogFileName+"</a>"
                                                +"<br/><br/>If your browser doesn't allow local file access copy the link and paste it in new tab"
                                        );
                                        retVal = "{ \"client\":\"Liquid.stopWaiting('getLogsCfg'); showLog('"+localLogFileName+"', '"+webApp+"')\" }";

                                    } else {
                                        String err = "log file not accessible";
                                        Callback.send("1&deg; - get log of " + cfgName + "failed, <span style=\"color:red\">" + err + "<span>");
                                        return (Object) "{ \"result\":-2, \"error\":\"" + utility.base64Encode(err) + "\" }";
                                    }                                 
                                }
                            } else {
                                String err = "log file not defined";
                                Callback.send("1&deg; - get log of " + cfgName + "failed, <span style=\"color:red\">" + err + "<span>");
                                return (Object) "{ \"result\":-2, \"error\":\"" + utility.base64Encode(err) + "\" }";
                            }                                 
                        } else {
                            String err = "log folfer not defined";
                            Callback.send("1&deg; - get log of " + cfgName + "failed, <span style=\"color:red\">" + err + "<span>");
                            return (Object) "{ \"result\":-2, \"error\":\"" + utility.base64Encode(err) + "\" }";
                        }                                 

                    } else {
                        Callback.send("Deploy of " + cfgName + "failed, <span style=\"color:red\">read bean error<span>");
                        retVal = "{ \"result\":-2, \"error\":\"" + utility.base64Encode("read bean error") + "\", \"client\":\"Liquid.stopWaiting('getLogsCfg')\" }";
                    }
                } else {
                    Callback.send("Deploy of " + cfgName + " failed, <span style=\"color:red\">primaryKey not found<span>");
                    retVal = "{ \"result\":-1, \"error\":\"" + utility.base64Encode("primaryKey not found") + "\", \"client\":\"Liquid.stopWaiting('getLogsCfg')\" }";
                }
            } else {
                Callback.send("Deploy failed, <span style=\"color:red\">params not defined<span>");
                retVal = "{ \"result\":-1, \"error\":\"" + utility.base64Encode("primaryKey not found") + "\", \"client\":\"Liquid.stopWaiting('getLogsCfg')\" }";
            }
        } catch (Throwable th) {
            String err = "Error:" + th.getLocalizedMessage();
            Callback.send("Deploy failed, <span style=\"color:red\">" + err + "<span>");
            retVal = "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('getLogsCfg')\" }";
        }
        
        return (Object)retVal;
    }

    public static String solve_variable_field(String expr, String user, String webApp) throws Exception {
        if (expr != null && !expr.isEmpty()) {
            // expr = expr.replaceAll("<!--.*?-->", "").replaceAll("<[^>]+>", "");
            expr = Jsoup.parse(expr).text().replaceAll("\\<.*?>","");
            ScriptEngineManager sem = new ScriptEngineManager();
            ScriptEngine js = sem.getEngineByName("JavaScript");
            Object result = null;
            String sResult = expr;
            try {
                result = js.eval(expr);
                sResult = (String) result;
            } catch (Throwable th) {
            }

            return sResult.replace("${user}", user).replace("${webApp}", webApp).replace("%user%", user).replace("%webApp%", webApp);
        }
        return expr;
    }

}
