/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;


/**
 *
 * @author Vegard
 */
public class Regulator {


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

    public Regulator() {
        TIME_SHIFT = 0.1f;
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
