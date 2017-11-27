package org.wso2.carbon.sample.performance.ApproxStabilizer;

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
            System.out.println("FEEDBACK SERVER started................");
            ServerSocket welcomeSocket = new ServerSocket(TCPServer.this.port);
            while (true) {
                connectionSocket = welcomeSocket.accept();
                new TCPSessionWriter(connectionSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
