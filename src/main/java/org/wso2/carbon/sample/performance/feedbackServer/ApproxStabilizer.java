package org.wso2.carbon.sample.performance.feedbackServer;

/**
 * Created by chamod on 10/26/17.
 */
public class ApproxStabilizer {
    private static final double fixedFilterFactor = 0.3;
    private static double filterRate;
    private static int oldMaxVal = Integer.MIN_VALUE;
    private static double filterFactor = fixedFilterFactor;

    public static double approxMax(int newVal, double queueFillage, boolean reset) {
        if (Math.abs(queueFillage - 1) < 0.001) {
            filterRate = 0;
        } else if (oldMaxVal < newVal) {
            filterRate = 1;
            oldMaxVal = newVal;
        } else {
            filterRate = 0.05;
        }
        if (reset) {
            oldMaxVal = Integer.MIN_VALUE;
        }
        return filterRate;
    }

    public static double approxFairSample(double queueFillage, boolean droppedBatch) {
        if (Math.abs(queueFillage - 1) < 0.001) {
            filterRate = 0;
        } else {
            if (droppedBatch) {
                filterFactor -= 0.1;
            } else {
                filterFactor = fixedFilterFactor;
            }
            filterRate = Math.floor(1000 * filterFactor * (1 - queueFillage)) / 1000.0;
////////                            filterRate = Math.floor((1 - queueFillage) * 1000) / 1000.0;
////////                            filterRate = Math.floor((1.5 - (2 * (Math.ceil(queueFillage * 10) / 10.0))) * 1000) / 1000.0;
////////                              filterRate = 0.4;
        }
        return filterRate;
    }
}
