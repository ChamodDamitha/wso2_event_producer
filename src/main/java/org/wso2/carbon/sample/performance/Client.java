/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sample.performance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.sample.performance.ApproxStabilizer.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

public class Client {
    static StreamSampler streamSampler = new StreamSampler(1);

    private static Log log = LogFactory.getLog(Client.class);
    private static String sCurrentLine = null;
    private static BufferedReader br = null;
    private static FileReader fr = null;

    public static void main(String[] args) {
//      Run the feedback sever
        TCPServer tcpServer = new TCPServer(6789);
        tcpServer.start();

//      Run event publish client

        log.info(Arrays.deepToString(args));
        try {
            log.info("Starting WSO2 Performance Test Client");

            AgentHolder.setConfigPath(DataPublisherUtil.getDataAgentConfigPath());
            DataPublisherUtil.setTrustStoreParams();

            String protocol = args[0];
            String host = args[1];
            String port = args[2];
            String username = args[3];
            String password = args[4];
            String eventCount = args[5];
            String elapsedCount = args[6];
            String warmUpCount = args[7];
            String calcType = args[8];

            //create data publisher
            DataPublisher dataPublisher = new DataPublisher(protocol, "tcp://" + host + ":" + port, null, username,
                    password);

            //Publish event for a valid stream
            if ("latency".equalsIgnoreCase(calcType)) {
                publishEventsForLatency(dataPublisher, Long.parseLong(eventCount), Long.parseLong(elapsedCount),
                        Long.parseLong(warmUpCount));
            } else {
                publishEvents(dataPublisher, Long.parseLong(eventCount), Long.parseLong(elapsedCount),
                        Long.parseLong(warmUpCount));
            }

//            dataPublisher.shutdownWithAgent();
        } catch (Throwable e) {
            log.error(e);
        }
//       initialize file read
        initFileRead(ClassLoader.getSystemResource("dataset2.csv").getPath());
    }

    private static void publishEvents(DataPublisher dataPublisher, long eventCount, long elapsedCount,
                                      long warmUpCount) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean droppedBatch = false;
                int droppedInWindow = 0;
                long totalDropped = 0;
                long controlledDropped = 0;
                long uncontrolledDropped = 0;
                double queueFilledPercentage;

                final long outOfOrderThresholdInterval = 0L;

                int counter = 0;
                Random randomGenerator = new Random(1233435);
                String streamId = "org.wso2.event.sensor.stream:1.0.0";
                long lastTime = System.currentTimeMillis();
                DecimalFormat decimalFormat = new DecimalFormat("#");


                CounterGenerator counterGenerator = new CounterGenerator(100000, 10);
                long startTime = 0;
                int newCounter = 0;
                while (counter < eventCount) {

//                  file read event
//                    Object[] eventObj = getNextEventFromFile();
//                    if (eventObj == null) {
//                        break;
//                    }

                    boolean isPowerSaveEnabled = randomGenerator.nextBoolean();
                    int sensorId = counter;
//                    int sensorId = (int) eventObj[0];
                    double longitude = randomGenerator.nextDouble();
                    double latitude = randomGenerator.nextDouble();
                    int humidity = randomGenerator.nextInt(1000000);
//                    int humidity = counterGenerator.getNextCounter();
//                    int humidity = counter % 10;
                    double sensorValue = randomGenerator.nextDouble();
                    long eventTimestamp = System.currentTimeMillis();
//                    long eventTimestamp = (long) eventObj[1];


                    Event event = new Event(streamId, System.currentTimeMillis(),
                            new Object[]{eventTimestamp, isPowerSaveEnabled, sensorId,
                                    "temperature-" + counter},
                            new Object[]{longitude, latitude},
                            new Object[]{humidity, sensorValue});

                    if (counter == 0) {
                        startTime = System.currentTimeMillis();
                        newCounter = 0;
                    }

//                  Stream sampling
                    queueFilledPercentage = dataPublisher.getQueueFilledPercentage();
//                  max operation
//                    boolean resetMax = false;
//                    if ((counter + 1) % 1000 == 0) {
//                        resetMax = true;
//                    }
//                    streamSampler.setFilterRate(ApproxStabilizer.approxMax(humidity, queueFilledPercentage, resetMax));

//                  fair sampling
                    streamSampler.setFilterRate(ApproxStabilizer.approxFairSample(queueFilledPercentage, droppedBatch));

                    //                  out of order sampling
//                    streamSampler.setFilterRate(ApproxStabilizer.approxOutOfOrder(queueFilledPercentage, eventTimestamp));
//                    droppedBatch = false;


//                  Impact based dropping
//                    if (counter > 1000) {
//                        streamSampler.setFilterRate(ApproxStabilizer.approxImpactModel(queueFilledPercentage, humidity));
//                    }
//                  distinct event filter
//                    streamSampler.setFilterRate(ApproxStabilizer.approxDistinctEvents(queueFilledPercentage, humidity));

//                  feedback event filter
//                    streamSampler.setFilterRate(ApproxFeedbackStabilizer.approxFeedbackDrop(queueFilledPercentage, humidity));
                    if (streamSampler.isAddable(counter)) {
                        if (!dataPublisher.tryPublish(event)) {
                            uncontrolledDropped++;
                            totalDropped++;
                            droppedInWindow++;
                        }
                    } else {
                        controlledDropped++;
                        totalDropped++;
                        droppedInWindow++;
                    }
                    if ((counter > warmUpCount) && ((counter + 1) % elapsedCount == 0)) {

                        long currentTime = System.currentTimeMillis();
                        long elapsedTime = currentTime - lastTime;
                        double throughputPerSecond = (((double) elapsedCount) / elapsedTime) * 1000;
                        lastTime = currentTime;
                        log.info("Sent " + elapsedCount + " sensor events in " + elapsedTime
                                + " milliseconds with total throughput of " + decimalFormat.format(throughputPerSecond)
                                + " events per second.");
                    }

                    FeedbackProcessor.getInstance().incrementTotalEvents();
                    counter++;

//                    if ((System.currentTimeMillis() - startTime) >= 1000) {
//                        System.out.println("Time Passage : 1 s : Events sent : " + (counter - newCounter));
//                        startTime = System.currentTimeMillis();
//                        newCounter = counter;
//                    }
//                  send punctuation
                    if (counter % Constants.EVENTS_IN_LENGTH_BATCH == 0) {
                        System.out.println("dropped in window : " + droppedInWindow);//TODO
                        new TCPClient(Constants.TCP_HOST, Constants.TCP_PORT).sendMsg("punctuation : "
                                + (Constants.EVENTS_IN_LENGTH_BATCH - droppedInWindow) + ", counter : " + counter);
                        if (droppedInWindow == Constants.EVENTS_IN_LENGTH_BATCH) {
                            droppedBatch = true;
                        }
                        droppedInWindow = 0;
                    }
//
//                  send special event : sensorId = -1
//                    if (counter % 500 == 0) {
//                        Event specialEvent = new Event(streamId, System.currentTimeMillis(),
//                                new Object[]{System.currentTimeMillis(), isPowerSaveEnabled, -1,
//                                        "temperature-" + counter},
//                                new Object[]{longitude, latitude},
//                                new Object[]{humidity, sensorValue});
//                        dataPublisher.tryPublish(specialEvent);
//                    }

//                  TPS control
//                    if (counter % 1 == 0) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {

                        }
//                    }
                }

                System.out.println("EVENT SENDING FINISHED.................");
                System.out.println("UNCONTROLLED DROPPED : " + uncontrolledDropped);
                System.out.println("CONTROLLED DROPPED : " + controlledDropped);
                System.out.println("TOTAL DROPPED : " + totalDropped);
                System.out.println("TOTAL NO OF EVENTS : " + eventCount);


                try {
                    dataPublisher.shutdownWithAgent();
                } catch (Throwable e) {
                    log.error(e);
                }

            }
        }).start();
    }

    private static void sendWarmUpEvents(DataPublisher dataPublisher, long warmUpCount) {
        long counter = 0;
        Random randomGenerator = new Random();
        String streamId = "org.wso2.event.sensor.stream:1.0.0";

        while (counter < warmUpCount) {
            boolean isPowerSaveEnabled = randomGenerator.nextBoolean();
            int sensorId = randomGenerator.nextInt();
            double longitude = randomGenerator.nextDouble();
            double latitude = randomGenerator.nextDouble();
            float humidity = randomGenerator.nextFloat();
            double sensorValue = randomGenerator.nextDouble();
            Event event = new Event(streamId, System.currentTimeMillis(),
                    new Object[]{System.currentTimeMillis(), isPowerSaveEnabled, sensorId,
                            "warmup-" + counter},
                    new Object[]{longitude, latitude},
                    new Object[]{humidity, sensorValue});

            dataPublisher.publish(event);
            counter++;
        }
    }

    private static void publishEventsForLatency(DataPublisher dataPublisher, long eventCount, long elapsedCount,
                                                long warmUpCount) {
        sendWarmUpEvents(dataPublisher, warmUpCount);
        long counter = 0;
        Random randomGenerator = new Random();
        String streamId = "org.wso2.event.sensor.stream:1.0.0";

        while (counter < eventCount) {
            boolean isPowerSaveEnabled = randomGenerator.nextBoolean();
            int sensorId = randomGenerator.nextInt();
            double longitude = randomGenerator.nextDouble();
            double latitude = randomGenerator.nextDouble();
            float humidity = randomGenerator.nextFloat();
            double sensorValue = randomGenerator.nextDouble();
            Event event = new Event(streamId, System.currentTimeMillis(),
                    new Object[]{System.currentTimeMillis(), isPowerSaveEnabled, sensorId,
                            "temperature-" + counter},
                    new Object[]{longitude, latitude},
                    new Object[]{humidity, sensorValue});

            dataPublisher.publish(event);
            log.info("Sent event " + counter + " at " + System.currentTimeMillis());

            if (elapsedCount > 0) {
                try {
                    Thread.sleep(elapsedCount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            counter++;
        }
    }


    //  file read for event generation
    private static void initFileRead(String FILENAME) {
        try {
            fr = new FileReader(FILENAME);
            br = new BufferedReader(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Object[] getNextEventFromFile() {
        try {
            if ((sCurrentLine = br.readLine()) != null) {
                String[] strArr = sCurrentLine.trim().split(",");
                return new Object[]{Integer.valueOf(strArr[0]), Long.valueOf(strArr[1])};
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sCurrentLine == null) {
                try {
                    if (br != null) {
                        br.close();
                    }
                    if (fr != null) {
                        fr.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

}
