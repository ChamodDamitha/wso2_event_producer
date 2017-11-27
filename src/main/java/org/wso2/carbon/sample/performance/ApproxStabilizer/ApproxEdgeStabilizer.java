package org.wso2.carbon.sample.performance.ApproxStabilizer;

/**
 * Created by chamod on 11/22/17.
 */
public class ApproxEdgeStabilizer {

    private static final double EXPECTED_ERROR = 0.3;
    private static final boolean CAL_AVG = true;
    private static final boolean CAL_MAX = false;
    private static final boolean CAL_MIN = false;

    private static long startTime = 0;
    private static int eventCount = 0;
    private static int lastEventCount = 0;
    private static int expectedEventsInBatch = 100000;
    private static int eventCountInWindow = 0;
    private static double errorSum = 0;

    private static double actualSum = 0;

    private static void setParams(int timeBatchWindowLength, int inputTps) {
        expectedEventsInBatch = timeBatchWindowLength * inputTps;
    }

    private static void resetWindow() {
        System.out.println("ACTUAL SUM : " + actualSum + " : count : " + eventCountInWindow);//todo
        eventCountInWindow = 0;
        errorSum = 0;
        actualSum = 0;
    }

    public static Double approxDrop(double queueFillage, double attributeValue) {
        actualSum += attributeValue;

        Double modifiedAttributeValue = null;
        if (Math.abs(queueFillage - 1) < 0.001) {
            errorSum += attributeValue;
        } else {
            double deltaValue = 10 * errorSum / (expectedEventsInBatch - eventCountInWindow);
//            double deltaValue = errorSum;
            errorSum -= deltaValue;
            modifiedAttributeValue = attributeValue + deltaValue;
        }
        eventCountInWindow++;
//        if (eventCountInWindow >= expectedEventsInBatch) {
//            resetWindow();
//        }
        if (checkTimeExpired()) {
            resetWindow();
        }
        return modifiedAttributeValue;
    }

    private static boolean checkTimeExpired() {
        eventCount++;
        if (eventCount == 1) {
            startTime = System.currentTimeMillis();
        }
        if ((System.currentTimeMillis() - startTime) >= 1000) {
            System.out.println("Time Passage : 1 s : Events sent : " + (eventCount - lastEventCount));
            startTime = System.currentTimeMillis();
            lastEventCount = eventCount;
            return true;
        }
        return false;
    }
}
