/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileappdevserver.mads;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import mobileappdevserver.mads.socket.PushNotification;
import mobileappdevserver.mads.threads.IOThread;
import mobileappdevserver.mads.threads.ServerThread;

/**
 *
 * @author seilecd
 */
public class Server {
    private static Server instance;
    private boolean running;
    private long socketIDCounter = 0;
    private Thread serverThread;
    private ServerSocket serverSocket;
    //public static final String IP = "localhost";
    public static final int port = 55151;
    private Database db;
    public static final char NET_SEP = (char) 0x00;
    public static Server getInstance() {
        return instance;
    }
    public Database getDatabase() {
        return db;
    }
    public Server() {
        running = false;
        instance = this;
    }
    public void start() {
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            serverThread = new Thread(new ServerThread(serverSocket));
            serverThread.start();
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        db = new Database("maddatab", "maddatab");
    }
    
    public void kill() {
        running = false;
    }
    public boolean isRunning() {
        return running;
    }
    /**
     * This method is called whenever a user sends a packet, maximum 1KB, to the server.
     * The method calls the appropriate responses to library catalog searches, book reserve requests, etc.
     * @param in
     * @param socketID
     * @param client 
     */
    public void processInput(byte[] in, long socketID, IOThread client) {
        System.out.println(client.toString() + ": " + new String(in));
        System.out.println("("+in.length+")");
        
        // Checks for if the user sending input has a push notification API token to access
        if(client.getRegistrationToken() == null) {
            String input = new String(in);
            try {
                String[] spl = input.split(NET_SEP+"");
                
                // If the user 
                if(spl[0].equalsIgnoreCase("pushkey")) {
                    String token = spl[1];
                    client.setRegistrationToken(token);
                    if(client.isVerified()) {
                        db.updatePushkey(token, client.getUsername());
                    }
                }
            } catch(Exception e) { e.printStackTrace(); }
        }
        if(!client.isVerified()) {
            String input = new String(in);
            try {
                String[] spl = input.split(NET_SEP+"");
                String q = spl[0];
                if(q.equalsIgnoreCase("login")) {
                    String user = spl[1];
                    String pass = spl[2];
                    String[] name = db.authenticate(user, pass);
                    if(name != null) {
                        client.verify(user, name[2]);
                        client.addMessage("accepted");
                        client.addMessage(name[0]);
                        client.addMessage(name[1]);
                        client.addMessage(name[2]);
                        client.flush();
                        ArrayList<Book> reserved = db.getReservedBooks(user);
                        ArrayList<Book> checkedout = db.getCheckedOutBooks(user);
                        client.sendBooks(reserved);
                        client.sendBooks(checkedout);
                        if(client.getRegistrationToken() != null) {
                            db.updatePushkey(client.getRegistrationToken(), user);
                        }
                        if(name[2].equals("Administrator")) {
                            client.sendRequests(db.getRequestedReserves());
                            client.sendRequests(db.getRequestedCheckouts());
                            client.sendRequests(db.getCurrentCheckouts());
                            client.sendUsers(db.getAllUsers());
                        }
                    } else {
                        client.addMessage("denied");
                        client.flush();
                    }
                    
                } else if(q.equalsIgnoreCase("createUser")) {
                    String fname = spl[1];
                    String lname = spl[2];
                    String occ = spl[3];
                    String user = spl[4];
                    String pass = spl[5];
                    if(occ.equalsIgnoreCase("Student")) {
                        if(db.addUser(fname, lname, occ, user, pass)) {
                            client.verify(user, occ);
                            client.addMessage("accepted");
                        } else {
                            client.addMessage("denied");
                        }
                    } else {
                        String key = spl[6];
                        if(key.equals("Elevate your future!")) {
                            if(db.addUser(fname, lname, occ, user, pass)) {
                                client.verify(user, occ);
                                client.addMessage("accepted");
                            } else {
                                client.addMessage("denied");
                            }
                        } else {
                            client.addMessage("wrongkey");
                        }
                    }
                   client.flush();
                }
                
            } catch(Exception e) { e.printStackTrace(); }
        } else {
            String input = new String(in);
            try {
                String[] spl = input.split(NET_SEP+"");
                if(spl[0].equalsIgnoreCase("search")) {
                    int n = Integer.valueOf(spl[1]);
                    String text = spl[2];
                    ArrayList<Book> output = new ArrayList<Book>();
                    if(n == 0) {
                        output.addAll(db.searchBooksByKeyword(text));
                    } else if(n == 1) {
                        output.addAll(db.searchBooksByTitle(text));
                    } else if(n == 2) {
                        output.addAll(db.searchBooksByAuthor(text));
                    } else if(n == 3) {
                        output.addAll(db.searchBooksBySubject(text));
                    } else {
                        // series?
                    }
                    client.sendBooks(output);
                } else if(spl[0].equalsIgnoreCase("reserve")) {
                    String isbn = spl[1];
                    String days = spl[2];
                    String response = db.reserveRequest(client.getUsername(), isbn, days);
                    if(response == null) {
                        client.addMessage("accepted");
                    } else {
                        client.addMessage(response);
                    }
                    client.flush();
                } else if(spl[0].equalsIgnoreCase("checkout")) {
                    String isbn = spl[1];
                    String days = spl[2];
                    String response = db.checkoutRequest(client.getUsername(), isbn, days);
                    if(response == null) {
                        client.addMessage("accepted");
                    } else {
                        client.addMessage(response);
                    }
                    client.flush();
                } else if(spl[0].equalsIgnoreCase("unreserve")) {
                    String isbn = spl[1];
                    String response = db.unreserve(client.getUsername(), isbn);
                    if(response == null) {
                        client.addMessage("accepted");
                    } else {
                        client.addMessage(response);
                    }
                    client.flush();
                }  else if(spl[0].equalsIgnoreCase("approve2")) {
                    String usercheckout = spl[1];
                    String isbn = spl[2];
                    if(client.getOccupation().equalsIgnoreCase("Administrator")) {
                         String response = db.approveCheckout(usercheckout, isbn);
                         if(response == null) {
                             client.addMessage("accepted");
                             // send push notification to username who got the book
                             PushNotification notif = PushNotification.generateCheckoutApproved(db.getTitle(isbn));
                             notif.sendTo(usercheckout);
                         } else {
                             client.addMessage(response);
                         }
                    }
                    client.flush();
                } else if(spl[0].equalsIgnoreCase("approve1")) {
                    String userreserve = spl[1];
                    String isbn = spl[2];
                    if(client.getOccupation().equalsIgnoreCase("Administrator")) {
                         String response = db.approveReserve(userreserve, isbn);
                         if(response == null) {
                             client.addMessage("accepted");
                             // send push notification to username who got the book
                             PushNotification notif = PushNotification.generateReserveApproved(isbn);
                             notif.sendTo(userreserve);
                         } else {
                             client.addMessage(response);
                         }
                    }
                    client.flush();
                } else if(spl[0].equalsIgnoreCase("decline")) {
                    String userdecline = spl[1];
                    String isbn = spl[2];
                    if(client.getOccupation().equalsIgnoreCase("Administrator")) {
                        try {
                            db.denyRequestOrCheckout(userdecline, isbn);
                            client.addMessage("accepted");
                        } catch(Exception e) {
                            e.printStackTrace();
                            client.addMessage("error");
                        }
                    }
                    client.flush();
                } else if(spl[0].equalsIgnoreCase("comment")) {
                    String isbn = spl[2];
                    
                } else if(spl[0].equalsIgnoreCase("bug")) {
                    String message = spl[1];
                    db.doBugReport(client.getUsername(), message);
                } else if(spl[0].equalsIgnoreCase("createUser")) {
                    String fname = spl[1];
                    String lname = spl[2];
                    String occ = spl[3];
                    String user = spl[4];
                    String pass = spl[5];
                    if(client.getOccupation().equals("Administrator")) {
                        if(db.addUser(fname, lname, occ, user, pass)) {
                            client.addMessage("accepted");
                        } else {
                            client.addMessage("denied");
                        }
                    } else {
                        String key = spl[6];
                        if(key.equals("Elevate your future!")) {
                            if(db.addUser(fname, lname, occ, user, pass)) {
                                client.verify(user, occ);
                                client.addMessage("accepted");
                            } else {
                                client.addMessage("denied");
                            }
                        } else {
                            client.addMessage("wrongkey");
                        }
                    }
                   client.flush();
                }
            } catch(Exception e) { e.printStackTrace(); }
        }
    }
    private List<IOThread> cons = new ArrayList<IOThread>();
    public void newConnection(IOThread io) {
        new Thread(io).start();
        cons.add(io);
        System.out.println("connection");
    }
    public void disconnect(IOThread io) {
        cons.remove(io);
        System.out.println("disconnection");
    }
    public void broadcast(byte[] message) {
        for(IOThread th : cons) {
            th.addMessage(message);
            th.flush();
        }
    }
    public long obtainSocketID() {
        return socketIDCounter++;
    }
}
