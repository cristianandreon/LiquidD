/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx.deploy;

import app.liquidx.*;
import com.liquid.db;
import com.liquid.workspace;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 *
 * @author Cristitan
 */
public class event {
            
    static public Object onInserting(Object tbl_wrk, Object params, Object clientData, Object requestParam ) throws Exception {
        if(tbl_wrk != null) {
            if(params != null) {
                if(clientData != null) {
                    // Get data (row data) as JSONObject
                    JSONObject rowData = com.liquid.event.getJSONObject(params, "data");
                    if(rowData != null) {
                        // Set password as encrypted
                        rowData.put("password", "xxx");
                        if(rowData.length()>0) {
                            return (Object)"{ resultSet:["+rowData.toString()+"], \"client\":\"\" }";
                        }
                    }
                }
            }
        }
        return (Object)null;
    }
}
