/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import java.util.ArrayList;
import org.opencv.core.Mat;

/**
 *
 * @author Morten
 */
public class Regulator
{

    private ArrayList<Mat> rawCoordinates;
    private ArrayList<double[]> filteredCoordinates;
    private final double CIRCLE_DIAMETER = 83;

    public Regulator()
    {
        rawCoordinates = new ArrayList<>();
        filteredCoordinates = new ArrayList<>();
    }

    /**
     *
     * Returns the average from the 6 last coordinates
     *
     */
    public void CoordinateFilter()
    {

        int elements = 9;
        double[] avg = new double[3];
        double sumX = 0;
        double sumY = 0;
        double sumR = 0;
        if (rawCoordinates.size() > 10) {
            rawCoordinates.remove(0);
        }
        for (int i = 0; i < (elements - 1); i++) {
            sumX = sumX + (double) rawCoordinates.get(i).get(0, 0)[0];
            sumY = sumY + (double) rawCoordinates.get(i).get(0, 0)[1];
            sumR = sumR + (double) rawCoordinates.get(i).get(0, 0)[2];
        }
        avg[0] = sumX / elements;
        avg[1] = sumY / elements;
        avg[2] = sumR / elements;
        filteredCoordinates.add(avg);
        if (rawCoordinates.size() > elements) {
            rawCoordinates.remove(0);
        }
    }

    /**
     * Add a new raw coordinate
     *
     * @param v
     */
    public synchronized void AddNewCoordinate(Mat v)
    {
        if (v.cols() == 1 && v.rows() == 1) {
            rawCoordinates.add(v);
        }        
        CoordinateFilter();
    }

    public synchronized Mat getLatestCoordinate()
    {
        Mat mat = new Mat();
        mat.put(0, 0, filteredCoordinates.get(filteredCoordinates.size() - 1));
        return mat;
    }

    public void DistanceEstimate(double[] circleinfo)
    {
        double realRad = CIRCLE_DIAMETER / 2;
        double virtualRadius = circleinfo[3];

    }

}
