/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx.deploy;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.liquid.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/*
faillock --reset --user sergio-xxx
*/

/**
 *
 * @author root
 */
public class deployManager {

    static String glSourceFile = "";
    static String glTtargetFile = "";
    static long glFileSize = 0;
    static float maxSpeed = 0.0f;
    static float minSpeed = 0.0f;

    static public Object deploy(Object tbl_wrk, Object params, Object clientData, Object requestParam) throws JSONException, InterruptedException, Exception {
        String retVal = "{ \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";

        try {
            if (params != null) {
                // {"params":[{"data":{"1":"38","2":"SIA to site","3":"192.168.0.116","4":"sysadmin","5":"geiadmin01","6":"<p>/Users/administrator/Workspaces/workspacevl/deploy_site_geisoft_org/webapp-zipped/sia.war</p>","7":"/opt/applicativi/jboss-4.0.5.GA/server/all_co-1/deploy","8":"<p>function getFolderDateName() {</p><p>var date = new Date(); var d = date.getDate(); var m = date.getMonth() + 1; var y = date.getFullYear();</p><p>var dateString = String(y) + String(m &lt;= 9 ? '0' + m : m) + String(d &lt;= 9 ? '0' + d : d);</p><p>return dateString;</p><p>}</p><p>\"/home/%user%/rilasci/%webApp%/\"+getFolderDateName()</p>","9":"<p>function getFolderDateName() {</p><p>var date = new Date(); var d = date.getDate(); var m = date.getMonth() + 1; var y = date.getFullYear();</p><p>var dateString = String(y) + String(m &lt;= 9 ? '0' + m : m) + String(d &lt;= 9 ? '0' + d : d);</p><p>return dateString;</p><p>}</p><p>\"/home/%user%/rilasci/%webApp%/\"+getFolderDateName()+\"/BACKUP\"</p>","10":"<p>sia.war</p>","11":"<p>http://site.geisoft.org/sia</p>","12":"10000","13":"10000","14":"''cristian.andreon@geisoft.com,info@cristiannadreon.eu"}},{"name":"doBackup","data":"true"},{"name":"askConfirmation","data":"false"},{"name":"deploysCfg","sel":["38"]}]}
                Object nameParam = com.liquid.event.getObject(params, "name");
                JSONArray rowsData = com.liquid.event.getJSONArray(params, "formX");
                String cfgId = null, cfgName = null, fileName = null, sFileSize = null, file = null;
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
                        sFileSize = rowData.getString("file.filesSize");
                    } catch (Exception e) {
                    }
                    String controlId = "deploysCfg";
                } else {
                    // New way ...
                    // N.B.: We just want the data of the current control (which is empty)
                    JSONObject rowData = com.liquid.event.getJSONObject(params, "data", "");
                    cfgId = rowData.getString("1");
                }
                    
                JSONObject doBackupJSON = com.liquid.event.getJSONObject(params, "form", "doBackup");
                JSONObject askConfirmationJSON = com.liquid.event.getJSONObject(params, "form", "askConfirmation");
                JSONObject openURLJSON = com.liquid.event.getJSONObject(params, "form", "openURL");

                    
                // Lettura del bean di configurazione
                if (cfgId != null && !cfgId.isEmpty()) {
                    // Object deplpoyBean = db.get_bean(requestParam, controlId, id, null, "*", null, 1);
                    Object deplpoyBean = bean.load_bean((HttpServletRequest) requestParam, "LiquidX.liquidx.deploysCfg", "*", "id="+cfgId);
                    if (deplpoyBean != null) {

                        boolean doBackup = "true".equalsIgnoreCase( doBackupJSON.getString("data")) ? true : false;
                        boolean askConfirmation = "true".equalsIgnoreCase( askConfirmationJSON.getString("data")) ? true : false;
                        boolean openURL = "true".equalsIgnoreCase( openURLJSON.getString("data")) ? true : false;

                        retVal = (String)do_deploy(deplpoyBean, cfgName, fileName, doBackup, askConfirmation, openURL, false, (HttpServletRequest) requestParam);



                    } else {
                        Callback.send("Deploy of " + cfgName + "failed, <span style=\"color:red\">read bean error<span>");
                        retVal = "{ \"result\":-2, \"error\":\"" + utility.base64Encode("read bean error") + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
                    }
                } else {
                    Callback.send("Deploy of " + cfgName + " failed, <span style=\"color:red\">primaryKey not found<span>");
                    retVal = "{ \"result\":-1, \"error\":\"" + utility.base64Encode("primaryKey not found") + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
                }
            } else {
                Callback.send("Deploy failed, <span style=\"color:red\">params not defined<span>");
                retVal = "{ \"result\":-1, \"error\":\"" + utility.base64Encode("primaryKey not found") + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
            }
        } catch (Throwable th) {
            String err = "Error:" + th.getLocalizedMessage();
            Callback.send("Deploy failed, <span style=\"color:red\">" + err + "<span>");
            retVal = "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
            java.util.logging.Logger.getLogger(deployManager.class.getName()).severe(th.getMessage());
        } finally {
        }
        
        return (Object)retVal;
    }


    static public Object openTerminal(Object tbl_wrk, Object params, Object clientData, Object requestParam) throws JSONException, InterruptedException, Exception {
        return doCommand(tbl_wrk, params, clientData, requestParam, "openTerminal");
    }
    static public Object openURL(Object tbl_wrk, Object params, Object clientData, Object requestParam) throws JSONException, InterruptedException, Exception {
        return doCommand(tbl_wrk, params, clientData, requestParam, "openURL");
    }

    static public Object doCommand(Object tbl_wrk, Object params, Object clientData, Object requestParam, String cmd) throws JSONException, InterruptedException, Exception {
        String retVal = "{ \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";

        try {
            if (params != null) {
                Object nameParam = com.liquid.event.getObject(params, "name");
                JSONArray rowsData = com.liquid.event.getJSONArray(params, "formX");
                String cfgId = null, cfgName = null, fileName = null, sFileSize = null, file = null;
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
                        sFileSize = rowData.getString("file.filesSize");
                    } catch (Exception e) {
                    }
                    String controlId = "deploysCfg";
                } else {
                    // New way ...
                    // N.B.: We just want the data of the current control (which is empty)
                    cfgId = com.liquid.db.getSelection(tbl_wrk, params);
                }

                // Lettura del bean di configurazione
                if (cfgId != null && !cfgId.isEmpty()) {
                    // Object deplpoyBean = db.get_bean(requestParam, controlId, id, null, "*", null, 1);
                    Object deplpoyBean = bean.load_bean((HttpServletRequest) requestParam, "LiquidX.liquidx.deploysCfg", "*", "id="+cfgId);
                    if (deplpoyBean != null) {

                        if("openTerminal".equalsIgnoreCase(cmd)) {

                            String ssh_cmd = "ssh " + utility.getString(deplpoyBean, "user") + "@" + utility.getString(deplpoyBean, "host");
                            utility.create_file(
                                    "/tmp/pass.txt",
                                    utility.getString(deplpoyBean, "password")
                            );

                            utility.create_file(
                                    "/tmp/tmp.sh",
                                    "cat /tmp/pass.txt|pbcopy\n"
                                            + "echo \"*** LIQUID : Paste your password to ssh ***\"\n"
                                            + "" + ssh_cmd + "\n"
                                    // + "pbpaste > "+ssh_cmd + ""
                                    // + "echo \""+utility.getString(deplpoyBean, "password")+"\" | tee | " + ssh_cmd + " pbcopy"
                            );

                            // /usr/bin/open -a /Applications/Utilities/Terminal.app /bin/bash
                            // String script = "/usr/bin/open -a /Applications/Utilities/Terminal.app /bin/bash";
                            // osascript -e 'tell application \"Terminal\" to do script "sh /tmp/tmp.sh"'
                            // String script = "osascript -e 'tell application \"Terminal\" to do script \"sh /tmp/tmp.sh\"'";
                            // Process r = Runtime.getRuntime().exec(script);

                            final ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/osascript",
                                    "-e", "tell app \"Terminal\"",
                                    "-e", "set currentTab to do script (\"sh /tmp/tmp.sh\")"
                                    ,"-e", "end tell"
                                    ,"-e", "tell application \"Terminal\" to activate"
                                    // ,"-e", "tell application \"Terminal\" keystroke \""+utility.getString(deplpoyBean, "password")+"\" using command down"
                            );
                            final Process r = processBuilder.start();

                            boolean a = r.isAlive();
                            int ev = r.exitValue();

                            try {
                                r.getOutputStream().write("\\x16\n".getBytes(StandardCharsets.UTF_8));
                                r.getOutputStream().write(16);
                                r.getOutputStream().flush();
                            }catch (Exception e) {}

                        } else if ("openURL".equalsIgnoreCase(cmd)) {
                            String webAppURL = utility.decodeHtml((String) utility.get(deplpoyBean, "webAppURL"));
                            String openScript = "window.open(\"" + webAppURL + "\")";
                            JSScript.script(openScript);
                        }

                    } else {
                        Callback.send("Deploy of " + cfgName + "failed, <span style=\"color:red\">read bean error<span>");
                        retVal = "{ \"result\":-2, \"error\":\"" + utility.base64Encode("read bean error") + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
                    }
                } else {
                    Callback.send("Deploy of " + cfgName + " failed, <span style=\"color:red\">primaryKey not found<span>");
                    retVal = "{ \"result\":-1, \"error\":\"" + utility.base64Encode("primaryKey not found") + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
                }
            } else {
                Callback.send("Deploy failed, <span style=\"color:red\">params not defined<span>");
                retVal = "{ \"result\":-1, \"error\":\"" + utility.base64Encode("primaryKey not found") + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
            }
        } catch (Throwable th) {
            String err = "Error:" + th.getLocalizedMessage();
            Callback.send("Deploy failed, <span style=\"color:red\">" + err + "<span>");
            retVal = "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
            java.util.logging.Logger.getLogger(deployManager.class.getName()).severe(th.getMessage());
        } finally {
        }

        return (Object)retVal;
    }



    /**
     *
     * @param deplpoyBean
     * @param cfgName
     * @param fileName
     * @param doBackup
     * @param askConfirmation
     * @param openURL
     * @param batchMode
     * @param request
     * @return
     * @throws Exception
     */
    public static Object do_deploy(Object deplpoyBean, String cfgName, String fileName, boolean doBackup, boolean askConfirmation, boolean openURL, boolean batchMode, HttpServletRequest request) throws Exception {

        String retVal = "{ \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
        sshManager ssh = null;
        sftpManager sftp = null;

        try {
            cfgName = (String) utility.get(deplpoyBean, "name");
            String host = (String) utility.get(deplpoyBean, "host");
            String user = (String) utility.get(deplpoyBean, "user");
            String password = (String) utility.get(deplpoyBean, "password");
            String sourceFile = (String) utility.get(deplpoyBean, "sourceFile");
            String sourceFileAlternative = (String) utility.get(deplpoyBean, "sourceFileAlternative");
            String deployFolder = (String) utility.get(deplpoyBean, "deployFolder");
            String copyFolder = (String) utility.get(deplpoyBean, "copyFolder");
            String backupFolder = (String) utility.get(deplpoyBean, "backupFolder");
            String webAppWAR = (String) utility.get(deplpoyBean, "webAppWAR");
            String webAppURL = (String) utility.get(deplpoyBean, "webAppURL");
            int undeployWaitTime = (int) utility.get(deplpoyBean, "undeployWaitTime");
            int checkWaitTime = (int) utility.get(deplpoyBean, "checkWaitTime");
            String notifyEmails = (String) utility.get(deplpoyBean, "notifyEmails");
            String protocol = (String) utility.get(deplpoyBean, "protocol");


            String sFileSize = "[N/D]";


            // format the html fields
            sourceFile = utility.decodeHtml(sourceFile);
            sourceFileAlternative = utility.decodeHtml(sourceFileAlternative);
            deployFolder = utility.decodeHtml(deployFolder);
            copyFolder = utility.decodeHtml(copyFolder);
            backupFolder = utility.decodeHtml(backupFolder);
            webAppWAR = utility.decodeHtml(webAppWAR);
            webAppURL = utility.decodeHtml(webAppURL);


            // Nome del WAR
            webAppWAR = webAppWAR != null && !webAppWAR.isEmpty() ? webAppWAR : fileName;

            String webApp = webAppWAR.substring(0, webAppWAR.lastIndexOf('.'));
            String sourceFileCDate = "[N/D]";
            String sourceFileLDate = "[N/D]";

            // Adattamento webAppName
            webAppWAR = webApp + ".war";

            Callback.send("<h2>Processing <b>" + cfgName + "</b> ...</h2>");

            //
            // put file to server via SFTP
            //
            boolean uploadFileOk = false;
            String uploadFileError = "";

            boolean bDataDecoded = false;
            InputStream sourceFileIS = null;

            if (
                    (sourceFile != null && !sourceFile.isEmpty())
                            || (sourceFileAlternative != null && !sourceFileAlternative.isEmpty())
            ) {

                File f = null;
                if (utility.fileExist(sourceFile)) {
                    f = new File(sourceFile);
                } else if (utility.fileExist(sourceFileAlternative)) {
                    sourceFile = sourceFileAlternative;
                    f = new File(sourceFile);
                }

                if (f != null) {

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss");
                    sourceFileLDate = dateFormat.format(f.lastModified());
                    BasicFileAttributes attrs = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                    FileTime time = attrs.creationTime();
                    sourceFileCDate = dateFormat.format(new Date(time.toMillis()));

                    sourceFileIS = new FileInputStream(new File(sourceFile));
                    if (sourceFileIS != null) {
                        bDataDecoded = true;
                        sFileSize = String.valueOf(f.length());
                    }
                    Callback.send("1&deg; - Uploading " + sourceFile + " " + (f.length() / 1024 / 1024) + "MB...");
                    glSourceFile = sourceFile;
                    glFileSize = f.length();

                } else {
                    String err = "source file / alternative source file not accessible";
                    Callback.send("1&deg; - Deploy of " + cfgName + "failed, <span style=\"color:red\">" + err + "<span>");
                    return (Object) "{ \"result\":-2, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
                }

            } else {
                Messagebox.show("Missing sourc file", "LiquidD", Messagebox.WARNING + Messagebox.OK);
            }

            if (bDataDecoded) {

                // Sample of js function
                                /*
                                    function getFolderDateName() {
                                        var date = new Date();
                                        var d = date.getDate();
                                        var m = date.getMonth() + 1;
                                        var y = date.getFullYear();
                                        var dateString = String(y) + (m <= 9 ? '0' + m : m) + (d <= 9 ? '0' + d : d); return dateString; }
                                    "/home/%user%/rilasci/%webApp%/getFolderDateName();
                                */

                //
                // version of the web app
                //
                String ver = (String) utility.getArchiveXMLTag(sourceFile, "WEB-INF/product.xml", "product/version");

                String desc_file = "" + ver;
                String desc_content = "" + (String) utility.getArchiveFile(sourceFile, "WEB-INF/product.xml", "product/version");
                ;

                utility.set(deplpoyBean, "version", ver);

                // Risoluzione backupFolder
                backupFolder = solve_variable_field(backupFolder, user, webApp);

                // Risoluzione copyFolder
                copyFolder = solve_variable_field(copyFolder, user, webApp);

                // Risoluzione deployFolder
                deployFolder = solve_variable_field(deployFolder, user, webApp);

                // Strip last /
                if (backupFolder.endsWith("/")) {
                    backupFolder = backupFolder.substring(0, backupFolder.length() - 1);
                }
                if (copyFolder.endsWith("/")) {
                    copyFolder = copyFolder.substring(0, copyFolder.length() - 1);
                }
                if (deployFolder.endsWith("/")) {
                    deployFolder = deployFolder.substring(0, deployFolder.length() - 1);
                }


                String msg = null, msg_for_notity = null;

                String message = " Processing <b>" + cfgName + "</b></br>"
                        + "</br>"
                        + "<span style=\"font-size:90%\">"
                        + "file : <b>" + sourceFile + "</b></br>"
                        + "</span>"
                        + "</br>"
                        + "Size: " + glFileSize + "</br>"
                        + "Creation date: " + sourceFileCDate + "</br>"
                        + "Last modify date: " + sourceFileLDate + "</br>"
                        + "</br>"
                        + "</br>"
                        + "<span style=\"font-size:85%; left:50px; position: relative;\">"
                        + "</span>"
                        + "target : <b>" + user + "@" + host + "</b></br>"
                        + "</br>"
                        + "<span style=\"font-size:80%; left:60px; position: relative;\">"
                        + "</br>Copy  to <b>" + copyFolder + "/" + webAppWAR + "</b></br>"
                        + "</br>"
                        + (!doBackup ? "<span style=\"color:darkGray; \">" : "")
                        + (doBackup ? ("</br>Backup to <b>" + backupFolder + "/" + webAppWAR + "</b></br>") : ("</br>Backup Disabled (to <b>" + backupFolder + "/" + webAppWAR + "</b>)</br>"))
                        + (!doBackup ? "</span>" : "")
                        + "</br>"
                        + "</br>Deploy to <b>" + deployFolder + "/" + webAppWAR + "</b></br>"
                        + "</span>";

                if (batchMode || Messagebox.show(message, "LiquidD", Messagebox.QUESTION + Messagebox.YES + Messagebox.NO) == Messagebox.YES) {

                    //
                    // Apertura sessione ssh
                    //
                    Callback.send("1&deg;/5 - Open ssh session ...");
                    ssh = new sshManager();
                    if (!ssh.connect(host, user, password)) {
                        String err = "Error: ssh session failed";
                        Callback.send("Deploy failed, <span style=\"color:red\">" + err + "<span>");
                        return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
                    }

                    Callback.send("1&deg;/5 - Logging as root...");


                    // impersona solamente l'utente root senza altre azioni di login
                    String cmd = " sudo su -";
                    ssh.cmd(cmd, password);


                    ssh.removeLastCommand();


                    //
                    // Verifica apszio disco
                    //
                    String[] disk_info = ssh.getRemoteDiskInfo();

                    if (disk_info != null) {
                        if (disk_info[2] != null && !disk_info[2].isEmpty()) {
                            long freeSpace = Long.parseLong(disk_info[2]);
                            long diskSpace = Long.parseLong(disk_info[0]);
                            if (freeSpace * 1024 < glFileSize * 3) {
                                message = " You are deploing info a low space disk : <b>" + (freeSpace / 1024) + "</b> Mb</br>"
                                        + "</br>"
                                        + "</br>"
                                        + "<span style=\"font-size:80%; left:50px; position: relative;\">"
                                        + " Disk size : <b>" + (diskSpace / 1024) + "</b> Mb</br>"
                                        + "</span>"
                                        + "</br>"
                                        + "</br>"
                                        + "<span style=\"font-size:80%; left:50px; position: relative;\">"
                                        + " Disk usage : <b>" + (disk_info[3]) + "</b> Mb</br>"
                                        + "</span>"
                                        + "</br>"
                                        + "</br>"
                                        + "<span style=\"font-size:100%; left:50px; position: relative;\">"
                                        + " Do you want to continue anyway ?</br>"
                                        + "</span>";
                                if (Messagebox.show(message, "LiquidD", Messagebox.WARNING + Messagebox.YES + Messagebox.NO) == Messagebox.YES) {

                                } else {
                                    String err = "Stopped by low disk free space warning";
                                    Callback.send("Deploy failed, <span style=\"color:red\">" + err + "<span>");
                                    return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
                                }
                            }
                        }
                    }


                    //
                    // 1° upload
                    //

                    long remoteFileSize = 0;
                    long lRetVal = 0;

                    Callback.send("1&deg;/5 - Uploading file...");


                    boolean uploadFile = true;
                    boolean isRemoteFileChanged = false;


                    if ("scp".equalsIgnoreCase(protocol)) {
                        //
                        // scp
                        //

                        isRemoteFileChanged = ssh.isRemoteFileChanged(host, user, password, glSourceFile, copyFolder + "/" + webAppWAR);

                    } else {
                        //
                        // sftp
                        //

                        sftp = new sftpManager();

                        isRemoteFileChanged = sftp.isRemoteFileChanged(host, user, password, glSourceFile, copyFolder + "/" + webAppWAR);

                    }


                    if (!isRemoteFileChanged) {
                        message = " Remote file <b>" + copyFolder + "/" + webAppWAR + "</b> is not changd</br>"
                                + "</br>"
                                + "</br>"
                                + "<span style=\"font-size:80%; left:50px; position: relative;\">"
                                + "</br>"
                                + " File size: <b>" + sFileSize + "</b></br>"
                                + "</span>"
                                + "</br>"
                                + "</br>"
                                + "<span style=\"font-size:100%; left:50px; position: relative;\">"
                                + " Do you want to upload upload file anyway ?</br>"
                                + "</span>";
                        if (Messagebox.show(message, "LiquidD", Messagebox.QUESTION + Messagebox.YES + Messagebox.NO) == Messagebox.YES) {
                            uploadFile = true;
                        } else {
                            uploadFile = false;
                        }
                    } else {
                        uploadFile = true;
                    }


                    if ("scp".equalsIgnoreCase(protocol)) {
                        //
                        // scp
                        //

                        try {

                            if (uploadFile) {

                                //
                                // make dir by ssh .. even scp fails if folder doesn't exist
                                //
                                if (!ssh.create_folders(copyFolder, user)) {
                                    String err = "Failed to create folder " + copyFolder + "...";
                                    Logger.getLogger(deployManager.class.getName()).log(Level.SEVERE, err);
                                    Callback.send("Error: <span style=\"color:red\">" + err + "<span>");
                                }

                                uploadFileOk = scpManager.uploadFile(user, password, host, 22, copyFolder + "/" + webAppWAR, glSourceFile);

                                lRetVal = getRemoteFileSize(ssh, host, user, password, copyFolder + "/" + webAppWAR, protocol);
                                remoteFileSize = lRetVal;
                            } else {
                                Callback.send("1&deg;/5 - upload file skipped, remote file is up to date...");
                                Thread.sleep(1000);
                                remoteFileSize = lRetVal = Long.parseLong(sFileSize);
                                uploadFileOk = true;
                            }

                        } catch (Exception ex) {
                            Logger.getLogger(deployManager.class.getName()).log(Level.SEVERE, null, ex);
                            String err = "Error:" + ex.getLocalizedMessage() + " Check Network/VPN";
                            Callback.send("Deploy failed, <span style=\"color:red\">" + err + "<span>");
                            return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
                        }


                    } else {
                        //
                        // Sftp
                        //
                        if (uploadFile) {

                            try {

                                Object[] result = sftp.upload(host, user, password, glSourceFile, sourceFileIS, copyFolder + "/" + webAppWAR);
                                lRetVal = (long) result[0];
                                if (doBackup) doBackup = (boolean) result[1];
                                if (lRetVal > 0) {
                                    if (sFileSize != null && !sFileSize.isEmpty()) {
                                        if (Long.parseLong(sFileSize) != lRetVal) {
                                            remoteFileSize = sftp.getRemoteFileSize(host, user, password, copyFolder + "/" + webAppWAR);
                                            if (Long.parseLong(sFileSize) == remoteFileSize) {
                                                lRetVal = remoteFileSize;
                                            }
                                        }

                                        if (Long.parseLong(sFileSize) == lRetVal) {
                                            uploadFileOk = true;
                                            // upload descriptor
                                            InputStream descFileIS = new ByteArrayInputStream(desc_content.getBytes());
                                            sftp.upload(host, user, password, null, descFileIS, copyFolder + "/" + (desc_file));
                                        }
                                    } else {
                                        uploadFileOk = true;
                                    }
                                } else {
                                    uploadFileError = "File has zero size";
                                }
                            } catch (JSchException ex) {
                                Logger.getLogger(deployManager.class.getName()).log(Level.SEVERE, null, ex);
                                String err = "Error:" + ex.getLocalizedMessage() + " Check Network/VPN";
                                Callback.send("Deploy failed, <span style=\"color:red\">" + err + "<span>");
                                return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
                            } catch (SftpException ex) {
                                Logger.getLogger(deployManager.class.getName()).log(Level.SEVERE, null, ex);
                                uploadFileError += ex.getLocalizedMessage();
                                String err = "Error:" + ex.getLocalizedMessage() + " Check Network/VPN";
                                Callback.send("Deploy failed, <span style=\"color:red\">" + err + "<span>");
                                return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
                            } catch (Exception e) {
                                String err = "Error:" + e.getLocalizedMessage() + " Check Network/VPN";
                                Callback.send("Deploy failed, <span style=\"color:red\">" + err + "<span>");
                                return (Object) "{ \"result\":-1, \"error\":\"" + utility.base64Encode(err) + "\", \"client\":\"Liquid.stopWaiting('deploysCfg')\" }";
                            }

                            remoteFileSize = sftp.getRemoteFileSize(host, user, password, copyFolder + "/" + webAppWAR);
                        } else {
                            Callback.send("1&deg;/5 - upload file skipped, remote file is up to date...");
                            Thread.sleep(1000);
                            lRetVal = remoteFileSize = Long.parseLong(sFileSize);
                            uploadFileOk = true;
                        }
                    }


                    String recipients[] = notifyEmails != null && !notifyEmails.isEmpty() ? notifyEmails.split(",") : null;

                    if (uploadFileOk) {
                        //
                        // Verifica del file caricato
                        //
                        Callback.send("1&deg;/5 - Checking uploaded file...");
                        if (remoteFileSize != lRetVal) {
                            msg = "Error : Failed to upload current war (" + copyFolder + "/" + webAppWAR + ")<br/><br/>... size mismath : " + remoteFileSize + "/" + lRetVal + "";
                            Callback.send(msg);
                            Messagebox.show(msg, "LiquidD", Messagebox.OK + Messagebox.ERROR);
                            return retVal;
                        }


                        // 2° backup
                        //
                        // Backup file attualmente in prod
                        //
                        if (doBackup) {
                            Callback.send("2&deg;/5 - Backup file (only if missing or older)...");

                            if (!ssh.create_folders(backupFolder, user)) {
                                String err = "Failed to create folder " + backupFolder + "...";
                                Logger.getLogger(deployManager.class.getName()).log(Level.SEVERE, err);
                                Callback.send("Error: <span style=\"color:red\">" + err + "<span>");
                            }

                            // N.B. una volta eseguito il primo backup i deploy sucessivi non modificheranno il backup esistente
                            // -u, --update                 copy only when the SOURCE file is newer than the destination file or when the destination file is missin
                            // -n, --no-clobber             do not overwrite an existing file (overrides a previous -i option)
                            cmd = "cp " + deployFolder + "/" + webAppWAR + " " + backupFolder + " -n";
                            ssh.cmd(cmd);


                            Thread.sleep(500);

                            //
                            // Verifica file copiato ...
                            //
                            Callback.send("2&deg;/5 - Checking backup file in " + backupFolder + "...");
                            remoteFileSize = getRemoteFileSize(ssh, host, user, password, backupFolder + "/" + webAppWAR, protocol);
                            if (remoteFileSize != 0 && remoteFileSize < 0xFFFFFFFF - 0xFF) {

                                if ("scp".equalsIgnoreCase(protocol)) {
                                    // scp cannot get file size in deploy folder, that is root owned
                                } else {

                                    Thread.sleep(1000);

                                    Callback.send("3&deg;/5 - Retry to copy current file to " + backupFolder + "...");
                                    cmd = " sudo mkdir -p " + backupFolder;
                                    ssh.cmd(cmd);
                                    cmd = "sudo cp " + deployFolder + "/" + webAppWAR + " " + backupFolder + " -u";
                                    ssh.cmd(cmd);

                                    Thread.sleep(1000);

                                    remoteFileSize = getRemoteFileSize(ssh, host, user, password, deployFolder + "/" + webAppWAR, protocol);
                                    if (remoteFileSize != 0 && remoteFileSize < 0xFFFFFFFF - 0xFF) {
                                        msg = "Error : Failed to backup current war (" + backupFolder + "/" + webAppWAR + ")<br/><br/>... maybe file was locked, not enough space or insufficient privileges...";
                                        Callback.send(msg);
                                        Messagebox.show(msg, "LiquidD", Messagebox.OK + Messagebox.ERROR);
                                        return retVal;
                                    }
                                }
                            }

                        } else {
                            Callback.send("2&deg;/5 - Backup skipped, remote file is up to date...");
                            Thread.sleep(3000);
                        }


                        if (askConfirmation) {
                            message = " Processing <b>" + cfgName + "</b></br>"
                                    + "</br>"
                                    + "<span style=\"font-size:130%\">"
                                    + "<b> Ready for deploy :</b></br>"
                                    + "</span>"
                                    + "</br>"
                                    + "</br>"
                                    + "<span style=\"font-size:80%; left:60px; position: relative;\">"
                                    + " - Undeploy app from <b>" + webAppWAR + "</b>"
                                    + "</span>"
                                    + "</br>"
                                    + "<span style=\"font-size:80%; left:60px; position: relative;\">"
                                    + " - Wait for application server ready</b>"
                                    + "</span>"
                                    + "</br>"
                                    + "<span style=\"font-size:80%; left:60px; position: relative;\">"
                                    + "</br>"
                                    + " - Deploy app from <b>" + deployFolder + "/" + webAppWAR + "</b></br>"
                                    + "</span>"
                                    + "</br>"
                                    + "<span style=\"font-size:80%; left:60px; position: relative;\">"
                                    + "</br>"
                                    + " to <b>" + deployFolder + "/" + webAppWAR + "</b></br>"
                                    + "</span>";
                            if (Messagebox.show(message, "LiquidD", Messagebox.QUESTION + Messagebox.YES + Messagebox.NO) == Messagebox.YES) {
                            } else {
                                // Stop here
                                Callback.send("Deploy not completed : operation not confirmed by user");
                                return retVal;
                            }
                        }


                        // 3° remove current war
                        //
                        // Rimozione file produzione
                        //


                        Callback.send("3&deg;/5 - Removing current file from " + deployFolder + "...");
                        cmd = "rm " + deployFolder + "/" + webAppWAR + " -f";
                        ssh.cmd(cmd);


                        Thread.sleep(1000);
                        remoteFileSize = getRemoteFileSize(ssh, host, user, password, deployFolder + "/" + webAppWAR, protocol);
                        if (remoteFileSize != 0 && remoteFileSize < (long) 0xFFFFFFFF - 0xFF) {

                            Thread.sleep(1000);

                            Callback.send("3&deg;/5 - Retry to removing current file from " + deployFolder + "...");
                            cmd = "sudo rm " + deployFolder + "/" + webAppWAR;
                            ssh.cmd(cmd);

                            Thread.sleep(1000);

                            remoteFileSize = getRemoteFileSize(ssh, host, user, password, deployFolder + "/" + webAppWAR, protocol);
                            if (remoteFileSize != 0 && remoteFileSize < 0xFFFFFFFF - 0xFF) {

                                msg = "Error : Failed to remove current war (" + deployFolder + "/" + webAppWAR + ")<br/><br/>... maybe file was locked ";
                                Callback.send(msg);
                                Messagebox.show(msg, "LiquidD", Messagebox.OK + Messagebox.ERROR);
                                return retVal;
                            }
                        }


                        //
                        // Attesa errore 404
                        //


                        Callback.send("3&deg;/5 - Waiting for application server...");
                        if (undeployWaitTime > 0) {
                            Thread.sleep(undeployWaitTime);
                        } else {
                            Thread.sleep(7000);
                        }
                        boolean isReadyForDeply = false;

                        if (webAppURL != null && !webAppURL.isEmpty()) {
                            int n = 10;
                            int code = 0;
                            utility.disableCertificateValidation();
                            for (int i = 0; i < n; i++) {
                                Object[] resURL = utility.readURL(webAppURL, "GET", null);
                                code = (int) resURL[0];
                                if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                                    isReadyForDeply = true;
                                    break;
                                } else {
                                    Thread.sleep((3000));
                                }
                                Callback.send("3&deg;/5 - Waiting for application server [" + (i + 1) + "/" + n + "]...");
                            }
                            if (code == HttpURLConnection.HTTP_OK) {
                                Callback.send("3&deg;/5 - <span style=\"color:red\">Web app still running : maybe deployFolder (" + deployFolder + "/" + webAppWAR + ") is not valid ... or 404 error redirected<span>");
                            } else {
                                Callback.send("3&deg;/5 - <span style=\"color:darkGreen\">Applicatin server ready for deploy <b>" + webAppWAR + "</b>...<span>");
                            }
                        }
                        Thread.sleep(3000);


                        if (!isReadyForDeply) {
                            message = " Processing <b>" + cfgName + "</b></br>"
                                    + "</br>"
                                    + "<span style=\"font-size:130%\">"
                                    + "<b> DANGER :</b></br>"
                                    + "</span>"
                                    + "</br>"
                                    + "</br>"
                                    + "<span style=\"font-size:110%; left:50px; position: relative;\">"
                                    + "Application Server didn't undeploy current app <b>" + webAppWAR + "</b>"
                                    + "</span>"
                                    + "</br>"
                                    + "</br>Deploy anyway to <b>" + deployFolder + "/" + webAppWAR + "</b></br>"
                                    + "</span>";
                            if (Messagebox.show(message, "LiquidD", Messagebox.ERROR + Messagebox.YES + Messagebox.NO) == Messagebox.YES) {
                            } else {
                                // Stop here
                                return retVal;
                            }
                        }

                        // 4° deploy new war
                        //
                        // Copia file nuova versione
                        //
                        if (isReadyForDeply) {
                            Callback.send("4&deg;/5 - Copying new app to application server...");
                        } else {
                            Callback.send("4&deg;/5 - Copying new app to application server (without Web App URL check)...");
                        }
                        cmd = "mv " + copyFolder + "/" + webAppWAR + " " + deployFolder + "/" + webAppWAR + "";
                        // cmd = "sudo cp " + copyFolder + "/" + webAppWAR + " " + deployFolder + "/" + webAppWAR + "";
                        ssh.cmd(cmd);

                        //
                        // Attesa
                        //
                        if (checkWaitTime > 0) {
                            Thread.sleep(checkWaitTime);
                        } else {
                            Thread.sleep(5000);
                        }

                        long copiedFileSize = getRemoteFileSize(ssh, host, user, password, deployFolder + "/" + webAppWAR, protocol);
                        if (copiedFileSize != glFileSize && copiedFileSize > 0) {

                            if ("scp".equalsIgnoreCase(protocol)) {
                                // scp cannot get file size in deploy folder, that is root owned
                            } else {
                                msg = "WARNING : remote file deployed size : " + copiedFileSize + " / uploaded file size : " + glFileSize;
                                msg += " <br/> may be cp command failed :";
                                msg += " <br/></b> " + cmd + "</b>";
                                Callback.send(msg);
                                Messagebox.show(msg, "LiquidD", Messagebox.OK + Messagebox.WARNING);
                                // return null;
                            }
                        }


                        cmd = "chown root " + deployFolder + "/" + webAppWAR + "";
                        cmd = "chgrp root " + deployFolder + "/" + webAppWAR + "";
                        cmd = "chmod 771 " + deployFolder + "/" + webAppWAR + "";


                        //
                        // 5° check web app
                        //
                        // verifica risposta
                        //
                        boolean installedSuccesfully = false;

                        Callback.send("5&deg;/5 - Checking application server...");
                        if (webAppURL != null && !webAppURL.isEmpty()) {
                            int n = 10;
                            utility.disableCertificateValidation();
                            for (int i = 0; i < n; i++) {
                                Object[] resURL = utility.readURL(webAppURL, "GET", null);
                                int code = (int) resURL[0];
                                if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_ACCEPTED) {
                                    installedSuccesfully = true;
                                    break;
                                } else {
                                    Thread.sleep((3000));
                                }
                            }
                            if (installedSuccesfully) {
                                long remoteDeployedFileSize = getRemoteFileSize(ssh, host, user, password, deployFolder + "/" + webAppWAR + "", protocol);
                                if (remoteDeployedFileSize == glFileSize) {
                                    msg_for_notity = "Deploy of " + cfgName + " done, checked and online";
                                    msg = "Deploy of " + cfgName + " <span style=\"color:darkGreen\">done, checked and online</span>";
                                    Callback.send("5&deg; - " + msg);
                                    utility.set(deplpoyBean, "lastUpdate", new Timestamp(System.currentTimeMillis()));
                                    utility.set(deplpoyBean, "counter", utility.getInt(deplpoyBean, "counter") + 1);
                                    utility.set(deplpoyBean, "lastMsg", "OK");
                                    String update_result = db.update(deplpoyBean, "LiquidX.liquidx.deploysCfg", (HttpServletRequest) request);
                                    retVal = "{ \"client\":\"Liquid.stopWaiting('deploysCfg'); Liquid.loadData('deploysCfg')\" }";
                                } else {
                                    if ("scp".equalsIgnoreCase(protocol)) {
                                        // scp cannot get file size in deploy folder, that is root owned
                                        msg_for_notity = "Deploy of " + cfgName + " done, checked and online ... Note : deployed file's size bot checked (scp doesn't allow to get file size)";
                                        msg = "Deploy of " + cfgName + " <span style=\"color:darkGreen\">deployed file's size mismath (" + remoteFileSize + "/" + glFileSize + ")<span>";
                                        Callback.send("5&deg; - " + msg);
                                        utility.set(deplpoyBean, "lastUpdate", new Timestamp(System.currentTimeMillis()));
                                        utility.set(deplpoyBean, "counter", utility.getInt(deplpoyBean, "counter") + 1);
                                        utility.set(deplpoyBean, "lastMsg", "OK");
                                        String update_result = db.update(deplpoyBean, "LiquidX.liquidx.deploysCfg", (HttpServletRequest) request);
                                        retVal = "{ \"client\":\"Liquid.stopWaiting('deploysCfg'); Liquid.loadData('deploysCfg')\" }";
                                    } else {
                                        msg_for_notity = "Deploy of " + cfgName + " done, checked and online ... Note : deployed file's size mismath (" + remoteFileSize + "/" + glFileSize + ")";
                                        msg = "Deploy of " + cfgName + " <span style=\"color:red\">deployed file's size mismath (" + remoteFileSize + "/" + glFileSize + ")<span>";
                                        Callback.send("5&deg; - " + msg);
                                        utility.set(deplpoyBean, "lastUpdate", new Timestamp(System.currentTimeMillis()));
                                        utility.set(deplpoyBean, "counter", utility.getInt(deplpoyBean, "counter") + 1);
                                        utility.set(deplpoyBean, "lastMsg", "OK(file size unchecked)");
                                        String update_result = db.update(deplpoyBean, "LiquidX.liquidx.deploysCfg", (HttpServletRequest) request);
                                        retVal = "{ \"client\":\"Liquid.stopWaiting('deploysCfg'); Liquid.loadData('deploysCfg')\" }";
                                    }
                                }
                            } else {
                                msg_for_notity = "Deploy of " + cfgName + " done but web app " + webAppWAR + " not running";
                                msg = "Deploy of " + cfgName + " <span style=\"color:red\">done but web app " + webAppWAR + " not running<span>";
                                Callback.send("5&deg; - " + msg);
                                utility.set(deplpoyBean, "lastUpdate", new Timestamp(System.currentTimeMillis()));
                                utility.set(deplpoyBean, "counter", utility.getInt(deplpoyBean, "counter") + 1);
                                utility.set(deplpoyBean, "lastMsg", "NOT RUNNING");
                                String update_result = db.update(deplpoyBean, "LiquidX.liquidx.deploysCfg", (HttpServletRequest) request);
                                retVal = "{ \"client\":\"Liquid.stopWaiting('deploysCfg'); Liquid.loadData('deploysCfg')\" }";
                            }
                            Thread.sleep(1000);
                        } else {
                            msg_for_notity = "Deploy of " + cfgName + " done but not checked";
                            msg = "Deploy of " + cfgName + " <span style=\"color:darkGray\">done but not checked<span>";
                            Callback.send("5&deg; - " + msg);
                            Thread.sleep(1000);
                            utility.set(deplpoyBean, "lastUpdate", new Timestamp(System.currentTimeMillis()));
                            utility.set(deplpoyBean, "counter", utility.getInt(deplpoyBean, "counter") + 1);
                            utility.set(deplpoyBean, "lastMsg", "NOT CHECKED");
                            String update_result = db.update(deplpoyBean, "LiquidX.liquidx.deploysCfg", (HttpServletRequest) request);
                            retVal = "{ \"client\":\"Liquid.stopWaiting('deploysCfg'); Liquid.loadData('deploysCfg')\" }";
                        }
                    } else {
                        msg_for_notity = "Upload of " + cfgName + " Failed with error:" + uploadFileError;
                        msg = "Upload of " + cfgName + " <span style=\"color:red\">Failed with error:" + uploadFileError + "<span>";
                        Callback.send("1&deg; - " + msg);
                        utility.set(deplpoyBean, "lastUpdate", new Timestamp(System.currentTimeMillis()));
                        utility.set(deplpoyBean, "counter", utility.getInt(deplpoyBean, "counter") + 1);
                        utility.set(deplpoyBean, "lastMsg", "UPLOAD ERROR:" + uploadFileError);
                        String update_result = db.update(deplpoyBean, "LiquidX.liquidx.deploysCfg", (HttpServletRequest) request);
                        retVal = "{ \"client\":\"Liquid.stopWaiting('deploysCfg'); Liquid.loadData('deploysCfg')\" }";
                    }

                    //
                    // Notiification
                    //
                    if (recipients != null) {
                        try {
                            String header = "<h1>LiquidD - WAR Deploy</h1></br></br><h4>LiquidD - WAR Deploy base on Liquid framework<br/>https://gitgub.com/cristianandreon/Liquid</h4><br/>https://gitgub.com/cristianandreon/LiquidD</h4>";
                            // TODO
                            // emailer.postMail(recipients, "Deploy notification: "+cfgName, header+msg, "email_addr");
                        } catch (Exception e) {
                        }
                    }

                    String script = "notifyMessage(\"" + msg_for_notity + "\")";
                    JSScript.script(script);

                    if (openURL) {
                        String openScript = "window.open(\"" + webAppURL + "\")";
                        JSScript.script(openScript);
                    }


                } else {
                    msg = "1&deg; - Upload of " + cfgName + " <span style=\"color:maroon\">Not confirmed by used<span>";
                    Callback.send(msg);
                }
            }

        } finally {
            try {
                if(ssh != null) {
                    String cmd = "history -c";
                    ssh.cmd(cmd);
                    ssh.close();
                }
                if(sftp != null) {
                    sftp.end();
                }
            } catch (Throwable th2){
                Logger.getLogger(deployManager.class.getName()).severe(th2.getMessage());
            }
        }
        return (Object)retVal;
    }

    public static long getRemoteFileSize(sshManager ssh, String host, String user, String password, String remoteFileName, String protocol) throws Exception {
        if ("scp".equalsIgnoreCase(protocol)) {
            if (ssh != null) {
                return ssh.getRemoteFileSize(remoteFileName);
            } else {
                // Fail if no user right
                return scpManager.getRemoteFileSize(user, password, host, 22, remoteFileName);
            }
        } else {
            return sftpManager.getRemoteFileSize(host, user, password, remoteFileName);
        }
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

    
    
    
    void testMe() {
        int a = 1;
        
        // a = a + except();
        
        try {
            
            a = a + except();
            
        } catch (Exception e) {            
        }
    }
    
    
    int except() 
            throws Exception
    {
        try {
            
            Object a = null;
            a.notify();
            return 1;
            
        } catch (Exception e) {
            // throw new Exception();
        }
        
        return 0;
    }
    
}
