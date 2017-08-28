package org.wso2.carbon.sample.performance.feedbackServer;

import java.io.*;
import java.net.*;

public class TCPServer extends Thread{
    private Socket connectionSocket;
    private int port;

    public TCPServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket welcomeSocket = new ServerSocket(TCPServer.this.port);
            while (true) {
                System.out.println("FEEDBACK SERVER started................");
                connectionSocket = welcomeSocket.accept();
                new TCPSessionWriter(connectionSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
