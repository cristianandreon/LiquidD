/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.liquidx;

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
     
    // Per Test con la sezione events ... nel jsondel controllo : 
    //          ...
    //          "events":[ 
    //                  { "name":"onRetieve", "server":"com.liquid.event.onRetrieveRows" } 
    //                  ]
    //          ...
    //
    // Ritorna  (boolean)false, per arrestare l'esecuzione
    //          (int) 0,    per arrestare l'esecuzione
    //          (long) 0,   per arrestare l'esecuzione
    //          (String) { "nRows" : x }        per imporre un limite sul numero di righe
    //          (String) { "result" : x }       per arrestare l'esecuzione porre x <= 0
    //          (String) { "terminate" : true } per arrestare l'esecuzione
    static public Object onRetrieveRows (Object tbl_wrk, Object params, Object clientData, Object freeParam ) {
        // System.out.println(" onRetrieveRows() Raised");
        // return (Object)"{\"nRows\":2}"; // OK
        // return (Object)"{\"nRows\":2, \"title\":\"LiquidX\", \"message\":\"Resultset limited to 2 rows by onRetrieve event\"}";  // OK
        // return (Object)false;  // OK
        return (Object)true;  // OK Continua la query
    }
        
    
    static public Object onFeedbacksSelectionChanged (Object tbl_wrk, Object params, Object clientData, Object requestParam ) throws JSONException {
        if(tbl_wrk != null) {
            if(params != null) {
                if(clientData != null) {
                    // Get data (rows selected data) as JSONArray
                    JSONArray rowData = com.liquid.event.getJSONArray(params, "data" );
                    if(rowData != null) {
                        if(rowData.length()>0) {
                            // Get first selected, than get "user_id" field
                            String feedbackId = rowData.getJSONObject(0).getString("id");
                            // Set userId as preFilter
                            workspace attach_tbl_wrk = workspace.get_tbl_manager_workspace("feedbacks_attach");
                            db.set_prefilter((workspace) attach_tbl_wrk, "feedback_id", feedbackId);
                            
                            workspace message_tbl_wrk = workspace.get_tbl_manager_workspace("feedbacks_message");
                            db.set_prefilter((workspace) message_tbl_wrk, "feedback_id", feedbackId);
                            
                            // Store selection
                            ((HttpServletRequest)requestParam).getSession().setAttribute("feedbackID", feedbackId);
                            
                            return (Object)"{ \"client\":[ \"Liquid.loadData('"+"feedbacks_attach"+"');\", \"Liquid.loadData('"+"feedbacks_message"+"');\" ] }";
                        }
                    }
                }
            }
        }
        return (Object)null;
    }
       
    static public Object onInsertingMessage (Object tbl_wrk, Object params, Object clientData, Object requestParam ) throws JSONException {
        if(tbl_wrk != null) {
            if(params != null) {
                if(clientData != null) {
                    // Get data (row data) as JSONObject
                    JSONObject rowData = com.liquid.event.getJSONObject(params, "data");
                    if(rowData != null) {
                        // Set current logged userID
                        rowData.put("3", com.liquid.login.getLoggedID((HttpServletRequest) requestParam));
                        // Set current feedbackID
                        rowData.put("2", ((HttpServletRequest)requestParam).getSession().getAttribute("feedbackID"));
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
