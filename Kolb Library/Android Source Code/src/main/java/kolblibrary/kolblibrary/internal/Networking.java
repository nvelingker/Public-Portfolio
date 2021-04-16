/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.internal;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbRequest;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.CheckBox;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import java.io.InputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kolblibrary.kolblibrary.R;
import kolblibrary.kolblibrary.adapters.UserInfoRDetails;
import kolblibrary.kolblibrary.objects.UserInfo;
import kolblibrary.kolblibrary.adapters.UserInfoCDetails;
import kolblibrary.kolblibrary.objects.Book;
import kolblibrary.kolblibrary.useractivities.CatalogSearchActivity;
import kolblibrary.kolblibrary.useractivities.CatalogSearchResult;
import kolblibrary.kolblibrary.useractivities.LoginActivity;
import kolblibrary.kolblibrary.useractivities.UserHomeScreenActivity;


public class Networking {
    private static boolean connected = false;
    private static List<byte[]> messages = new ArrayList<>();
    private static NetThread netThread = null;
    private static final char NET_SEP = (char) 0x00;
    private static String password;
    public static final Object MESSAGE_LOCK = new Object();
    private static GoogleSignInClient mGoogleSignInClient = null;

   /**
    * Connects to the remote server, the main source of book and user information.
    * @param timeout
    */
    public static void connect(int timeout) {
        netThread = new NetThread("kolb.bridge-network.net", 55151, timeout);
        new Thread(netThread).start();
    }
   /**
    * Waits for a successful connection to the server, timing out after a given amount of milliseconds.
    * @param timeout
    * @return true, if the connection is successful, and false otherwise
    */
    public static boolean waitForConnect(int timeout) {
        long timeBegin = System.currentTimeMillis();
        while(timeBegin - System.currentTimeMillis() <= timeout) {
            // chill for a while
            if(connected) return true;
        }
        return false;
    }
   /**
    * Informs the server that the connection has been successful.
    */
    public static void connectSuccess() {
        connected = true;
    }
   /**
    * Disconnects from the remote server.
    */
    public static void disconnect() {
        connected = false;
        try {
            netThread.getSocket().close();
        } catch(Exception e) {}
        Log.e("Disconnect", "Network disconnected");
        synchronized (MESSAGE_LOCK) {
            messages.clear();
        }
    }
    private static byte[] buildMessage(String... messages) {
        String buildM = "";
        for(int i = 0; i < messages.length; i++) {
            buildM += messages[i] + NET_SEP;
        }
        return buildM.getBytes();
    }
   /**
    * Signs the user out of his or her account.
    * @param ctx
    */
    public static void logout(Context ctx) {
        try{
            mGoogleSignInClient.signOut();
            Log.d("beebwoop","Aloe Blacc left the stage.");
        }catch(Exception eg){eg.printStackTrace();Log.d("beebwoop", "Google sign-out failed.  This is probaby because the user is not signed in under a google account or their was a connection error.");}
        final SharedPreferences prefs = LoginActivity.loginPrefs;
        SharedPreferences.Editor e = prefs.edit();
        e.putString("username", null);
        e.putString("password", null);
        e.commit();
        disconnect();
        connect(2000);
        Intent userHome = new Intent(ctx, LoginActivity.class);
        ctx.startActivity(userHome);
    }
   /**
    * Attempts to verify the given credentials.
    * @param user, pass
    * @return null if the verification is successful, the error message as a String if not.
    */
    public static String sendCredentials(String user, String pass) {
        if(!Networking.isConnected()) {
            if(!Networking.waitForConnect(3000)) {
                Networking.connect(3000);
                if(!Networking.waitForConnect(3000)) {
                    Log.d("Message", "NULL...");
                    return LoginActivity.getInstance().getString(R.string.network_unavailable);
                }
            }
        }
        byte[] message = buildMessage("login", user, pass);
        if(!sendMessage(message)) {
            return LoginActivity.getInstance().getString(R.string.network_unavailable);
        }
        byte[] response;
        try {
            response = waitForMessage(2000);
        } catch(NetworkTimedOutException ne) {
            Log.d("Message", "NULL RESPONSE FROM CREDENTIALS");
            return LoginActivity.getInstance().getString(R.string.credentials_timeout);
        }
            String resp = new String(response);
            Log.d("Message", "response from credentials: " + resp);
            if(resp.equalsIgnoreCase("accepted")) {
                return null;
            } else if(resp.equalsIgnoreCase("denied")) {
                return LoginActivity.getInstance().getString(R.string.credentials_denied);
            } else {
                return LoginActivity.getInstance().getString(R.string.credentials_weird);
            }

    }
    public static String sendUpdate() {
        byte[] message = buildMessage("update");
        if(!sendMessage(message)) {
            return LoginActivity.getInstance().getString(R.string.network_unavailable);
        }
        byte[] response;
        try {
            response = waitForMessage(2000);
        } catch(NetworkTimedOutException ne) {
            Log.d("Message", "NULL RESPONSE FROM CREDENTIALS");
            return LoginActivity.getInstance().getString(R.string.credentials_timeout);
        }
        String resp = new String(response);
        Log.d("Message", "response from credentials: " + resp);
        if(resp.equalsIgnoreCase("accepted")) {
            return null;
        } else if(resp.equalsIgnoreCase("denied")) {
            return LoginActivity.getInstance().getString(R.string.credentials_denied);
        } else {
            return LoginActivity.getInstance().getString(R.string.credentials_weird);
        }
    }
    private static boolean isUpdating = false;
    public static void update(Activity current, Intent goal) {
        //if(!isUpdating) {
            UI.doLoadingDialog(current);
            isUpdating = true;
            new DoUpdateTask(current, goal).execute(null,  null);
      //  }
    }
    public static class DoUpdateTask extends AsyncTask<String, Void, Void> {
        private boolean success = false;
        private Activity from;
        private Intent goal;
        public DoUpdateTask(Activity from, Intent goal) {
            this.from = from;
            this.goal = goal;
        }

        /**
         * Run background thread to get search results
         */

        protected Void doInBackground(String... urls) {
            try {
                Networking.doUpdateInternal();
                success = true;
            } catch(Networking.NetworkTimedOutException ne) {
                ne.printStackTrace();
                UI.networkTimedOut(from);
            } catch(RuntimeException re) { re.printStackTrace(); UI.networkTimedOut(from);}
            return null;
        }


        /**
         * This method updates the CatalogSearchResult screen when background processing is complete
         */
        protected void onPostExecute(Void result) {
            if(isUpdating) {
                isUpdating = false;
            }
            from.startActivity(goal);
            UI.closeLoadingDialog();
        }
    }
    public static void doUpdateInternal() {
        String response = Networking.sendUpdate();
        //String response = null;
        if(response == null) {
            // move on to next page
            try {
                SelfUserInfo.firstName = new String(Networking.waitForMessage(1000));
                SelfUserInfo.lastName = new String(Networking.waitForMessage(1000));
                String temptype = new String(Networking.waitForMessage(1000));
                if(temptype.equals("Administrator")){
                    SelfUserInfo.userType = 2;
                }
                if(temptype.equals("Teacher")){
                    SelfUserInfo.userType = 1;
                }
                if(temptype.equals("Student")){
                    SelfUserInfo.userType = 0;
                }
                Log.d("message", "usertype:" + SelfUserInfo.userType);
                SelfUserInfo.isNewUser = false;
                SelfUserInfo.reservedBooks = Networking.waitForBooks(2000);
                SelfUserInfo.checkedBooks = Networking.waitForBooks(2000);
                if(SelfUserInfo.userType == 2) {
                    AdminInfo.reserverequests = Networking.waitForRequests(2000);
                    AdminInfo.checkoutrequests = Networking.waitForRequests(2000);
                    AdminInfo.allcheckouts = Networking.waitForAllCheckouts(2000);
                    AdminInfo.userslist = Networking.waitForUsers(2000);
                }
            } catch(Networking.NetworkTimedOutException ne) {
                ne.printStackTrace();
            }

        } else {
            throw new RuntimeException("Error");
        }
    }
   /**
    * Waits for the server to send a list of books and metadata.
    * @param timeout
    * @return An ArrayList of books received, in order, from the server, or null if an error occurred.
    */
    public static ArrayList<Book> waitForBooks(int timeout) {
        byte[] bookAmount = waitForMessage(timeout);
        int amountBooks = Integer.valueOf((new String(bookAmount)));
        try {
            ArrayList<Book> books = new ArrayList<>();
            int nBooks = 0;
            while(hasMessage(timeout) && nBooks < amountBooks) {
                String title = new String(waitForMessage(timeout));
                String author = new String(waitForMessage(timeout));
                String subject = new String(waitForMessage(timeout));
                String description = new String(waitForMessage(timeout));
                String isbn = new String(waitForMessage(timeout));
                String status = new String(waitForMessage(timeout));
                String daysUntilAvailable = new String(waitForMessage(timeout));
                String daysUntilMustReturn = new String(waitForMessage(timeout));
                String dateRequested = new String(waitForMessage(timeout));
                String year = new String(waitForMessage(timeout));
                String publisher = new String(waitForMessage(timeout));
                if(!daysUntilMustReturn.trim().isEmpty()) {
                    int i = Integer.valueOf(daysUntilMustReturn);
                    Date d;
                    try {
                        d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateRequested.substring(0, 19));
                    } catch (ParseException e) { e.printStackTrace(); d = new Date(0); }
                    Log.d("MessageSZ", SelfUserInfo.checkedBooksInfo.size()+"");
                    for(Map.Entry<String, UserInfoCDetails> entry : SelfUserInfo.checkedBooksInfo.entrySet()) {
                        Log.d("MessageENTRY",entry.getKey() + "; " + entry.getValue());
                    }
                    SelfUserInfo.checkedBooksInfo.put(isbn, new UserInfoCDetails(i, d));
                    //fix null
                }
                if(!dateRequested.trim().isEmpty()) {
                    Date dateRQ = null;
                    try {
                        dateRQ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateRequested.substring(0, 19));
                        //dateRQ = new SimpleDateFormat("yyyy-MM-dd").parse(dateRequested);
                    } catch(ParseException e) {
                        e.printStackTrace();
                    }
                    SelfUserInfo.reservedBookInfo.put(isbn, new UserInfoRDetails(status, dateRQ));
                }

                Book book = new Book();
                book.setTitle(title);
                book.setAuthor(author);
                book.setSubject(subject);
                book.setDesc(description);
                book.setISBN(isbn);
                book.setStatus(status);
                book.setDaysUntilAvailable(daysUntilAvailable);
                book.setPublishYear(year);
                book.setPublisher(publisher);

                books.add(book);
                nBooks++;
            }
            return books;
        } catch(NullPointerException e) {
            e.printStackTrace();
            return null;
        } catch(NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
   /**
    * Waits for the server to send a list of metadata for user's requested checkout or reserves. Used only by administrators.
    * @param timeout
    * @return A LinkedHashMap of books received, in order, from the server, including the user it belongs to, or null if an error occurred.
    */
    public static LinkedHashMap<UserInfo, Book> waitForRequests(int timeout) {
        byte[] requestAmount = waitForMessage(timeout);
        if(requestAmount == null) {
            return null;
        }
        int amountRequests = Integer.valueOf(new String(requestAmount));
        try {
            LinkedHashMap<UserInfo, Book> ret = new LinkedHashMap<>();
            int nRequests = 0;
            while(hasMessage(timeout) && nRequests < amountRequests) {
                String userUserName = new String(waitForMessage(timeout));
                String userFirstName = new String(waitForMessage(timeout));
                String userLastName = new String(waitForMessage(timeout));
                String dateRequested = new String(waitForMessage(timeout));
                String daysRequested = new String(waitForMessage(timeout));
                int iDaysRequested = -1;
                try {
                    iDaysRequested = Integer.valueOf(daysRequested);
                } catch(Exception e) {
                    e.printStackTrace();
                }


                Date asDate = null;
                try {
                    //asDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(dateRequested);
                    asDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateRequested);
                } catch(ParseException e) {
                    e.printStackTrace();
                }
                UserInfo inf = new UserInfo();
                inf.userName = userUserName;
                inf.firstName = userFirstName;
                inf.lastName = userLastName;
                inf.dateRequested = asDate;
                inf.daysRequested = iDaysRequested;

                String title = new String(waitForMessage(timeout));
                String author = new String(waitForMessage(timeout));
                String subject = new String(waitForMessage(timeout));
                String description = new String(waitForMessage(timeout));
                String isbn = new String(waitForMessage(timeout));
                String status = new String(waitForMessage(timeout));
                String daysUntilAvailable = new String(waitForMessage(timeout));
                waitForMessage(timeout);
                waitForMessage(timeout);
                String year = new String(waitForMessage(timeout));
                String publisher = new String(waitForMessage(timeout));

                Book book = new Book();
                book.setTitle(title);
                book.setAuthor(author);
                book.setSubject(subject);
                book.setDesc(description);
                book.setISBN(isbn);
                book.setStatus(status);
                book.setDaysUntilAvailable(daysUntilAvailable);
                book.setPublishYear(year);
                book.setPublisher(publisher);
                ret.put(inf, book);
                nRequests++;
            }
            return ret;
        } catch(NullPointerException e) {
            e.printStackTrace();
            return null;
        } catch(NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
   /**
    * Waits for the server to send a list of metadata for all books checked out. Used only by administrators.
    * @param timeout
    * @return A LinkedHashMap of books received, in order, from the server, including the user it belongs to, or null if an error occurred.
    */
    public static LinkedHashMap<UserInfo, Book> waitForAllCheckouts(int timeout) {
        byte[] checkoutAmount = waitForMessage(timeout);
        if(checkoutAmount == null) {
            return null;
        }
        int amountChecked = Integer.valueOf(new String(checkoutAmount));
        try {
            LinkedHashMap<UserInfo, Book> checkouts = new LinkedHashMap<>();
            int nChecked = 0;
            while(hasMessage(timeout) && nChecked < amountChecked) {
                String userUserName = new String(waitForMessage(timeout));
                String userFirstName = new String(waitForMessage(timeout));
                String userLastName = new String(waitForMessage(timeout));
                String dateChecked = new String(waitForMessage(timeout));
                String daysChecked = new String(waitForMessage(timeout));
                int iDaysChecked = -1;
                try {
                    iDaysChecked = Integer.valueOf(daysChecked);
                } catch(Exception e) {
                    e.printStackTrace();
                }


                Date asDate = null;
                try {
                    //asDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(dateChecked);
                    asDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateChecked);
                } catch(ParseException e) {
                    e.printStackTrace();
                }
                UserInfo inf = new UserInfo();
                inf.userName = userUserName;
                inf.firstName = userFirstName;
                inf.lastName = userLastName;
                inf.dateRequested = asDate;
                inf.daysRequested = iDaysChecked;

                String title = new String(waitForMessage(timeout));
                String author = new String(waitForMessage(timeout));
                String subject = new String(waitForMessage(timeout));
                String description = new String(waitForMessage(timeout));
                String isbn = new String(waitForMessage(timeout));
                String status = new String(waitForMessage(timeout));
                String daysUntilAvailable = new String(waitForMessage(timeout));
                waitForMessage(timeout);
                waitForMessage(timeout);
                String year = new String(waitForMessage(timeout));
                String publisher = new String(waitForMessage(timeout));

                Book book = new Book();
                book.setTitle(title);
                book.setAuthor(author);
                book.setSubject(subject);
                book.setDesc(description);
                book.setISBN(isbn);
                book.setStatus(status);
                book.setDaysUntilAvailable(daysUntilAvailable);
                book.setPublishYear(year);
                book.setPublisher(publisher);
                checkouts.put(inf, book);
                nChecked++;
            }
            return checkouts;
        } catch(NullPointerException e) {
            e.printStackTrace();
            return null;
        } catch(NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
   /**
    * Waits for the server to send a list of metadata for all users in the system. Used only by administrators.
    * @param timeout
    * @return An ArrayList of all UserInfos, or null if an error occurred.
    */
    public static ArrayList<UserInfo> waitForUsers(int timeout) {
        byte[] userAmount = waitForMessage(timeout);
        if(userAmount == null) {
            return null;
        }
        int amountUsers = Integer.valueOf(new String(userAmount));
        try {
            ArrayList<UserInfo> ret = new ArrayList<>();
            int nUsers = 0;
            while(hasMessage(timeout) && nUsers < amountUsers) {

                String userFirstName = new String(waitForMessage(timeout));
                String userLastName = new String(waitForMessage(timeout));
                String roleString = new String(waitForMessage(timeout));
                String userUserName = new String(waitForMessage(timeout));
                int iRole = -1;
                if(roleString.equals("Administrator")) {
                    iRole = 2;
                } else if(roleString.equals("Teacher")) {
                    iRole = 1;
                } else if(roleString.equals("Student")) {
                    iRole = 0;
                }
                UserInfo info = new UserInfo();
                info.userName = userUserName;
                info.firstName = userFirstName;
                info.lastName = userLastName;
                info.userType = iRole;
                ret.add(info);
                nUsers++;
            }
            Collections.sort(ret);

            return ret;
        } catch(NullPointerException e) {
            e.printStackTrace();
            return null;
        } catch(NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
   /**
    * Approves the checkout process for a user. Used only by administrators.
    * @param user, isbn
    * @return A boolean of whether or not the approval was successful.
    */
    public static boolean approveCheckout(String user, String isbn) {
        byte[] message = buildMessage("approve2", user, isbn);
        sendMessage(message);
        byte[] resp = waitForMessage(3000);
        if(resp == null) {
            return false;
        }
        String reply = new String(resp);
        if(reply.equals("accepted")) {
            return true;
        }
        Log.d("Message", "Approve checkout denied: " + reply);
        return false;
    }
   /**
    * Approves the reserve process for a user. Used only by administrators.
    * @param user, isbn
    * @return A boolean of whether or not the approval was successful.
    */
    public static boolean approveReserve(String user, String isbn) {
        byte[] message = buildMessage("approve1", user, isbn);
        sendMessage(message);
        byte[] resp = waitForMessage(3000);
        if(resp == null) {
            return false;
        }
        String reply = new String(resp);
        if(reply.equals("accepted")) {
            return true;
        }
        Log.d("Message", "Approve reserve denied: " + reply);
        return false;
    }
   /**
    * Declines the reserve or checkout process for a user. Used only by administrators.
    * @param user, isbn
    * @return A boolean of whether or not the declining was successful.
    */
    public static boolean declineReserveOrCheckout(String user, String isbn) {
        byte[] message = buildMessage("decline", user, isbn);
        sendMessage(message);
        byte[] resp = waitForMessage(3000);
        if(resp == null) {
            return false;
        }
        String reply = new String(resp);
        if(reply.equals("accepted")) {
            return true;
        }
        Log.d("Message", "Decline reserve/checkout denied: " + reply);
        return false;
    }
   /**
    * Searches the book database for appropriate books.
    * @param index, term
    * @return An ArrayList of Book objects returned by the search.
    */
    public static ArrayList<Book> doSearch(int index, String term) {
        byte[] message = buildMessage("search", String.valueOf(index), term);
        if(!sendMessage(message)) {
            return null;
        }
        return waitForBooks(5000);
    }





    /*
	The next section contains a variety of similarly functioning methods. The process given by the name of the method is attempted to
	occur. If the operation succeeds, the method will return null. If the operation fails, a String object will be returned, describing
	the reason for failure with varying degrees of descriptiveness. In most cases, the response is a very generic answer that the app
	must process, such as "unavailable" or "null response". 

	*/
    public static String reserveBook(Book book, String nDays) {
        byte[] message = buildMessage("reserve", book.getISBN(), nDays);
        sendMessage(message);
        byte[] resp = waitForMessage(5000);
        if(resp == null) {
            return "null response";
        }
        String reply = new String(resp);
        if(reply.equalsIgnoreCase("accepted")) {
            return null;
        } else if(reply.equalsIgnoreCase("unavailable")) {
            // Book is not available
            return "unavailable";
        } else if(reply.equalsIgnoreCase("alreadyholding")) {
            // User already has the book reserved or checked out
            return "alreadyholding";
        } else {
            // this shouldn't happen
            return "what";
        }
    }
    public static String checkoutBook(Book book, String nDays) {
        byte[] message = buildMessage("checkout", book.getISBN(), nDays);
        sendMessage(message);
        byte[] resp = waitForMessage(5000);
        if(resp == null) {
            return "null response";
        }
        String reply = new String(resp);
        if(reply.equalsIgnoreCase("accepted")) {
            return null;
        } else if(reply.equalsIgnoreCase("unavailable")) {
            // Book is not available
            return "unavailable";
        } else if(reply.equalsIgnoreCase("alreadyholding")) {
            // User already has the book reserved or checked out
            return "alreadyholding";
        } else {
            // this shouldn't happen
            return "what";
        }
    }
    public static String unreserveBook(Book book) {
        byte[] message = buildMessage("unreserve", book.getISBN());
        sendMessage(message);
        byte[] resp = waitForMessage(5000);
        if(resp == null) {
            return "null response";
        }
        String reply = new String(resp);
        if(reply.equalsIgnoreCase("accepted")) {
            return null;
        }
        return reply;
    }
    public static String createUser(String firstname, String lastname, String occupation, String username, String password) {
        byte[] message = buildMessage("createUser", firstname, lastname, occupation, username, password);
        if(!Networking.sendMessage(message)) {
            return LoginActivity.getInstance().getString(R.string.network_unavailable);
        }
        byte[] reply;
        try {
            reply = Networking.waitForMessage(2000);
        } catch (Exception e) {
            return LoginActivity.getInstance().getString(R.string.network_unavailable);
        }
        String resp = new String(reply);
        if(resp.equalsIgnoreCase("accepted")) {
            return null;
        } else {
            return LoginActivity.getInstance().getString(R.string.create_user_fail);
        }
    }

    public static String returnBook(Book book, UserInfo u) {
        byte[] message = buildMessage("return", book.getISBN(), u.userName);
        sendMessage(message);
        byte[] resp = waitForMessage(5000);
        if(resp == null) {
            return "null response";
        }
        String reply = new String(resp);
        if(reply.equalsIgnoreCase("accepted")) {
            return null;
        }
        return reply;
    }

    public static String deleteUser(String username) {
        byte[] message = buildMessage("deleteUser", username);
        if(!Networking.sendMessage(message)) {
            return LoginActivity.getInstance().getString(R.string.network_unavailable);
        }
        byte[] reply;
        try {
            reply = Networking.waitForMessage(2000);
        } catch (Exception e) {
            return LoginActivity.getInstance().getString(R.string.network_unavailable);
        }
        String resp = new String(reply);
        if(resp.equalsIgnoreCase("accepted")) {
            return null;
        } else {
            return LoginActivity.getInstance().getString(R.string.delete_user_fail);
        }
    }

   /**
    * Sends a raw message to a server. Do not use unless you understand sockets.
    * @param info
    * @return A boolean giving whether or not the packet was sent.
    */
    public static boolean sendMessage(byte[] info) {
        if(!Networking.isConnected()) {
            Networking.connect(2000);
        }
        try {
            netThread.getSocket().getOutputStream().write(info);
            netThread.getSocket().getOutputStream().flush();
            return true;
        } catch(Exception e) {
            disconnect();
            return false;
        }
    }
   /**
    * Registers this device's GCM API key to use for push notifications.
    * @param key
    */
    public static void registerPushKey(String key) {
        byte[] message = buildMessage("pushkey", key);
        sendMessage(message);
    }
    public static boolean isConnected() {
        try {
            netThread.getSocket().getOutputStream();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static boolean hasMessage() {
        synchronized(MESSAGE_LOCK) {
            return messages.size() != 0;
        }
    }
    public static boolean hasMessage(long timeout) {
        long timeSt = System.currentTimeMillis();
        while(System.currentTimeMillis() - timeSt <= timeout) {
            if(hasMessage()) return true;
        }
        return false;
    }
   /**
    * Waits for a message to be added via NetThread, timing out after the given amount of milliseconds.
    * @param timeoutMillis
    */
    public static byte[] waitForMessage(long timeoutMillis) {
        long timeInitial = System.currentTimeMillis();
        long timeNow;
        while(!hasMessage()) {
            timeNow = System.currentTimeMillis();
            if(timeNow - timeInitial >= timeoutMillis) {
                throw new NetworkTimedOutException("Network timed out given " + timeoutMillis + "ms");
            }
        }
        byte[] msg = getMessage();
        if(msg == null) {
            msg = new byte[0];
            Log.d("Message", "Null message caught");
        }
        return msg;
    }
    private static byte[] getMessage() {
        synchronized(MESSAGE_LOCK) {
            return messages.remove(0);
        }
    }
    public static void addMessage(byte[] b) {
        synchronized (MESSAGE_LOCK) {
            messages.add(b);
        }
    }

    public static boolean checkPassword(String str) {
        return str.equals(password);
    }

    public static void setPassword(String passworde) {
        if(password == null) {
            password = passworde;
        }
    }

    public static void setGoogleClient(GoogleSignInClient mGoogleSignInClien){
        if(mGoogleSignInClient==null){
            mGoogleSignInClient = mGoogleSignInClien;
        }
    }

    public static boolean sendBugReport(String report) {
        byte[] message = buildMessage("bug", report);
        return sendMessage(message);
    }


    public static class NetworkTimedOutException extends RuntimeException {
        public NetworkTimedOutException(String message) {
            super(message);
        }
        public NetworkTimedOutException(String message, Exception cause) {
            super(message, cause);
        }
    }
}

