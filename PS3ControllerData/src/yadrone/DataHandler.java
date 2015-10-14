/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import java.util.ArrayDeque;
import java.util.Deque;
import org.opencv.core.Mat;

/**
 *
 * @author Martin Strøm Pedersen
 */
public class DataHandler
{

    private long lastTimeCircleDetected = 0;
    private final long CIRCLE_EXPIRATION_TIME = 1000;
    private int centroidAndRadiusCapacity = 3;
    private Deque<Mat> rawCoordinates = new ArrayDeque<>();

    public DataHandler()
    {
    }

    public synchronized void addCentroidAndRadius(Double[] centroidAndRadius)
    {
        try {
            this.rawCoordinates.add(centroidAndRadius);
            lastTimeCircleDetected = System.currentTimeMillis();
            if (this.rawCoordinates.size() > centroidAndRadiusCapacity) {
                this.rawCoordinates.remove();
            }
        }
        catch (Exception e) {
        }
    }

    public synchronized Double[] getCentroidAndRadius()
    {
        return rawCoordinates.peekLast();
    }

    public synchronized boolean isCircleDataFresh()
    {
        return (lastTimeCircleDetected + CIRCLE_EXPIRATION_TIME)
                > System.currentTimeMillis();
    }

    private boolean circleFilter(Integer[] centroidAndRadius)
    {
        double sumX = 0;
        double sumY = 0;
        double sumRadius = 0;
        double meanX = 0;
        double meanY = 0;
        double meanRadius = 0;
        double devCentroid = 0;
        double devRadius = 0;
        for (Mat values : this.rawCoordinates) {
            sumX += values.get(0, 0)[0];
            sumY += values[1];
            sumRadius += values[3];
        }
        meanX = sumX / this.rawCoordinates.size();
        meanY = sumY / this.rawCoordinates.size();
        meanRadius = meanRadius / this.rawCoordinates.size();
        for (Double[] values : this.rawCoordinates) {
            devCentroid += Math.pow(values[0] - meanX, 2);
            devRadius += Math.pow(values[1] - meanRadius, 2);
        }
        devCentroid = Math.sqrt(devCentroid / this.rawCoordinates.size());
        devRadius   = Math.sqrt(devRadius / this.rawCoordinates.size());
        return true;
    }
}
