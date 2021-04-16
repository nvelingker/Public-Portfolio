/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileappdevserver.mads.threads;

import java.net.ServerSocket;
import java.net.Socket;
import mobileappdevserver.mads.Server;

/**
 *
 * @author seilecd
 */
public class ServerThread implements Runnable {
    private final ServerSocket server;
    public ServerThread(ServerSocket server) {
        this.server = server;
    }
    public void run() {
        while(Server.getInstance().isRunning()) {
            try {
                Socket sock = server.accept();
                IOThread io = new IOThread(sock, Server.getInstance().obtainSocketID());
                Server.getInstance().newConnection(io);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
