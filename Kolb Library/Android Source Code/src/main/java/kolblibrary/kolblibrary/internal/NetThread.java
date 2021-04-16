/*
* @authors Tejas Priyadarshi, Christopher Seiler, Neelay Velingker
*/
package kolblibrary.kolblibrary.internal;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


public class NetThread implements Runnable {
    private final String address;
    private final int port, timeout;
    private boolean connected = false;
    private Socket socket;
    public NetThread(String address, int port, int timeout) {
        this.address = address;
        this.port = port;
        this.timeout = timeout;
        this.socket = new Socket();
    }
    public Socket getSocket() {
        return socket;
    }
   /**
    * Connects to the server on a separate thread and constantly waits for messages, sending results to Networking's messages.
    */
    public void run() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), timeout);
            Networking.connectSuccess();
            ByteArrayOutputStream messageReader = new ByteArrayOutputStream();
            while(Networking.isConnected()) {
                try {
                    int i = socket.getInputStream().read();
                    if (i == -1) {
                        Networking.disconnect();
                        Log.d("Message", "NETWORK DISCONNECTED");
                        throw new Networking.NetworkTimedOutException("Disconnected for stack trace");
                    }
                    if(i == 0x00) { // split messages with blank (0x00) delimiter
                        byte[] mess = messageReader.toByteArray();
                        if(mess == null) {
                            mess = new byte[0];
                        }

			// To prevent concurrent modifications
                        synchronized(Networking.MESSAGE_LOCK) {
                            Log.d("Message", "Added message: " + new String(mess));
                            Networking.addMessage(mess);
                        }
                        messageReader.close();
                        messageReader = new ByteArrayOutputStream();
                    } else {
                        messageReader.write(i);
                    }
                } catch(IOException e) {
                    Log.d("Message", "Exception caught: " + e.getMessage());
                    e.printStackTrace();
                    Networking.disconnect();
                    break;
                }
            }
            Log.d("Message", "While loop broken");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
