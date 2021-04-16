/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileappdevserver.mads;

/**
 *
 * @author seilecd
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
        
        try {
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
