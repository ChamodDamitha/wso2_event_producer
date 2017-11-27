package org.wso2.carbon.sample.performance.ApproxStabilizer;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by chamod on 11/14/17.
 */
public class CounterGenerator {
    private ArrayList<Integer> counters;
    private Random random;

    public CounterGenerator(int noOfEvents, double uniqueness) {
        random = new Random(124);
        counters = new ArrayList<>();
        initCounters(noOfEvents, uniqueness);
    }

    private void initCounters(int noOfEvents, double uniqueness) {
        int n = (int) uniqueness;

        for (int i = 0; i < noOfEvents; i++) {
            counters.add(i / n);
        }
    }

    public int getNextCounter(){
        int i = random.nextInt(counters.size());
        return counters.remove(i);
    }
}
