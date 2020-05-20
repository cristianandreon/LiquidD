/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx.ssh;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import java.io.IOException;

/**
 *
 * @author root
 */
public class sshManager {

    private Connection conn = null;
    private Session sess = null;
    private String ip = null;
    public String usr = null;
    private String psw = null;
    ArrayList<String> errors = new ArrayList<String> ();

    // ...
    public boolean connect(String ip, String usr, String psw) {
        if (conn == null) {
            ArrayList<String> ls = new ArrayList<String>();
            try {
                
                errors.clear();
                        
                conn = new Connection(ip);
                conn.connect();
                //Effettuo l'autenticazione...
                boolean isAuthenticated = conn.authenticateWithPassword(usr, psw);
                //...e verifico che sia andata a buon fine
                if (isAuthenticated == false) {
                    errors.add("Authentication error");
                    return false;
                }
                // Creo l'oggetto Session, aprendo di fatto una sessione
                sess = conn.openSession();
                
                this.ip = ip;
                this.usr = usr;
                this.psw = psw;

            } catch (IOException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean close() {
        if (conn != null) {
            conn.close();
        }
        conn = null;

        if (sess != null) {
            sess.close();
        }
        sess = null;

        return true;
    }

    public ArrayList<String> ls(String dir) {
        return cmd("ls -r", new Object [] { dir } );
    }
    public ArrayList<String> cp(String source, String target) {
        return cmd("cp", new Object [] { source, target } );
    }
    
    public ArrayList<String> cmd( String cmd, Object [] args) {
        if (conn != null && sess != null) {
            String command = cmd;
            for(int i=0; i<args.length; i++) {
                String arg = (String)args[i];
                command += " "+arg;
            }
            return cmd(command);
        }
        return null;
    }
    public ArrayList<String> cmd( String cmd) {
        ArrayList<String> ls = new ArrayList<String>();
        if(cmd != null) {
            try {
                sess.execCommand(cmd);
                //...e ne gestisco l'output, popolando l'ArrayList
                InputStream stdout = new StreamGobbler(sess.getStdout());
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    ls.add(line);
                }
            } catch (IOException e) {
                return null;
            }
        }
        return ls;
    }
}
