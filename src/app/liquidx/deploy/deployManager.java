/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx.deploy;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.liquid.sshManager;
import com.liquid.sftpManager;

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
public class deployManager {

    static String glSourceFile = "";
    static String glTtargetFile = "";
    static long glFileSize = 0;
    static float maxSpeed = 0.0f;
    static float minSpeed = 0.0f;

    static public Object deploy(Object tbl_wrk, Object params, Object clientData, Object requestParam) throws JSONException, InterruptedException, Exception {
        try {
            if (params != null) {
                // {"params":[{"formX":[{"1":"","2":"","3":""}]},{"name":"deploy"}]}
                Object nameParam = com.liquid.event.getObject(params, "name");
                JSONArray rowsData = com.liquid.event.getJSONArray(params, "formX");
                String cfgId = null, cfgName = null, fileName = null, fileSize = null, file = null;
                if (rowsData != null) {
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
                    String controlId = "deploysCfg";
                } else {
                    JSONObject rowData = com.liquid.event.getJSONObject(params, "data");
                    cfgId = rowData.getString("1");
                }

                // Lettura del bean di configurazione
                if (cfgId != null && !cfgId.isEmpty()) {
                    // Object deplpoyBean = db.get_bean(requestParam, controlId, id, null, "*", null, 1);
                    Object deplpoyBean = db.load_bean((HttpServletRequest) requestParam, "LiquidX.liquidx.deploysCfg", "*", cfgId);
                    if (deplpoyBean != null) {
                        cfgName = (String) utility.get(deplpoyBean, "name");
                        String host = (String) utility.get(deplpoyBean, "host");
                        String user = (String) utility.get(deplpoyBean, "user");
                        String password = (String) utility.get(deplpoyBean, "password");
                        String sourceFile = (String) utility.get(deplpoyBean, "sourceFile");
                        String deployFolder = (String) utility.get(deplpoyBean, "deployFolder");
                        String copyFolder = (String) utility.get(deplpoyBean, "copyFolder");
                        String backupFolder = (String) utility.get(deplpoyBean, "backupFolder");
                        String webAppWAR = (String) utility.get(deplpoyBean, "webAppWAR");
                        String webAppURL = (String) utility.get(deplpoyBean, "webAppURL");
                        int undeployWaitTime = (int) utility.get(deplpoyBean, "undeployWaitTime");
                        int checkWaitTime = (int) utility.get(deplpoyBean, "checkWaitTime");
                        String notifyEmails = (String) utility.get(deplpoyBean, "notifyEmails");

                        // Nome del WAR
                        webAppWAR = webAppWAR != null && !webAppWAR.isEmpty() ? webAppWAR : fileName;

                        String webApp = webAppWAR.substring(0, webAppWAR.lastIndexOf('.'));

                        // Adattamento webAppName
                        webAppWAR = webApp + ".war";

                        Callback.send("Processing " + cfgName + " ...");

                        //
                        // put file to server via SFTP
                        //
                        boolean uploadFileOk = false;
                        String uploadFileError = "";

                        boolean bDataDecoded = false;
                        InputStream sourceFileIS = null;

                        if (sourceFile != null && !sourceFile.isEmpty()) {
                            // direct acess local file
                            File f = new File(sourceFile);
                            if (f != null) {
                                sourceFileIS = new FileInputStream(new File(sourceFile));
                                if (sourceFileIS != null) {
                                    bDataDecoded = true;
                                    fileSize = String.valueOf(f.length());
                                }
                                Callback.send("1&deg; - Uploading " + sourceFile + " " + (f.length() / 1024 / 1024) + "MB...");
                                glSourceFile = sourceFile;
                                glFileSize = f.length();
                            } else {
                                String err = "source file not accessible";
                                Callback.send("1&deg; - Deploy of " + cfgName + "failed, <span style=\"color:red\">" + err + "<span>");
                                return (Object) "{ \"result\":-2, \"error\":\"" + utility.base64Encode(err) + "\" }";
                            }
                        } else {
                            // uploaded from a form
                            Callback.send("1&deg; - Uploading " + fileName + " " + fileSize + "bytes...");
                            if (file != null && !file.isEmpty()) {
                                int index = file.indexOf("binaryData,");
                                if (index < 0) {
                                    bDataDecoded = true;
                                    int index2 = file.indexOf(":");
                                    String ContentSize = file.substring(0, index2);
                                    int contentSize = Integer.parseInt(ContentSize);
                                    file = file.substring(index2 + 1);
                                    sourceFileIS = new ByteArrayInputStream(file.getBytes(StandardCharsets.UTF_8));
                                    glSourceFile = "";
                                    glFileSize = contentSize;
                                } else {
                                    index = file.indexOf("base64,");
                                    if (index < 0) {
                                        Callback.send("1&deg; - Deploy of " + cfgName + " failed <span style=\"color:red\">invalid file format<span>");
                                    } else {
                                        file = utility.base64Decode(file.substring(7));
                                        File f = new File(file);
                                        if (f != null) {
                                            fileSize = String.valueOf(f.length());
                                            sourceFileIS = new ByteArrayInputStream(file.getBytes(StandardCharsets.UTF_8));
                                            glSourceFile = "";
                                            glFileSize = f.length();
                                        }
                                    }
                                }
                            }
                        }

                        if (bDataDecoded) {

                            /*
function getFolderDateName() { var date = new Date(); var d = date.getDate(); var m = date.getMonth() + 1; var y = date.getFullYear(); var dateString = (y) + (m <= 9 ? '0' + m : m) + (d <= 9 ? '0' + d : d); return dateString; }
"/home/%user%/rilasci/%webApp%/getFolderDateName();
*/
	
	                       
	                        // InputStream inputStream = new FileInputStream(filePath);
	                        String ver = (String)utility.getArchiveXMLTag(sourceFile, "WEB-INF/product.xml", "product/version");
	                        
	                        String desc_file = "" + ver;
	                        String desc_content = "" + (String)utility.getArchiveFile(sourceFile, "WEB-INF/product.xml", "product/version");;
	                    
	                    	
	                        // Risoluzione backupFolder
	                        backupFolder = solve_variable_field(backupFolder, user, webApp);
	
	                        // Risoluzione copyFolder
	                        copyFolder = solve_variable_field(copyFolder, user, webApp);
	
	                        // Risoluzione deployFolder
	                        deployFolder = solve_variable_field(deployFolder, user, webApp);
	
	                        // Strip last /
	                        if(backupFolder.endsWith("/")) backupFolder = backupFolder.substring(9, deployFolder.length()-1);
	                        if(copyFolder.endsWith("/")) copyFolder = copyFolder.substring(9, deployFolder.length()-1);
	                        if(deployFolder.endsWith("/")) deployFolder = deployFolder.substring(9, deployFolder.length()-1);
	                        
	                        String message = " Processing <b>"+cfgName+"</b></br>"
	                        		+"</br>"
	                        		+"<span style=\"font-size:90%\">"
	                        		+"file : <b>"+sourceFile+"</b></br>"
	                        		+"</span>"
	                        		+"</br>"
	                        		+"</br>"
	                        		+"<span style=\"font-size:80%; left:60px; position: relative;\">"
	                        		+"</br>Copy  to <b>"+copyFolder+"/"+webAppWAR+"</b></br>"
	                        		+"</br>"
	                        		+"</br>Backup to <b>"+backupFolder+"/"+webAppWAR+"</b></br>"
	                        		+"</br>"
	                        		+"</br>Deploy to <b>"+deployFolder+"/"+webAppWAR+"</b></br>"
	                        		+"</span>"
	                        		;
	                        
	                        if(Messagebox.show(message, "LiquidD", Messagebox.QUESTION+Messagebox.YES+Messagebox.NO) == Messagebox.YES) {	                        
		                        
		                        // 1° upload
		                        sftpManager sftp = new sftpManager();
		                        boolean doBackup = true;
		                        long retVal = 0;
		                        try {
		                        	Object [] result = sftp.upload( host, user, password, glSourceFile, sourceFileIS, copyFolder+"/"+webAppWAR );
		                            retVal = (long)result[0];
		                            doBackup = (boolean)result[1]; 
		                            if(retVal > 0) {
		                                if(fileSize != null && !fileSize.isEmpty()) {
		                                    if(Long.parseLong(fileSize) == retVal) {
		                                        uploadFileOk = true;
		                                        // upload descriptor
		        	                    		InputStream descFileIS = new ByteArrayInputStream(desc_content.getBytes());                       
		        	                            sftp.upload( host, user, password, null, descFileIS, copyFolder+"/"+(desc_file) );
		                                    }
		                                } else {
		                                    uploadFileOk = true;
		                                }
		                            } else {
		                            	uploadFileError = "File has zero size";
		                            }
		                        } catch (JSchException ex) {
		                            java.util.logging.Logger.getLogger(deployManager.class.getName()).log(Level.SEVERE, null, ex);
		                            uploadFileError += ex.getLocalizedMessage();
		                        } catch (SftpException ex) {
		                            java.util.logging.Logger.getLogger(deployManager.class.getName()).log(Level.SEVERE, null, ex);
		                            uploadFileError += ex.getLocalizedMessage();
		                        }
		                        
	                    		String recipients[] = notifyEmails != null && !notifyEmails.isEmpty() ? notifyEmails.split(",") : null;
	                        	String msg = null;
		                        
		                        if(uploadFileOk) {                        
		
		                            // Verifica del file caricato
		                            Callback.send("1&deg;/5 - Checking uploaded file...");
		                            long currentFileSize = sftp.getRemoteFileSize ( host, user, password, copyFolder+"/"+webAppWAR );
		                            if(currentFileSize != retVal) {
	                                	msg = "Error :Failed to upload current war ("+copyFolder+"/"+webAppWAR+")<br/><br/>... size mismath : "+currentFileSize+"/"+retVal+"";
		                                Callback.send(msg);
		                                Messagebox.show(msg, "LiquidD", Messagebox.OK + Messagebox.ERROR);
		                                return null;
		                            }
		
		
		                            //
		                            // Apertura sessione ssh
		                            //
		                            Callback.send("1&deg;/5 - Open ssh session ...");
		                            sshManager ssh = new sshManager();
		                            ssh.connect(host, user, password);
		
		
		
		
		                            Callback.send("1&deg;/5 - Logging as root...");
		                            String cmd = "su -";
		                            ssh.cmd(cmd, password);
		
		                            
			                        // 2° backup
		                            //
		                            // Backup file attualmente in prod
		                            //
		                            if(doBackup) {
			                            Callback.send("2&deg;/5 - Backup file (only if missing or older)...");
			                            cmd = "mkdir -p "+backupFolder;
			                            ssh.cmd(cmd);
			                            cmd = "cp "+deployFolder+"/"+webAppWAR+ " " + backupFolder + " -u";
			                            ssh.cmd(cmd);
			
			                            //
			                            // Verifica file copiato
			                            //
			                            Callback.send("2&deg;/5 - Checking backup file in "+backupFolder+"...");
			                            cmd = "ls "+backupFolder+"/"+webAppWAR+" -al";
			                            ssh.cmd(cmd);
		                            } else {
			                            Callback.send("2&deg;/5 - Backup skipped, remote file is up to date...");
			                            Thread.sleep(3000);
		                            }
		                            
		                            
			                        // 3° remove current war
		                            //
		                            // Rimozione file produzione
		                            //
		                            Callback.send("3&deg;/5 - Removing current file from "+deployFolder+"...");
		                            cmd = "sudo rm "+deployFolder+"/"+webAppWAR;
		                            ssh.cmd(cmd, password);
		
		                            currentFileSize = sftp.getRemoteFileSize ( host, user, password, deployFolder+"/"+webAppWAR );
		                            if(currentFileSize != 0) {
	                                	msg = "Error :Failed to remove current war ("+deployFolder+"/"+webAppWAR+")<br/><br/>... maybe file was locked ";
		                                Callback.send(msg);
		                                Messagebox.show(msg, "LiquidD", Messagebox.OK + Messagebox.ERROR);
		                                return null;
		                            }

		                            
	
		                            
		                            //                        
		                            // Attesa errore 404
		                            //
		                            Callback.send("3&deg;/5 - Waiting for application server...");
		                            if(undeployWaitTime > 0) {
		                                Thread.sleep(undeployWaitTime);
		                            } else {
		                                Thread.sleep(7000);
		                            }
		                            boolean isReadyForDeply = false;
		                            
	                            	
		                            		
		                            if(webAppURL != null && !webAppURL.isEmpty()) {
		                            	int n = 10;
		                            	int code = 0;
			                            utility.disableCertificateValidation();
			                            for(int i=0; i<n; i++) {
				                            Object [] resURL = utility.readURL( webAppURL, "GET", null );
				                            code = (int)resURL[0];
		                                    if(code == HttpURLConnection.HTTP_NOT_FOUND) {
		                                        isReadyForDeply = true;
		                                        break;
		                                    } else {
		                                        Thread.sleep((3000));
		                                	}
			                            }
	                                    if(code == HttpURLConnection.HTTP_OK) {
	                                    	Callback.send("3&deg;/5 - <span style=\"color:red\">Web app still running : maybe deployFolder ("+deployFolder+"/"+webAppWAR+") is not valid<span>");
	                                    } else {
	                                    	Callback.send("3&deg;/5 - <span style=\"color:darkGreen\">Applicatin server ready for install <b>"+webAppWAR+"</b>...<span>");
	                                    }
		                            }
		                            Thread.sleep(3000);
		
		                            
		                            
		                            // 4° deploy new war
		                            //
		                            // Copia file nuova versione
		                            //
		                            if(isReadyForDeply) {
		                                Callback.send("4&deg;/5 - Ready for deply, copying new app to application server...");
		                            } else {
		                                Callback.send("4&deg;/5 - Copying new app to application server (without Web App URL check)...");                            
		                            }
		                            cmd = "sudo cp "+copyFolder+"/"+webAppWAR+" "+deployFolder+"/"+webAppWAR+"";
		                            ssh.cmd(cmd, password);
		
		
		                            //
		                            // Attesa
		                            //
		                            if(checkWaitTime > 0) {
		                                Thread.sleep(checkWaitTime);
		                            } else {
		                                Thread.sleep(5000);
		                            }
		
		                            
		                            long copiedFileSize = sftp.getRemoteFileSize ( host, user, password, deployFolder+"/"+webAppWAR );
		                            if(copiedFileSize != glFileSize) {
	                                	msg = "Error : remote file deployed sie : "+copiedFileSize+" / uploaded file size : "+fileSize;
		                                Callback.send(msg);
		                                Messagebox.show(msg, "LiquidD", Messagebox.OK + Messagebox.ERROR);
		                                return null;
		                            }
		                            
		                            
		                            // 5° check web app	                            
		                            //
		                            // verifica risposta
		                            //
		                            boolean installedSuccesfully = false;
	
	                            	Callback.send("5&deg;/5 - Checking application server...");
		                            if(webAppURL != null && !webAppURL.isEmpty()) {
		                            	int n = 10;	                            	
			                            utility.disableCertificateValidation();
			                            for(int i=0; i<n; i++) {
				                            Object [] resURL = utility.readURL( webAppURL, "GET", null );
				                            int code = (int)resURL[0];			                            
		                                    if(code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_ACCEPTED) {
		                                    	installedSuccesfully = true;
		                                        break;
		                                    } else {	                                    	
		                                        Thread.sleep((3000));
		                                	}
		                                }
			                            if(installedSuccesfully) {
		                                	long remoteFileSize = sftp.getRemoteFileSize ( host, user, password, deployFolder+"/"+webAppWAR+"" );
		                                	if(remoteFileSize == glFileSize) {
		                                		 msg = "5&deg; - Deploy of "+cfgName+" <span style=\"color:darkGreen\">done, checked and online</span>";
		                                		Callback.send(msg);
		                                	} else {
		                                		msg = "5&deg; - Deploy of "+cfgName+" <span style=\"color:red\">deployed file's size mismath ("+remoteFileSize+"/"+glFileSize+")<span>";
			                                    Callback.send(msg);
		                                	}
		                                } else {
		                                	msg = "5&deg; - Deploy of "+cfgName+" <span style=\"color:red\">done but web app "+webAppWAR+" not running<span>";
		                                    Callback.send(msg);
		                                }
		                                Thread.sleep(1000);
		                            } else {                        
	                                	msg = "5&deg; - Deploy of "+cfgName+" <span style=\"color:darkGray\">done but not checked<span>";
		                                Callback.send(msg);
		                                Thread.sleep(1000);
		                            }
		                        } else {
		                        	msg = "1&deg; - Upload of "+cfgName+" <span style=\"color:red\">Failed with error:"+uploadFileError+"<span>";
		                            Callback.send(msg);
		                        }
		                        
		                        //
		                        // Notiification
		                        //
		                        if(recipients != null) {
		                        	String header = "<h1>LiquidD - WAR Deploy</h1></br></br><h4>LiquidD - WAR Deploy base on Liquid framework<br/>https://gitgub.com/cristianandreon/Liquid</h4><br/>https://gitgub.com/cristianandreon/LiquidD</h4>";
		                        	emailer.postMail(recipients, "Deploy notification: "+cfgName, header+msg, "info@cristianandreon.eu");
		                        }
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
    	} catch (Throwable th) {
    		String err = "Error:"+th.getLocalizedMessage();
            Callback.send("Deploy failed, <span style=\"color:red\">"+err+"<span>");
            return (Object)"{ \"result\":-1, \"error\":\""+utility.base64Encode(err) + "\" }";                
    	}
        return null;
    }

    public static String solve_variable_field(String expr, String user, String webApp) throws Exception {
        if (expr != null && !expr.isEmpty()) {
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
