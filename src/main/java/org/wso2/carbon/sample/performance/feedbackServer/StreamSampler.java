package org.wso2.carbon.sample.performance.feedbackServer;/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import java.util.ArrayList;

/**
 * Used to sample a set of data depending on a specified accuracy
 *
 * @param <T>
 */
public class StreamSampler<T> {
    private int totalNoOfHashValues = 1000;
    private int acceptedNoOfHashValues;
    private double accuracy;

    private ArrayList<T> events;

    private int totalCount;
    private int[] counts;


    /**
     * Based on a specified accuracy and a precision of accuracy, an StreamSampler object is created
     *
     * @param accuracy            is a double value in the range [0,1]
     * @param precisionOfAccuracy is a integer value specifying the number of decimal
     *                            places of the accuracy(precision) to be considered
     */
    public StreamSampler(double accuracy, int precisionOfAccuracy) {
        int temp = 1;
        for (int i = 0; i < precisionOfAccuracy; i++) {
            temp *= 10;
        }
        this.totalNoOfHashValues = temp;
        init(accuracy);
    }

    /**
     * Based on a specified accuracy, an StreamSampler object is created.
     * The default precision of the accuracy is taken for 3 decimal places.
     *
     * @param accuracy is a double value in the range [0,1]
     */
    public StreamSampler(double accuracy) {
        init(accuracy);
    }

    private void init(double accuracy) {
        if (accuracy > 1 || accuracy < 0) {
            throw new IllegalArgumentException("accuracy must be in the range of (0,1)");
        }
        this.accuracy = accuracy;

        String accuracyStr = accuracy + "";
        System.out.println(accuracyStr);
        int decimalLength = (accuracyStr.split("\\.")[1]).length();

        int precisionOfAccuracy = (totalNoOfHashValues + "").length() - 1;
        if (decimalLength > precisionOfAccuracy) {
            throw new IllegalArgumentException("precision of accuracy must be at most for "
                    + precisionOfAccuracy + " decimal places");
        }
        acceptedNoOfHashValues = (int) Math.ceil(accuracy * totalNoOfHashValues);

        System.out.println("totalNoOfHashValues : " + totalNoOfHashValues);
        System.out.println("acceptedNoOfHashValues : " + acceptedNoOfHashValues);
        events = new ArrayList<T>();

        totalCount = 0;
        counts = new int[totalNoOfHashValues];
    }

    /**
     * Add the given object to a list if it is not dropped.
     *
     * @param t is the object
     * @return {@code true} if the object is added to the list, {@code false} if the object is dropped from the list.
     */
    public boolean add(T t) {
        if (isAddable(t)) {
            events.add(t);
            return true;
        }
        return false;
    }

    /**
     * Calculate whether a given {@code t} object can be passed through the sampling filter or not.
     *
     * @param t is the object
     * @return {@code true} if the object is included in the samples, {@code false} if the object is dropped from samples.
     */
    public boolean isAddable(T t) {
        int hash = getHashValue(t);
        counts[hash]++;
        totalCount++;

        if (hash <= acceptedNoOfHashValues) {
            return true;
        }
        return false;
    }

    private int getHashValue(T t) {
        int hash = Math.abs(MurmurHash.hash(t));
        return hash % totalNoOfHashValues;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public ArrayList<T> getEvents() {
        return events;
    }

    public int[] getCounts() {
        return counts;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
