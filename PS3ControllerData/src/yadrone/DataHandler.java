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
public class DataHandler extends Thread {

    private long lastTimeCircleDetected = 0;
    private final long CIRCLE_EXPIRATION_TIME = 1000;
    private final int CAPACITY = 15;
    private final double dev = 0.1;
    private Deque<double[]> centroidAndRadius = new ArrayDeque<>();
    private int imageWidth;
    private int imageHeight;
    

    public DataHandler() {
    }
    
    public synchronized void setImageWidthAndHight(Mat image){
        this.imageWidth = image.width();
        this.imageHeight = image.height();
    }
    
    public synchronized float[] GetDiff() {
        float[] diff = new float[4];
        diff[0] = (((float) getCentroidAndRadius()[0] - imageWidth / 2) 
                / imageWidth) * 93;
        diff[1] = (((float) getCentroidAndRadius()[1] - imageHeight / 2) 
                / imageHeight) * (imageHeight * 93 / imageWidth);

        return diff;
         
    }

    public synchronized void addCentroidAndRadius(Mat centroidAndRadius) {
        try {
            isCircleDataFresh();
            double[] circle = circleFilter(centroidAndRadius);
            if (circle != null) {
                this.centroidAndRadius.add(centroidAndRadius.get(0, 0));
                lastTimeCircleDetected = System.currentTimeMillis();
                if (this.centroidAndRadius.size() > CAPACITY) {
                    this.centroidAndRadius.remove();
                }
            }
        } catch (Exception e) {
        }
    }

    public synchronized double[] getCentroidAndRadius() {

        return centroidAndRadius.peekLast();
        
    }

    private synchronized boolean isCircleDataFresh() {

        if ((lastTimeCircleDetected + CIRCLE_EXPIRATION_TIME)
                > System.currentTimeMillis()) {
            return true;
        }
        centroidAndRadius.clear();
        return false;
    }

    private double[] circleFilter(Mat centroidAndRadius) {
        if (centroidAndRadius.cols() < 0) {
            return null;
        }
        double sumX = 0;
        double sumY = 0;
        double sumRadius = 0;
        double[] avg = new double[3];
        for (double[] values : this.centroidAndRadius) {
            sumX += values[0];
            sumY += values[1];
            sumRadius += values[3];
        }
        // Average
        avg[0] = sumX / this.centroidAndRadius.size();
        avg[1] = sumY / this.centroidAndRadius.size();
        avg[2] = sumRadius / this.centroidAndRadius.size();

        if (avg[0] == 0 && avg[0] == 0 && avg[0] == 0) {
            return centroidAndRadius.get(0, 0);
        }
        // Returns valid circle
        for (int x = 0; x < centroidAndRadius.cols(); x++) {
            for (int i = 0; i < 3; i++) {
                if ((double) centroidAndRadius.get(0, x)[i] < avg[i] * (1 + dev)
                        || (double) centroidAndRadius.get(0, x)[i] > avg[i] * (1 - dev)) {
                    return centroidAndRadius.get(0, x);
                }
            }
        }
        return null;
    }
}
