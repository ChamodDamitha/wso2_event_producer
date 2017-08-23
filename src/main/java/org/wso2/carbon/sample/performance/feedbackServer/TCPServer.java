package org.wso2.carbon.sample.performance.feedbackServer;

import java.io.*;
import java.net.*;

public class TCPServer {

    private int port;

    public TCPServer(int port) {
        this.port = port;
    }


    public void startServer() {
        try {
            ServerSocket welcomeSocket = new ServerSocket(this.port);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Socket connectionSocket = null;
                            connectionSocket = welcomeSocket.accept();
                            BufferedReader inFromClient =
                                    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                            String clientSentence = inFromClient.readLine();
                            System.out.println("Received: " + clientSentence);
                            String capitalizedSentence = clientSentence.toUpperCase() + '\n';
                            outToClient.writeBytes(capitalizedSentence);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
