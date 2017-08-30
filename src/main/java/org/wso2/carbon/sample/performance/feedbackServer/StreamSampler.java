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

public class StreamSampler<T> {
    private int totalNoOfHashValues = 1000;
    private int acceptedNoOfHashValues;
    private double accuracy;
    private ArrayList<T> events;

    public StreamSampler(double accuracy, int precisionOfAccuracy) {
        int temp = 1;
        for (int i = 0; i < precisionOfAccuracy; i++) {
            temp *= 10;
        }
        this.totalNoOfHashValues = temp;
        init(accuracy);
    }

    public StreamSampler(double accuracy) {
        init(accuracy);
    }

    private void init(double accuracy) {
        if (accuracy >= 1 || accuracy <= 0) {
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
    }

    public boolean add(T t) {
        if (isAddable(t)) {
            events.add(t);
            return true;
        }
        return false;
    }

    public boolean isAddable(T t) {
        if (getHashValue(t) <= acceptedNoOfHashValues) {
            return true;
        }
        return false;
    }

    private int getHashValue(T t) {
        int hash = MurmurHash.hash(t);
        return hash % totalNoOfHashValues;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public ArrayList<T> getEvents() {
        return events;
    }
}
