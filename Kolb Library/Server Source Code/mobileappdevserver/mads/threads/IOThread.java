/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileappdevserver.mads.threads;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mobileappdevserver.mads.Book;
import mobileappdevserver.mads.Server;

/**
 *
 * @author seilecd
 */
public class IOThread implements Runnable {
    private final InputStream in;
    private final OutputStream out;
    private ByteArrayOutputStream outStack;
    private final Object outStackLock;
    private final long socketID;
    private boolean verified = false;
    private String user;
    private String occupation;
    private String registrationToken = null;
    public IOThread(Socket socket, long socketID) throws IOException {
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        this.socketID = socketID;
        outStack = new ByteArrayOutputStream();
        outStackLock = new Object();
    }
    public void setRegistrationToken(String token) {
        registrationToken = token;
    }
    public String getRegistrationToken() {
        return registrationToken;
    }
    public boolean isVerified() {
        return verified;
    }
    public void verify(String user, String occupation) {
        verified = true;
        this.user = user;
        this.occupation = occupation;
    }
    public String getUsername() {
        return user;
    }
    public String getOccupation() {
        return occupation;
    }
    @Override public void run() {
        byte[] buf = new byte[1024];
        while(Server.getInstance().isRunning()) {
            try {
                int i = in.read(buf);
                if(i == -1 || i == 0) {
                    throw new RuntimeException("ayy nah b");
                }
                byte[] copy = Arrays.copyOf(buf, i);
                Server.getInstance().processInput(copy, socketID, this);
                
            } catch(Exception e) {
                e.printStackTrace();
                break;
            }
        }
        Server.getInstance().disconnect(this);
    }
    public void sendBooks(List<Book> books) {
        addMessage(String.valueOf(books.size()));
        for(Book b : books) {
            addBookToStack(b);
        }
        flush();
    }
    public void sendUsers(List<List<String>> users) {
        addMessage(String.valueOf(users.size()));
        for(List<String> user : users) {
            addMessage(user.get(0));
            addMessage(user.get(1));
            addMessage(user.get(2));
            addMessage(user.get(3));
        }
        flush();
    }
    public void sendRequests(HashMap<List<String>, Book> requests) {
        int size = requests.size();
        addMessage(String.valueOf(size));
        for(Map.Entry<List<String>, Book> entry : requests.entrySet()) {
            List<String> userInfo = entry.getKey();
            for(int i = 0; i < userInfo.size(); i++) {
                addMessage(userInfo.get(i));
            }
            addBookToStack(entry.getValue());
        }
        flush();
    }
    public void addBookToStack(Book b) {
        addMessage(b.getTitle());
        addMessage(b.getAuthor());
        addMessage(b.getSubject());
        addMessage(b.getDesc1());
        addMessage(b.getISBN());
        addMessage(b.getStatus());
        addMessage(b.getDaysUntilAvailable());
        addMessage(b.getDaysUntilMustReturn());
        addMessage(b.getDateLastAccountedFor());
    }
    public void addMessage(String line) {
        addMessage(line.getBytes());
    }
    public void addMessage(byte[] bytes) {
        addToStack(bytes);
        addToStack(new byte[] { 0x00 });
    }
    private void addToStack(byte[] message) {
        synchronized(outStackLock) {
            try {
                outStack.write(message);
            } catch(IOException e) { e.printStackTrace(); }
        }
    }
    public void flush() {
        synchronized(outStackLock) {
            try {
                byte[] output = outStack.toByteArray();
                out.write(output);
                out.flush();
                System.out.println("wrote " + new String(output) + " to " + this);
                outStack.close();
                outStack = new ByteArrayOutputStream();
            } catch(IOException e) {
                e.printStackTrace();
                Server.getInstance().disconnect(this);
            }
        }
    }
    public String toString() {
        return "IOThread" + socketID;
    }
}
