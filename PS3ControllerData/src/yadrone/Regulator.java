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

    public Regulator(DroneControl dc, DataHandler dh) {
        TIME_SHIFT = 0.1f;
        navData = new NavDataListener(dc.getDrone());
        this.dh = dh;
        //autoMode = false;
        autoMode = true; // for testing purposes, remove before flight!
    }

    public synchronized boolean isAutoMode() {
        return autoMode;
    }

    public synchronized void setAutoMode(boolean autoMode) {
        this.autoMode = autoMode;
    }

    private float pitchPID(float pitchDes, float pitchAct) {
        pitchErr = pitchDes - pitchAct;
        pitchErrSum += pitchErr * TIME_SHIFT;
        pitchDerr = (pitchErr - prevPitchErr) / TIME_SHIFT;
        pitchSpeedOut = kpPitch * pitchErr + kiPitch * pitchErrSum + kdPitch * pitchDerr;
        prevPitchErr = pitchErr;
        return pitchSpeedOut;
    }
    /* Drone dynamics are unknown, these methods are for tuning the gain
     parameters in order to achieve acceptable performance
     @param: Kp = proportional gain, Ki = integral gain, Kd = derivative gain
     */

    public synchronized void setKpYaw(float kpYaw) {
        this.kpYaw = kpYaw;
    }

    public synchronized void setKiYaw(float kiYaw) {
        this.kiYaw = kiYaw;
    }

    public synchronized void setKdYaw(float kdYaw) {
        this.kdYaw = kdYaw;
    }

    public synchronized void setKpZ(float kpZ) {
        this.kpZ = kpZ;
    }

    public synchronized void setKiZ(float kiZ) {
        this.kiZ = kiZ;
    }

    public synchronized void setKdZ(float kdZ) {
        this.kdZ = kdZ;
    }

    public synchronized void setKpPitch(float kpPitch) {
        this.kpPitch = kpPitch;
    }

    public synchronized void setKiPitch(float kiPitch) {
        this.kiPitch = kiPitch;
    }

    public synchronized void setKdPitch(float kdPitch) {
        this.kdPitch = kdPitch;
    }


    public synchronized float getKpYaw() {
        return kpYaw;
    }

    public synchronized float getKiYaw() {
        return kiYaw;
    }

    public synchronized float getKdYaw() {
        return kdYaw;
    }

    public synchronized float getKpZ() {
        return kpZ;
    }

    public synchronized float getKiZ() {
        return kiZ;
    }

    public synchronized float getKdZ() {
        return kdZ;
    }

    public synchronized float getKpPitch() {
        return kpPitch;
    }

    public synchronized float getKiPitch() {
        return kiPitch;
    }

    public synchronized float getKdPitch() {
        return kdPitch;
    }

    // PID algorithm for controlling the yaw angle of the drone
    private float yawPID(float yawDes, float yawAct) {
        yawErr = yawDes - yawAct;
        if (yawErr >= 180f) {
            yawErr -= 360f; // Compensate for errors that cross 0 deg.
        }
        yawErrSum += yawErr * TIME_SHIFT;
        yawDerr = (yawErr - prevYawErr) / TIME_SHIFT;
        yawSpeedOut = kpYaw * yawErr + kiYaw * yawErrSum + kdYaw * yawDerr;
        prevYawErr = yawErr;
        return yawSpeedOut;
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
  

    @Override
    public void run() {
        while (true) {
            // Only run while drone is in autonomous mode
            while (autoMode) {
                yawAct = navData.getYaw() / 1000f; // angles from the drone is in 1/1000 degrees
                pitchAct = navData.getPitch() / 1000f;
                rollAct = navData.getRoll() / 1000f;
                zAct = navData.getExtAltitude().getRaw() / 1000f; // altitude from the drone is in mm

                float yaw = dh.GetDiff()[0];
                float yawDes = yawAct + yaw; // convert desired angular movement to global yaw coordinates
                if (yawDes >= 360f) {
                    yawDes = (yawAct - 360f) + yaw; // Compensate for illegal desired angles (0<yaw<360)
                } else if (yawDes < 0) {
                    yawDes = (360f + yawAct) + yaw;
                }
                droneInputs[3] = yawPID(yawDes, yawAct);
//                System.out.println("Desired yaw angle: " + yawDes + " - actual yaw angle: " + yawAct);
//                System.out.println("-----------------------------------------------------------");

                float z = dh.GetDiff()[1];
                float zDes = zAct + z; // Convert desired upward movement to altitude referenced from ground
                droneInputs[2] = zPID(zDes, zAct);
//                System.out.println("Desired altitude: " + zDes + " - actual altitude: " + zAct);
//                System.out.println("-----------------------------------------------------------");

                // TODO: control algorithms for roll and pitch
                droneInputs[1] = droneInputs[0] = 0f;
                //dc.moveAuto(droneInputs); || testing
            }
        }
    }
}
