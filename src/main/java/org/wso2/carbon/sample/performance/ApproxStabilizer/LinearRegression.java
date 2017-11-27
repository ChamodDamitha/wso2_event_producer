package org.wso2.carbon.sample.performance.ApproxStabilizer;

import org.omg.CORBA.portable.ValueOutputStream;

/**
 * Created by chamod on 11/15/17.
 */
public class LinearRegression {
//    private double maxY = Double.MAX_VALUE;

    private int n;

    private double sigmaX1;
    private double sigmaX2;
    private double sigmaY;
    ;
    private double sigmaX1_2;
    private double sigmaX2_2;
    private double sigmaX1Y;
    private double sigmaX2Y;
    private double sigmaX1X2;

    private double a;
    private double b1;
    private double b2;

    public LinearRegression() {
        sigmaX1 = 0;
        sigmaX2 = 0;
        sigmaY = 0;
        sigmaX1_2 = 0;
        sigmaX2_2 = 0;
        sigmaX1Y = 0;
        sigmaX2Y = 0;
        sigmaX1X2 = 0;
        n = 0;
    }

    public void addPoint(double x1, double x2, double y) {
//        if (y > maxY ){
//            maxY = y;
//            System.out.println("y : " + y);//todo
//        }

        sigmaX1 += x1;
        sigmaX2 += x2;
        sigmaY += y;
        sigmaX1_2 += (x1 * x1);
        sigmaX2_2 += (x2 * x2);
        sigmaX1Y += (x1 * y);
        sigmaX2Y += (x2 * y);
        sigmaX1X2 += (x1 * x2);
        n++;
        calculateParams();
    }

    private void calculateParams() {
        b1 = (sigmaX2_2 * sigmaX1Y - sigmaX1X2 * sigmaX2Y) / (sigmaX1_2 * sigmaX2_2 - sigmaX1X2 * sigmaX1X2);
        b2 = (sigmaX1_2 * sigmaX2Y - sigmaX1X2 * sigmaX1Y) / (sigmaX1_2 * sigmaX2_2 - sigmaX1X2 * sigmaX1X2);
        a = (sigmaY - b1 * sigmaX1 - b2 * sigmaX2) / n;
    }

    public double getForecastY(double x1, double x2) {
        return a + b1 * x1 + b2 * x2;
    }
}
