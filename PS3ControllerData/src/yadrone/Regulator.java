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
public class Regulator {

    private ArrayList<Mat> rawCoordinates;
    private ArrayList<double[]> filteredCoordinates;
    private final double CircleDiameter = 83;
    private float[] droneInputs = new float[4];
    private float yawErr;
    private float yawErrSum;
    private float yawDerr;
    private float prevYawErr;
    private float kpYaw;
    private float kiYaw;
    private float kdYaw;
    private float yawSpeedOut;
    private float zErr;
    private float zErrSum;
    private float zDerr;
    private float prevZerr;
    private float kpZ;
    private float kiZ;
    private float kdZ;
    private float zSpeedOut;
    private final float TIME_SHIFT;
    
    private final double CIRCLE_DIAMETER = 83;
    private final double dev = 1.1;

    public Regulator() {

        TIME_SHIFT = 0.1f;

        rawCoordinates = new ArrayList<>();
        filteredCoordinates = new ArrayList<>();

    }

    /**
     *
     * Returns the average from the 6 last coordinates
     *
     */
    public void CoordinateFilter() {

        int elements = 9;
        double[] avg = new double[3];
        double[] stdD = new double[3];
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
        for (int x = 0; x < (elements - 1); x++) {
            for (int i = 0; i < 3; i++) {
                if ((double) rawCoordinates.get(x).get(0, 0)[i] > avg[i] * dev
                        || (double) rawCoordinates.get(x).get(0, 0)[i] < avg[i] / dev) {
                    rawCoordinates.remove(x);
                }
            }
        }

        if (rawCoordinates.size() > elements) {
            rawCoordinates.remove(0);
        }

    }

    /**
     * Add a new raw coordinate
     *
     * @param v
     */
    public synchronized void AddNewCoordinate(Mat v) {
        if (v.cols() == 1 && v.rows() == 1) {
            rawCoordinates.add(v);
        }
        CoordinateFilter();
    }

    public synchronized Mat getLatestCoordinate() {
        Mat mat = new Mat();
        mat.put(0, 0, filteredCoordinates.get(filteredCoordinates.size() - 1));
        return mat;
    }

    public void DistanceEstimate(double[] circleinfo) {
        double realRad = CIRCLE_DIAMETER / 2;
        double virtualRadius = circleinfo[3];

    }

    
    public float[] getDroneInputs() {
        return droneInputs;
    }
    
    private float yawPID(float yawDes, float yawAct) {
        yawErr = yawDes - yawAct;
        yawErrSum += yawErr*TIME_SHIFT;
        yawDerr = (yawErr - prevYawErr)/TIME_SHIFT;
        yawSpeedOut = kpYaw*yawErr + kiYaw*yawErrSum + kdYaw*yawDerr;
        prevYawErr = yawErr;
        return yawSpeedOut;
    }
    public void setYawTuning(float Kp, float Ki, float Kd) {
        kpYaw = Kp;
        kiYaw = Ki;
        kdYaw = Kd;        
    }
    
    private float zPID(float zDes, float zAct) {
        zErr = zDes - zAct;
        zErrSum += zErr*TIME_SHIFT;
        zDerr = (zErr - prevZerr)/TIME_SHIFT;
        zSpeedOut = kpZ*zErr + kiZ*zErrSum + kdZ*zDerr;
        prevZerr = zErr;
        return zSpeedOut;
        
    }
    private void run() {
        
    }

}
