package org.wso2.carbon.sample.performance.ApproxStabilizer;

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
            String clientSentence;
            String[] msgStr;
            while ((clientSentence = inFromClient.readLine()) != null) {
                msgStr = clientSentence.trim().split(":");
                if (msgStr[0].trim().equals("TIME_BATCH_EXPIRE_FEEDBACK")) {
                    ApproxStabilizer.expireTimeBatch();
                } else if (msgStr[0].trim().equals("SIDDHI_QUERY_FEEDBACK")) {
//                    ApproxStabilizer.calculateImpact(Double.valueOf(msgStr[1].split("=")[1].trim()),
//                            Double.valueOf(msgStr[2].split("=")[1].trim()));
                    for (int i = 1; i < msgStr.length; i += 2) {
                        if (msgStr[i].trim().equals("EVENT_COUNT")) {
                            ApproxFeedbackStabilizer.updateCurrentEventCount(Integer.valueOf(msgStr[i + 1].trim()));
                        } else if (msgStr[i].trim().equals("AVG")) {
                            ApproxFeedbackStabilizer.updateCurrentAvg(Double.valueOf(msgStr[i + 1].trim()));
                        } else if (msgStr[i].trim().equals("MAX")) {
                            ApproxFeedbackStabilizer.updateCurrentMax(Double.valueOf(msgStr[i + 1].trim()));
                        } else if (msgStr[i].trim().equals("MIN")) {
                            ApproxFeedbackStabilizer.updateCurrentMin(Double.valueOf(msgStr[i + 1].trim()));
                        }
                    }


                } else if (msgStr[0].substring(0, 17).equals("thresholdInterval")) {
                    long thresholdInterval = Long.valueOf(msgStr[1].trim());
                    ApproxStabilizer.setThresholdInterval(thresholdInterval);
                }
            }
//            System.out.println("Received: " + clientSentence);
//            FeedbackProcessor.getInstance().handleFeedback(clientSentence);

//          String capitalizedSentence = clientSentence.toUpperCase() + '\n';
//          outToClient.writeBytes(capitalizedSentence);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
