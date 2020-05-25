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
import com.liquid.db;
import com.liquid.emailer;
import com.liquid.utility;
import com.sun.net.ssl.HttpsURLConnection;
import com.sun.net.ssl.SSLContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sun.security.provider.SecureRandom;

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
    
    static public Object deploy (Object tbl_wrk, Object params, Object clientData, Object requestParam ) throws JSONException, InterruptedException, Exception {
    	try {
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
	                    String mailingList = (String) utility.get(deplpoyBean, "mailingList");
	
	                    // Nome del WAR
	                    webAppWAR = webAppWAR != null && !webAppWAR.isEmpty() ? webAppWAR : fileName;
	                    
	                    String webApp = webAppWAR.substring(0, webAppWAR.lastIndexOf('.')); 

	                    // Adattamento webAppName
                        webAppWAR = webApp+".war";
	                    
	        
	                    Callback.send("Processing "+cfgName+" ...");
	                    
	                    //
	                    // put file to server via SFTP
	                    //
	
	                    boolean uploadFileOk = false;
	                    String uploadFileError = "";
	                    
	                    boolean bDataDecoded = false;
	                    InputStream sourceFileIS = null;
	                    
	                    if(sourceFile != null && !sourceFile.isEmpty()) {
	                    	// direct acess local file
	                    	File f = new File(sourceFile);
	                    	if(f != null) {
		                        sourceFileIS = new FileInputStream(new File(sourceFile));
		                        if(sourceFileIS != null) {
		                        	bDataDecoded = true;
		                        	fileSize = String.valueOf( f.length() );
		                        }
	                    		Callback.send("1&deg; - Uploading "+sourceFile+" " + (f.length() / 1024 / 1024) + "MB...");
	                    	    glSourceFile = sourceFile;
	                    	    glFileSize = f.length();
	                    	} else {
	                    		String err = "source file not accessible";
	    	                    Callback.send("1&deg; - Deploy of "+cfgName+"failed, <span style=\"color:red\">"+err+"<span>");
	    	                    return (Object)"{ \"result\":-2, \"error\":\""+utility.base64Encode(err) + "\" }";                
	                    	}
	                    } else {
	                    	// uploaded from a form
	                    	Callback.send("1&deg; - Uploading "+fileName+" " + fileSize + "bytes...");
	                    	if(file != null && !file.isEmpty()) {
			                    int index = file.indexOf("binaryData,");
			                    if(index < 0) {
			                    	bDataDecoded = true;
			                        int index2 = file.indexOf(":");
			                    	String ContentSize = file.substring(0, index2);
			                    	int contentSize = Integer.parseInt(ContentSize);
			                    	file = file.substring(index2+1);
			                        sourceFileIS = new ByteArrayInputStream(file.getBytes(StandardCharsets.UTF_8));
		                    	    glSourceFile = "";
		                    	    glFileSize = contentSize;
			                    } else {                    
			                    	index = file.indexOf("base64,");
			                    	if(index < 0) {
			                    		Callback.send("1&deg; - Deploy of "+cfgName+" failed <span style=\"color:red\">invalid file format<span>");
			                    	} else {
			                    		file = utility.base64Decode(file.substring(7));
			                    		File f = new File(file);
			                    		if(f != null) {
			                    			fileSize = String.valueOf( f.length() );
			                    			sourceFileIS = new ByteArrayInputStream(file.getBytes(StandardCharsets.UTF_8));			                            
			                    			glSourceFile = "";
			                    			glFileSize = f.length();
			                    		}
			                    	}
			                    }
	                    	}
	                    }
	                    
	                    if(bDataDecoded) {
	                    	
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
	
	                        // 1° upload
	                        sftpManager sftp = new sftpManager();
	                        try {
	                            long retVal = sftp.upload( host, user, password, glSourceFile, sourceFileIS, copyFolder+"/"+(webAppWAR) );
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
	                            }
	                        } catch (JSchException ex) {
	                            java.util.logging.Logger.getLogger(deployManager.class.getName()).log(Level.SEVERE, null, ex);
	                            uploadFileError += ex.getLocalizedMessage();
	                        } catch (SftpException ex) {
	                            java.util.logging.Logger.getLogger(deployManager.class.getName()).log(Level.SEVERE, null, ex);
	                            uploadFileError += ex.getLocalizedMessage();
	                        }
	                        
                    		String recipients[] = mailingList != null && !mailingList.isEmpty() ? mailingList.split(",") : null;
                        	String msg = null;
	                        
	                        if(uploadFileOk) {                        
	
	                            // Verifica del file caricato
	                            Callback.send("1&deg; - Checking uploaded file...");
	
	
	                            //
	                            // Apertura sessione ssh
	                            //
	                            Callback.send("1&deg; - Open ssh session ...");
	                            sshManager ssh = new sshManager();
	                            ssh.connect(host, user, password);
	
	
	
	
	                            Callback.send("1&deg; - Logging as root...");
	                            String cmd = "su -";
	                            ssh.cmd(cmd, password);
	
		                        // 2° backup
	                            //
	                            // Backup file attualmente in prod
	                            //
	                            Callback.send("2&deg; - Backup file...");
	                            cmd = "mkdir -p "+backupFolder;
	                            ssh.cmd(cmd);
	                            cmd = "cp "+deployFolder+"/"+webAppWAR+ " " + backupFolder;
	                            ssh.cmd(cmd);
	
	                            //
	                            // Verifica file copiato
	                            //
	                            Callback.send("2&deg; - Checking backup file in "+backupFolder+"...");
	                            cmd = "ls "+backupFolder+"/"+webAppWAR+" -al";
	                            ssh.cmd(cmd);
	                            
	                            
	                            
		                        // 3° remove current war
	                            //
	                            // Rimozione file produzione
	                            //
	                            Callback.send("3&deg; - Removing current file from "+deployFolder+"...");
	                            cmd = "rm "+deployFolder+"/"+webAppWAR;
	                            ssh.cmd(cmd);
	
	                            

	                            
	                            //                        
	                            // Attesa errore 404
	                            //
	                            Callback.send("3&deg; - Waiting for application server...");
	                            if(deployWaitTime > 0) {
	                                Thread.sleep(deployWaitTime);
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
                                    	Callback.send("3&deg; - <span style=\"color:red\">Web app still running : maybe deployFolder ("+deployFolder+"/"+webAppWAR+") is not valid<span>");
                                    } else {
                                    	Callback.send("3&deg; - <span style=\"color:darkGreen\">Applicatin server ready for install <b>"+webAppWAR+"</b>...<span>");
                                    }
	                            }
	                            Thread.sleep(3000);
	
	                            
	                            
	                            // 4° deploy new war
	                            //
	                            // Copia file nuova versione
	                            //
	                            if(isReadyForDeply) {
	                                Callback.send("4&deg; - Ready for deply, copying new app to application server...");
	                            } else {
	                                Callback.send("4&deg; - Copying new app to application server (without Web App URL check)...");                            
	                            }
	                            cmd = "cp "+copyFolder+"/"+webAppWAR+" "+deployFolder+"/"+webAppWAR+"";
	                            ssh.cmd(cmd);
	
	
	                            //
	                            // Attesa
	                            //
	                            if(checkWaitTime > 0) {
	                                Thread.sleep(checkWaitTime);
	                            } else {
	                                Thread.sleep(5000);
	                            }
	
	                            // 5° check web app	                            
	                            //
	                            // verifica risposta
	                            //
	                            boolean installedSuccesfully = false;

                            	Callback.send("5&deg; - Checking application server...");
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
	                                		 msg = "5&deg; - Deploy of "+cfgName+" <span style=\"color:darkGreen\">done and checked</span>";
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




    public static String solve_variable_field( String expr, String user, String webApp) throws Exception {
    	if(expr != null && !expr.isEmpty()) {
	        ScriptEngineManager sem = new ScriptEngineManager();
	        ScriptEngine js = sem.getEngineByName("JavaScript");
	        Object result = null;
	        String sResult = expr;
	        try {
	        	result = js.eval(expr);
	        	sResult = (String)result;
	        } catch (Throwable th) {
	        }
	        
	        return sResult.replace("${user}", user).replace("${webApp}", webApp).replace("%user%", user).replace("%webApp%", webApp);
    	}
    	return expr;
    }

}
