/*
 * Manages/filter data from OPEN CV data.
 */
package yadrone;

import java.util.ArrayDeque;
import java.util.Deque;
import org.opencv.core.Mat;

/**
 *
 * @author Martin Strøm Pedersen
 */
public class DataHandler {

    private long lastTimeCircleDetected = 0;
    private final long CIRCLE_EXPIRATION_TIME = 100;
    private final int CAPACITY = 25;
    private final double dev = 0.1;
    private Deque<double[]> centroidAndRadiusFilt = new ArrayDeque<>();
    private int imageWidth;
    private int imageHeight;
    private double[] avg = new double[3];
    public DataHandler() {
    }

    public synchronized double[] getAvg()
    {
        return avg;
    }

    /**
     * must be called first to get the correct values
     *
     * @param image
     */
    public synchronized void setImageWidthAndHight(Mat image) {
        this.imageWidth = image.width();
        this.imageHeight = image.height();
    }

    /**
     * Gets the mean values from the filtered array, converts it into degrees
     * and returns the values as a float[] array float[0] = YAW float[1] =
     * altitude difference from center image to center ring float[2] = null
     * float[3] = null
     *
     * @return float[]
     */
    public synchronized float[] GetDiff() {
        float[] diff = new float[4];
        diff[0] = (((float) getCentroidAndRadius()[0] - imageWidth / 2)
                / imageWidth) * 93;
        diff[1] = ((float) getCentroidAndRadius()[1] - imageHeight / 2);

        System.out.println("Filtered values: YAW diff: " + diff[0]
                + " Altitude Diff: " + diff[1]);
        return diff;

    }

    /**
     * Called by Circle detection, adds a new raw value to calculate the mean
     *
     * @param centroidAndRadius
     */
    public synchronized void addCentroidAndRadius(Mat centroidAndRadius) {
        try {
            isCircleDataFresh();
            double[] circle = circleFilter(centroidAndRadius);
            if (circle != null) {
                this.centroidAndRadiusFilt.add(circle);
                lastTimeCircleDetected = System.currentTimeMillis();
                if (this.centroidAndRadiusFilt.size() > CAPACITY) {
                    this.centroidAndRadiusFilt.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * returns the latest value from the filtered data
     *
     * @return double[]
     */
    public synchronized double[] getCentroidAndRadius() {

        return centroidAndRadiusFilt.peekLast();

    }

    public synchronized boolean HasCircle() {
        if (centroidAndRadiusFilt.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if the data from circledetection is fresh
     *
     * @return
     */
    public synchronized boolean isCircleDataFresh() {

        if ((lastTimeCircleDetected + CIRCLE_EXPIRATION_TIME)
                > System.currentTimeMillis()) {
            return true;
        }
        centroidAndRadiusFilt.clear();
        return false;
    }

    /**
     * takes the mean value of N- elements from raw circle data. It removes
     * unvalid circles, and chooses the right circle if there are more than one.
     *
     * @param centroidAndRadius
     * @return double[]
     */
    private double[] circleFilter(Mat centroidAndRadius) {
        if (centroidAndRadius.cols() < 0) {
            return null;
        }
        double sumX = 0;
        double sumY = 0;
        double sumRadius = 0;
        
        avg[0] = 0;
        avg[1] = 0;
        avg[2] = 0;

        for (double[] values : this.centroidAndRadiusFilt) {
            sumX += values[0];
            sumY += values[1];
            sumRadius += values[2];
        }
        // Average
        if (this.centroidAndRadiusFilt.size() > 0) {
            avg[0] = sumX / this.centroidAndRadiusFilt.size();
            avg[1] = sumY / this.centroidAndRadiusFilt.size();
            avg[2] = sumRadius / this.centroidAndRadiusFilt.size();
        }

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
