/*
 * FBLA Mobile Application Development 2017-2018
 * KolbLibrary
 */
package mobileappdevserver.mads.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import mobileappdevserver.mads.Server;

/**
 *
 * @author Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
 */
public class PushNotification {
    // Identifiers for the PushNotification's possible icons
    public static final int ICON_OVERDUE = 0;
    public static final int ICON_CHECK = 1;
    
    // KolbLibrary's unique GCM API key to send notifications to devices.
    public static final String API_KEY = "AIzaSyDSOc7_AE4VsyzWNnUNxZNwb_RdfTCviG0";
    
    // Content of the PushNotification
    private final String title, body;
    private final int icon;
    public PushNotification(String title, String body, int icon) {
        this.title = title;
        this.body = body;
        this.icon = icon;
    }
    /**
     * 
     * @return the title of this PushNotification that appears in the recipient's notifications.
     */
    public String getTitle() {
        return title;
    }
    /**
     * 
     * @return the body of this PushNotification that appears in the recipient's notifications.
     */
    public String getBody() {
        return body;
    }
    /**
     * 
     * @return the icon of this PushNotification that appears in the recipient's notifications.
     * Examples may include ICON_OVERDUE and ICON_CHECK.
     */
    public int getIcon() {
        return icon;
    }
    /**
     * 
     * @param user
     * @return Whether or not this PushNotification could be sent to a device logged in with the
     * given user's credentials.
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException 
     */
    public boolean sendTo(String user) throws SQLException, ClassNotFoundException, IOException {
        List<String> pushkeys = Server.getInstance().getDatabase().getPushkeys(user);
        if(pushkeys.isEmpty()) {
            return false;
        }
        for(String key : pushkeys) {
            sendToKey(key);
        }
        return true;
    }
    /**
     * Sends this PushNotification with a unique key of the parameter 'token'.
     * @param token
     * @throws IOException 
     */
    private void sendToKey(String token) throws IOException {
        // Send POST request to GCM (Google Cloud Messaging) to effectively send the notification.
        
        
        URL url = new URL("https://gcm-http.googleapis.com/gcm/send");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "key=" + API_KEY);
        con.setRequestProperty("Content-type", "application/json");
        String ic;
        if(icon == ICON_OVERDUE) {
            ic = "ic_assignment_late_white_48dp";
        } else if(icon == ICON_CHECK) {
            ic = "ic_assignment_turned_in_white_48dp";
        } else {
            ic = "";
        }
        
        // The necessary payload to send to the GCM API containing the device's key
        // and the content of the notification.
        String json =
        "{\n" +
        "    \"to\" : \"" + token + "\",\n" +
        "    \"notification\" : {\n" +
        "        \"body\" : \"" + escapeIntoJSON(body) + "\",\n" +
        "        \"title\" : \"" + escapeIntoJSON(title) + "\",\n" +
        "        \"icon\" : \"" + ic + "\"\n" +
        "    }\n" +
        "}";
        con.setDoInput(true);
        con.setDoOutput(true);
        byte[] bytes = json.getBytes("UTF-8");
        OutputStream out = con.getOutputStream();
        out.write(bytes);
        out.flush();
        int httpResult = con.getResponseCode(); 
        if (httpResult == HttpsURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(
            new InputStreamReader(con.getInputStream(), "utf-8"));
            String line = null;  
            while ((line = br.readLine()) != null) {  
                 System.out.println(line);
            }
            br.close();
        } else {
            // As a debug measure in case the POST request does not go as planned
            System.out.println(con.getResponseMessage());
        }
        
    }
    /**
     * 
     * @param mess
     * @return 
     */
    private String escapeIntoJSON(String mess) {
        String ret = mess;
        ret = ret.replace("\n", "\\n");
        ret = ret.replace("\r", "\\r");
        ret = ret.replace("\t", "\\t");
        ret = ret.replace("\\", "\\\\");
        ret = ret.replace("\"", "\\\"");
        return ret;
    }
    public static PushNotification generateCheckoutApproved(String bookTitle) {
        String title = "Checked out";
        String body = "Your copy of " + bookTitle + " has been checked out.";
        int icon = ICON_CHECK;
        return new PushNotification(title, body, icon);
    }
    public static PushNotification generateReserveApproved(String bookTitle) {
        String title = "Checked out";
        String body = "Your copy of " + bookTitle + " has been reserved.";
        int icon = ICON_CHECK;
        return new PushNotification(title, body, icon);
    }
}
