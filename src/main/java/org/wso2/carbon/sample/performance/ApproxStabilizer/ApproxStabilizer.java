package org.wso2.carbon.sample.performance.ApproxStabilizer;

/**
 * Created by chamod on 10/26/17.
 */
public class ApproxStabilizer {
    private static final double FIXED_FILTER_FACTOR = 0.9;
    private static final double IMPACT_THRESHOLD = 0.01;
    private static final int WINDOW_SIZE = 1000;

    private static double filterRate;
    private static int oldMaxVal = Integer.MIN_VALUE;
    private static double filterFactor = FIXED_FILTER_FACTOR;
    private static long prevTimestamp = -1;
    private static long thresholdInterval = 0L;
    private static BloomFilter<Object> bloomFilter = BloomFilter.createBloomFilter(100000,
            0.1);
    private static double prevAnswer;
    private static long noOfEvents = 0;
    private volatile static LinearRegression linearRegression = new LinearRegression();


//////////////////////////////////////////////////////////////////////////////////////////////

    public static double approxMax(int newVal, double queueFilledPercentage, boolean reset) {
        if (Math.abs(queueFilledPercentage - 1) < 0.001) {
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

    public static double approxFairSample(double queueFilledPercentage, boolean droppedBatch) {
        if (Math.abs(queueFilledPercentage - 1) < 0.001) {
            filterRate = 0;
        } else {
            if (droppedBatch) {
                filterFactor -= 0.1;
            } else {
                filterFactor = FIXED_FILTER_FACTOR;
            }
            filterRate = calculateFilterRate(filterFactor, queueFilledPercentage);
//                            filterRate = Math.floor((1 - queueFilledPercentage) * 1000) / 1000.0;
//                            filterRate = Math.floor((1.5 - (2 * (Math.ceil(queueFilledPercentage * 10) / 10.0))) * 1000) / 1000.0;
//                              filterRate = 0.4;
        }
        return filterRate;
    }

    public static double approxOutOfOrder(double queueFilledPercentage, long eventTimestamp) {
        if (Math.abs(queueFilledPercentage - 1) < 0.001) {
            filterRate = 0;
        } else {
            if (prevTimestamp - thresholdInterval <= eventTimestamp) {
//                filterFactor = FIXED_FILTER_FACTOR;
//                filterRate = calculateFilterRate(filterFactor, queueFilledPercentage);
                filterRate = 1;
            } else {
                filterRate = 0;
            }
        }
        prevTimestamp = eventTimestamp;
        return filterRate;
    }

    public static double approxDistinctEvents(double queueFilledPercentage, Object attributeValue) {
        if (Math.abs(queueFilledPercentage - 1) < 0.001) {
            filterRate = 0;
        } else {
            if (bloomFilter.mayContain(attributeValue)) {
                filterRate = 0;
            } else {
                bloomFilter.insert(attributeValue);
                filterRate = 1;
            }
        }
        return filterRate;
    }

    public static double approxImpactModel(double queueFilledPercentage, double attributeValue) {
        noOfEvents++;
        if (Math.abs(queueFilledPercentage - 1) < 0.001) {
            filterRate = 0;
        } else {
//            System.out.println("forecast : " + (linearRegression.getForecastY(attributeValue,
//                    (1.0 / (noOfEvents % WINDOW_SIZE)))));//todo
            if (linearRegression.getForecastY(attributeValue, (1.0 / (noOfEvents % WINDOW_SIZE))) > IMPACT_THRESHOLD) {
                filterRate = 1;
            } else {
                filterRate = 0;
            }
//            System.out.println("Forecast : " + linearRegression.getForecastY(attributeValue, noOfEvents));
        }
        return filterRate;
    }




    //////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized static void expireTimeBatch() {
        bloomFilter.clear();
    }

    private static double calculateFilterRate(double filterFactor, double queueFilledPercentage) {
        return Math.floor(1000 * filterFactor * (1 - queueFilledPercentage)) / 1000.0;
    }

    public static long getThresholdInterval() {
        return thresholdInterval;
    }

    public synchronized static void setThresholdInterval(long thresholdInterval) {
        ApproxStabilizer.thresholdInterval = thresholdInterval;
        System.out.println("new out-of-order thresholdInterval : " + ApproxStabilizer.thresholdInterval);//todo
    }

    public synchronized static void calculateImpact(double eventVal, double answer) {
        if (noOfEvents != 0) {
            double eventImpact = 100 * Math.abs(answer - prevAnswer) / (prevAnswer);
//            System.out.println("Event : Count = " + noOfEvents + " : Value = " + eventVal + " : Impact = " + eventImpact);
            linearRegression.addPoint(eventVal, (1.0 / (noOfEvents % WINDOW_SIZE)), eventImpact);
        }
        noOfEvents++;
        prevAnswer = answer;
    }

}
