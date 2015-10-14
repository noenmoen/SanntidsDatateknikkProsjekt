/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 *
 * @author Martin Strøm Pedersen
 */
public class DataHandler
{

    private long lastTimeCircleDetected = 0;
    private final long CIRCLE_EXPIRATION_TIME = 1000;
    private int centroidAndRadiusCapacity = 3;
    private Deque<Integer[]> centroidAndRadius = new ArrayDeque<>();

    public DataHandler()
    {
    }

    public synchronized void addCentroidAndRadius(Integer[] centroidAndRadius)
    {
        try {
            this.centroidAndRadius.add(centroidAndRadius);
            lastTimeCircleDetected = System.currentTimeMillis();
            if (this.centroidAndRadius.size() > centroidAndRadiusCapacity) {
                this.centroidAndRadius.remove();
            }
        }
        catch (Exception e) {
        }
    }

    public synchronized Integer[] getCentroidAndRadius()
    {
        return centroidAndRadius.peekLast();
    }

    public synchronized boolean isCircleDataFresh()
    {
        return (lastTimeCircleDetected + CIRCLE_EXPIRATION_TIME)
                > System.currentTimeMillis();
    }

    private boolean circleFilter(Integer[] centroidAndRadius)
    {
        double sumCentroid = 0;
        double sumRadius = 0;
        double meanCentroid = 0;
        double meanRadius = 0;
        double devCentroid = 0;
        double devRadius = 0;
        for (Integer[] ints : this.centroidAndRadius) {
            sumCentroid += ints[0];
            sumRadius += ints[1];
        }
        meanCentroid = sumCentroid / this.centroidAndRadius.size();
        meanRadius = sumRadius / this.centroidAndRadius.size();
        for (Integer[] ints : this.centroidAndRadius) {
            devCentroid += Math.pow(ints[0] - meanCentroid, 2);
            devRadius += Math.pow(ints[1] - meanRadius, 2);
        }
        devCentroid = Math.sqrt(devCentroid / this.centroidAndRadius.size());
        devRadius   = Math.sqrt(devRadius / this.centroidAndRadius.size());
        return true;
    }
}
