package org.wso2.carbon.sample.performance.ApproxStabilizer;

import java.util.Queue;

/**
 * Created by chamod on 8/28/17.
 */
public class FeedbackProcessor {
    private static FeedbackProcessor feedbackProcessor = null;

    private int totalEventCount = 0;
    private int receivedEventCount = 0;

    public void incrementTotalEvents() {
        totalEventCount++;
    }

    private FeedbackProcessor() {
    }

    public synchronized static FeedbackProcessor getInstance() {
        if (feedbackProcessor == null) {
            feedbackProcessor = new FeedbackProcessor();
        }
        return feedbackProcessor;
    }


    public void handleFeedback(String msg) {
        if (isAccuracyFeedback(msg)) {
            System.out.println("FeedbackProcessor : handling accuracy feedback");
            String[] str = msg.split(":");
            String eventStreamName = str[2].split(",")[0];
            double accuracy = Double.valueOf(str[3]);

            System.out.println("STREAM NAME : " + eventStreamName);
            System.out.println("ACCURACY : " + accuracy);
        } else if (isReceivedFeedback(msg)){
            receivedEventCount += Integer.valueOf(msg.split(":")[1]);

            System.out.println("EVENT RECEIVING % = " + (100.0 * receivedEventCount / totalEventCount));
        }
    }


    private boolean isAccuracyFeedback(String msg) {
        if (msg.subSequence(0, 17).equals("ACCURACY_FEEDBACK")) {
            return true;
        }
        return false;
    }

    private boolean isReceivedFeedback(String msg) {
        if (msg.subSequence(0, 17).equals("RECEIVED_FEEDBACK")) {
            return true;
        }
        return false;
    }
}
