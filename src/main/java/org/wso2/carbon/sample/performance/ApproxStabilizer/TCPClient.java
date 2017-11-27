package org.wso2.carbon.sample.performance.ApproxStabilizer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPClient extends Thread {
    private String host;
    private int port;
    private String msg;

    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void sendMsg(String msg) {
        this.msg = msg;
        this.start();
    }

    @Override
    public void run() {
//                System.out.println("MSG Sending triggered................");
        try {
//            String sentence;
//            String modifiedSentence;
//            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            Socket clientSocket = new Socket(TCPClient.this.host, TCPClient.this.port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
//            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                    sentence = msg;
//            sentence = inFromUser.readLine();
            outToServer.writeBytes(msg + "\n");
//            modifiedSentence = inFromServer.readLine();
//                        System.out.println("FROM SERVER: " + modifiedSentence);
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
