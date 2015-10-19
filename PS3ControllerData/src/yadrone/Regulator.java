/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import java.util.TimerTask;

/**
 *
 * @author Vegard
 */
public class Regulator extends TimerTask {

    private float[] droneInputs = new float[4];
    private float[] desValues = new float[4];
    private float yawAct;
    private float pitchAct;
    private float rollAct;
    private float zAct;
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
    private float pitchErr;
    private float pitchErrSum;
    private float pitchDerr;
    private float prevPitchErr;
    private float kpPitch;
    private float kiPitch;
    private float kdPitch;
    private float pitchSpeedOut;
    private float zSpeedOut;
    private boolean autoMode;
    private final float TIME_SHIFT;
    private NavDataListener navData;
    private DroneControl dc;
    private DataHandler dh;

    public Regulator(DroneControl dc,DataHandler dh) {
        TIME_SHIFT = 0.1f;
        navData = new NavDataListener(dc.getDrone());
        this.dh = dh;
    }

    public boolean isAutoMode() {
        return autoMode;
    }

    public void setAutoMode(boolean autoMode) {
        this.autoMode = autoMode;
    }

    public float[] getDroneInputs() {
        return droneInputs;
    }

    private float pitchPID(float pitchDes, float pitchAct) {
        pitchErr = pitchDes - pitchAct;
        pitchErrSum += pitchErr * TIME_SHIFT;
        pitchDerr = (pitchErr - prevPitchErr) / TIME_SHIFT;
        pitchSpeedOut = kpPitch * pitchErr + kiPitch * pitchErrSum + kdPitch * pitchDerr;
        prevPitchErr = pitchErr;
        return pitchSpeedOut;
    }

    public void setPitchTuning(float Kp, float Ki, float Kd) {
        kpPitch = Kp;
        kiPitch = Ki;
        kdPitch = Kd;
    }

    // PID algorithm for controlling the yaw angle of the drone
    private float yawPID(float yawDes, float yawAct) {
        yawErr = yawDes - yawAct;
        if(yawErr >= 180f) yawErr -= 360f; // Compensate for errors that cross 0 deg.
        yawErrSum += yawErr * TIME_SHIFT;
        yawDerr = (yawErr - prevYawErr) / TIME_SHIFT;
        yawSpeedOut = kpYaw * yawErr + kiYaw * yawErrSum + kdYaw * yawDerr;
        prevYawErr = yawErr;
        return yawSpeedOut;
    }
    /* Drone dynamics are unknown, this method is for tuning the gain
     parameters in order to achieve acceptable performance
    
     */

    public void setYawTuning(float Kp, float Ki, float Kd) {
        kpYaw = Kp;
        kiYaw = Ki;
        kdYaw = Kd;
    }

    // PID algorithm for controlling the altitude of the drone (relative to ground)
    private float zPID(float zDes, float zAct) {
        zErr = zDes - zAct;
        zErrSum += zErr * TIME_SHIFT;
        zDerr = (zErr - prevZerr) / TIME_SHIFT;
        zSpeedOut = kpZ * zErr + kiZ * zErrSum + kdZ * zDerr;
        prevZerr = zErr;
        return zSpeedOut;
    }
    /* Drone dynamics are unknown, this method is for tuning the gain
     parameters in order to achieve acceptable performance
    
     */

    public void setZtuning(float Kp, float Ki, float Kd) {
        kpZ = Kp;
        kiZ = Ki;
        kdZ = Kd;
    }
    /*
     Set the desired values for the drone
     Param: roll, pitch, z (altitude), yaw
     */

//    public synchronized void setDesValues() {
//        this.desValues = dh.GetDiff();
//    }
//
//    private synchronized float[] getDesValues() {
//        return desValues;
//    }

    public void run() {
        while (true) {
            while (autoMode) {
                yawAct = navData.getYaw() / 1000f; // angles from the drone is in 1/1000 degrees
                pitchAct = navData.getPitch() / 1000f;
                rollAct = navData.getRoll() / 1000f;
                zAct = navData.getExtAltitude().getRaw() / 1000f; // altitude from the drone is in mm

                float yaw = dh.GetDiff()[0];
                float yawDes = yawAct + yaw; // convert desired angular movement to global yaw coordinates
                if(yawDes >= 360f) yawDes = (yawAct - 360f) + yaw; // Compensate for illegal desired angles (0<yaw<360)
                else if(yawDes < 0) yawDes = (360f + yawAct) + yaw;
                droneInputs[3] = yawPID(yawDes, yawAct);

                float z = dh.GetDiff()[1];
                float zDes = zAct + z; // Convert desired upward movement to altitude referenced from ground
                droneInputs[2] = zPID(zDes, zAct);

                // TODO: control algorithms for roll and pitch
                droneInputs[1] = droneInputs[0] = 0f;
                dc.moveAuto(droneInputs);
            }
        }
    }
}
