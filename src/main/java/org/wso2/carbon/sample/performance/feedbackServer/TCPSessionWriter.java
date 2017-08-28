package org.wso2.carbon.sample.performance.feedbackServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by chamod on 8/28/17.
 */
public class TCPSessionWriter extends Thread {

    private Socket connectionSocket;

    public TCPSessionWriter(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run() {
        BufferedReader inFromClient = null;
        try {
            inFromClient = new BufferedReader(new InputStreamReader(TCPSessionWriter.this.connectionSocket.getInputStream()));

//          DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            String clientSentence = null;

            clientSentence = inFromClient.readLine();
            System.out.println("Received: " + clientSentence);


//          String capitalizedSentence = clientSentence.toUpperCase() + '\n';
//          outToClient.writeBytes(capitalizedSentence);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
