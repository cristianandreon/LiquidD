/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx.deploy;

import app.liquidx.ssh.sshManager;
import ch.ethz.ssh2.Connection;
import com.jcraft.jsch.*;

import com.liquid.Callback;
import com.liquid.db;
import com.liquid.utility;
import com.liquid.workspace;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
public class deployManager implements SftpProgressMonitor {
    
    long uploadingTotal = 0;
    long uploadingCurrent = 0;

    
    static public Object deploy (Object tbl_wrk, Object params, Object clientData, Object requestParam ) throws JSONException, InterruptedException, Exception {
        if(params != null) {
            // {"params":[{"formX":[{"1":"","2":"","3":""}]},{"name":"deploy"}]}
            JSONArray rowsData = com.liquid.event.getJSONArray(params, "formX");
            JSONObject rowData = rowsData.getJSONObject(0);
            // String id = rowData.getString("1");
            String cfgId = rowData.getString("2");
            String cfgName = rowData.getString("3");
            String file = rowData.getString("4");
            String fileName = null; try { rowData.getString("file.filesName"); } catch(Exception e) {}
            String fileSize = null; try { fileSize = rowData.getString("file.filesSize"); } catch(Exception e) {}
            String controlId = "deploysCfg";

            // Lettura del bean di configurazione
            if(cfgId != null && !cfgId.isEmpty()) {
                // Object deplpoyBean = db.get_bean(requestParam, controlId, id, null, "*", null, 1);
                Object deplpoyBean = db.load_bean( (HttpServletRequest) requestParam, "LiquidX.liquidx.deploysCfg", "*", cfgId);
                if(deplpoyBean != null) {
                    String host = (String) utility.get(deplpoyBean, "host");
                    String user = (String) utility.get(deplpoyBean, "user");
                    String password = (String) utility.get(deplpoyBean, "password");
                    String sourceFile = (String) utility.get(deplpoyBean, "sourceFile");
                    String deployFolder = (String) utility.get(deplpoyBean, "deployFolder");
                    String copyFolder = (String) utility.get(deplpoyBean, "copyFolder");
                    String backupFolder = (String) utility.get(deplpoyBean, "backupFolder");
                    String webAppWAR = (String) utility.get(deplpoyBean, "webAppWAR");
                    String webAppURL = (String) utility.get(deplpoyBean, "webAppURL");
                    int deployWaitTime = (int) utility.get(deplpoyBean, "deployWaitTime");
                    int checkWaitTime = (int) utility.get(deplpoyBean, "checkWaitTime");

                    // Nome del WAR
                    webAppWAR = webAppWAR != null && !webAppWAR.isEmpty() ? webAppWAR : fileName;
        
                    Callback.send("Processing "+cfgName+" ...");
                    
                    //
                    // put file to server via SFTP
                    //
                    Callback.send("Uploading "+fileName+" " + fileSize + "bytes...");

                    boolean uploadFileOk = false;
                    String uploadFileError = "";
                    
                    boolean bDataDecoded = false;
                    InputStream sourceFileIS = null;
                    
                    if(sourceFile != null && !sourceFile.isEmpty()) {
                    	// direct acess local file
                        sourceFileIS = new FileInputStream(new File(sourceFile));
                        bDataDecoded = true;
                    } else {
                    	// uploaded from a form
                    	if(file != null && !file.isEmpty()) {
		                    int index = file.indexOf("binaryData,");
		                    if(index < 0) {
		                    	bDataDecoded = true;
		                        int index2 = file.indexOf(":");
		                    	String ContentSize = file.substring(0, index2);
		                    	int contentSize = Integer.parseInt(ContentSize);
		                    	file = file.substring(index2+1);
		                        sourceFileIS = new ByteArrayInputStream(file.getBytes(StandardCharsets.UTF_8));
		                    } else {                    
		                    	index = file.indexOf("base64,");
		                    	if(index < 0) {
		                    		Callback.send("Deploy of "+cfgName+" failed <span style=\"color:red\">invalid file format<span>");
		                    	} else {
		                    		file = utility.base64Decode(file.substring(7));
		                            sourceFileIS = new ByteArrayInputStream(file.getBytes(StandardCharsets.UTF_8));
		                    	}
		                    }
                    	}
                    }
                    if(bDataDecoded) {
                        try {
                            long retVal = onUpload( host, user, password, sourceFileIS, copyFolder+"/"+(webAppWAR) );
                            if(retVal > 0) {
                                if(fileSize != null && !fileSize.isEmpty()) {
                                    if(Long.parseLong(fileSize) == retVal) {
                                        uploadFileOk = true;
                                    }
                                } else {
                                    uploadFileOk = true;
                                }
                            }
                        } catch (JSchException ex) {
                            java.util.logging.Logger.getLogger(deployManager.class.getName()).log(Level.SEVERE, null, ex);
                            uploadFileError += ex.getLocalizedMessage();
                        } catch (SftpException ex) {
                            java.util.logging.Logger.getLogger(deployManager.class.getName()).log(Level.SEVERE, null, ex);
                            uploadFileError += ex.getLocalizedMessage();
                        }
                        
                        if(uploadFileOk) {
                        
                            Callback.send("Upload of "+fileName+" done...");
                            Thread.sleep(1000);

                            // Verifica del file caricato
                            Callback.send("Checking uploaded file...");


                            //
                            // Apertura sessione ssh
                            //
                            Callback.send("Open ssh session ...");
                            sshManager ssh = new sshManager();
                            ssh.connect(host, user, password);


                            // Adattamento webAppName
                            int idx = webAppWAR.indexOf(".war");
                            if(idx > 0) webAppWAR = webAppWAR.substring(0, idx);


                            /*
                            function getBackupDate() {
                            var date = new Date(); var d = date.getDate(); var m = date.getMonth() + 1; var y = date.getFullYear();
                            var dateString = (y) + (m <= 9 ? '0' + m : m) + (d <= 9 ? '0' + d : d);
                            return dateString;
                            }
                            getBackupDate();
                            */

                            // Risoluzione backupFolder
                            backupFolder = solve_variable_field(backupFolder);

                            // Risoluzione copyFolder
                            copyFolder = solve_variable_field(copyFolder);

                            // Risoluzione deployFolder
                            deployFolder = solve_variable_field(deployFolder);


                            Callback.send("Logging as root...");
                            String cmd = "su -";
                            ssh.cmd(cmd);
                            ssh.cmd(password);

                            //
                            // Backup file attualmente in prod
                            //
                            Callback.send("Backup file...");
                            cmd = "mkdir -p "+backupFolder;
                            ssh.cmd(cmd);
                            cmd = "cp "+deployFolder+"/"+webAppWAR+".war" + " " + backupFolder;
                            ssh.cmd(cmd);

                            //
                            // Verifica file copiato
                            //
                            Callback.send("Checking backup file in "+backupFolder+"...");

                            //
                            // Rimozione file produzione
                            //
                            Callback.send("Removing current file from "+deployFolder+"...");
                            cmd = "rm "+deployFolder+"/"+webAppWAR+".war";
                            ssh.cmd(cmd);

                            //                        
                            // Attesa errore 404
                            //
                            Callback.send("Waiting for application server...");
                            if(deployWaitTime > 0) {
                                Thread.sleep(deployWaitTime);
                            } else {
                                Thread.sleep(7000);
                            }
                            boolean isReadyForDeply = false;
                            if(webAppURL != null && !webAppURL.isEmpty()) {
                                int nt = 3;
                                for(int it=0; it<nt; it++) {
                                    URL url = new URL(webAppURL);
                                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                                    connection.setRequestMethod("GET");
                                    connection.connect();
                                    int code = connection.getResponseCode();
                                    connection.disconnect();
                                    if(code == 404) {
                                        isReadyForDeply = true;
                                        break;
                                    } else {
                                        Thread.sleep((3000));
                                    }
                                }                                
                            }

                            //
                            // Copia file nuova versione
                            //
                            if(isReadyForDeply) {
                                Callback.send("Ready for deply, copying new app to application server...");
                            } else {
                                Callback.send("Copying new app to application server (without Web App URL check)...");                            
                            }
                            cmd = "cp "+deployFolder+"/"+webAppWAR+".war "+deployFolder+"/"+webAppWAR+".war";
                            ssh.cmd(cmd);


                            //
                            // Attesa
                            //
                            if(checkWaitTime > 0) {
                                Thread.sleep(checkWaitTime);
                            } else {
                                Thread.sleep(5000);
                            }

                            //
                            // verifica risposta
                            //
                            boolean installedSuccesfully = false;
                            Callback.send("Checking application server...");
                            if(webAppURL != null && !webAppURL.isEmpty()) {
                                int nt = 3;
                                for(int it=0; it<nt; it++) {
                                    URL url = new URL(webAppURL);
                                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                                    connection.setRequestMethod("GET");
                                    connection.connect();
                                    int code = connection.getResponseCode();
                                    connection.disconnect();
                                    if(code == 200) {
                                        installedSuccesfully = true;
                                        break;
                                    } else {
                                        Thread.sleep((3000));
                                    }
                                }                                
                                if(installedSuccesfully) {
                                    Callback.send("Deploy of "+cfgName+" <span style=\"color:darkGreen\">done and checked</span>");
                                } else {
                                    Callback.send("Deploy of "+cfgName+" <span style=\"color:red\">done but not running<span>");
                                }
                                Thread.sleep(1000);
                            } else {                        
                                Callback.send("Deploy of "+cfgName+" <span style=\"color:darkGray\">done but not checked<span>");
                                Thread.sleep(1000);
                            }
                        } else {
                            Callback.send("Upload of "+cfgName+" <span style=\"color:red\">Failed with error:"+uploadFileError+"<span>");
                        }
                    }
                    
                } else {
                    Callback.send("Deploy of "+cfgName+"failed, <span style=\"color:red\">read bean error<span>");
                    return (Object)"{ \"result\":-2, \"error\":\""+utility.base64Encode("read bean error") + "\" }";                
                }
            } else {
                Callback.send("Deploy of "+cfgName+" failed, <span style=\"color:red\">primaryKey not found<span>");
                return (Object)"{ \"result\":-1, \"error\":\""+utility.base64Encode("primaryKey not found") + "\" }";                
            }
        } else {
            Callback.send("Deploy failed, <span style=\"color:red\">params not defined<span>");
            return (Object)"{ \"result\":-1, \"error\":\""+utility.base64Encode("primaryKey not found") + "\" }";                
        }
        return null;
    }

    
    static public long onUpload ( String host, String user, String password, InputStream sourceFileIS, String targetFile ) throws JSchException, SftpException {
        long retVal = 0;
        int port = 22;
        String knownHostsFilename = "/home/world/.ssh/known_hosts";        
        
        JSch jsch = new JSch();
        jsch.setKnownHosts(knownHostsFilename);
        Session session = jsch.getSession(user, host, port);
        session.setPassword(password);
        session.connect(); 
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        deployManager monitor = new deployManager();        
        sftpChannel.put(sourceFileIS, targetFile, monitor);
        sftpChannel.exit();
        session.disconnect();
        
        if(monitor.uploadingCurrent == monitor.uploadingTotal && monitor.uploadingTotal > 0) {
            retVal = monitor.uploadingCurrent;
        }

        return retVal;        
    }
    


    
    public void init(int op, java.lang.String src, java.lang.String dest, long total) {
        // System.out.println("STARTING: " + op + " " + src + " -> " + dest + " total: " + max);
        this.uploadingTotal = total;
        this.uploadingCurrent = 0;
    }

    public boolean count(long bytes) {
        for(int x=0; x < bytes; x++) {
            System.out.print("#");
        }
        this.uploadingCurrent = bytes;
        return(true);
    }

    public void end() {
        // System.out.println("\nFINISHED!");
        this.uploadingCurrent = this.uploadingTotal;
    }



    public static String solve_variable_field( String expr) throws Exception {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine js = sem.getEngineByName("JavaScript");    
        Object result = js.eval(expr);
        return (String)result;
    }

}
