package org.wso2.carbon.sample.performance.ApproxStabilizer;

/**
 * Created by chamod on 11/20/17.
 */
public class ApproxFeedbackStabilizer {
    private static final double EXPECTED_ERROR = 0.1;
    private static final boolean CAL_AVG = false;
    private static final boolean CAL_MAX = true;
    private static final boolean CAL_MIN = false;

    private static int noEvenSentAfter = 0;
    private static int eventCount = 0;

    private static boolean FIRST_EVENT_ARRIVED = false;
    private static double filterRate;
    private volatile static double exact_event_count = 0;
    private volatile static double exact_avg = 0;
    private volatile static double exact_max = 0;
    private volatile static double exact_min = 0;

    public static double approxFeedbackDrop(double queueFillage, double attributeValue) {
        eventCount++;
        exact_event_count++;
        if (Math.abs(queueFillage - 1) < 0.001) {
            filterRate = 0;
        } else if ((eventCount - noEvenSentAfter) > 100) {
            noEvenSentAfter = eventCount;
            filterRate = 1;
        } else {
            if (eventCount == 1) {
                filterRate = 1;
            } else {
                filterRate = 0;
//                System.out.println("i : " + getImpactOnAverage(attributeValue));//todo
                if (CAL_AVG && getImpactOnAverage(attributeValue) > EXPECTED_ERROR) {
                    updateExactValues(attributeValue);
                    return 1;
                }
                if (CAL_MAX && getImpactOnMax(attributeValue) > EXPECTED_ERROR) {
                    updateExactValues(attributeValue);
                    return 1;
                }
                if (CAL_MIN && getImpactOnMin(attributeValue) > EXPECTED_ERROR) {
                    updateExactValues(attributeValue);
                    return 1;
                }
            }
        }
        updateExactValues(attributeValue);
        return filterRate;
    }
///////////////////////////////////////////////////////////////////////////////////////////////////

    private static double getImpactOnAverage(double attributeValue) {
        double new_avg = (exact_avg * (exact_event_count - 1) + attributeValue)
                / exact_event_count;
//        System.out.println("Value : " + attributeValue + " :Impact : " +
//                Math.abs((exact_avg - new_avg) / exact_avg)); //todo
        return Math.abs((exact_avg - new_avg) / new_avg);
    }

    private static double getImpactOnMax(double attributeValue) {
        if (attributeValue > exact_max) {
            return Math.abs((attributeValue - exact_max) / attributeValue);
        }
        return 0;
    }

    private static double getImpactOnMin(double attributeValue) {
        if (attributeValue < exact_min) {
            return Math.abs((exact_min - attributeValue) / attributeValue);
        }
        return 0;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public synchronized static void updateCurrentEventCount(int event_count) {
        if (!FIRST_EVENT_ARRIVED) {
            FIRST_EVENT_ARRIVED = true;
        }
        exact_event_count = event_count;
    }

    public synchronized static void updateCurrentAvg(double avg) {
        exact_avg = avg;
    }

    public synchronized static void updateExactValues(double attributeValue) {
        exact_avg = (exact_avg * (exact_event_count - 1) + attributeValue) / exact_event_count;
        if (exact_max < attributeValue) {
            exact_max = attributeValue;
        }
        if (exact_min > attributeValue) {
            exact_min = attributeValue;
        }
    }

    public synchronized static void updateCurrentMax(double max) {
        exact_max = max;
    }

    public synchronized static void updateCurrentMin(double min) {
        exact_min = min;
    }
}
