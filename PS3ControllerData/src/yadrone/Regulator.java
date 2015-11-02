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
    private float yawAct;
    private float pitchAct;
    private float rollAct;
    private float zAct;
    private boolean autoMode;
    private final float TIME_SHIFT;
    private NavDataListener navData;
    private DroneControl dc;
    private DataHandler dh;
    private PIDController yawPID;
    private PIDController zPID;
    private PIDController pitchPID;

    public Regulator(DroneControl dc, DataHandler dh, float CYCLE_TIME) {
        TIME_SHIFT = CYCLE_TIME;
        navData = new NavDataListener(dc.getDrone());
        this.dh = dh;

        // set up the PD-controller for the yaw axis
        yawPID = new PIDController(0, 0, 0, TIME_SHIFT);
        yawPID.setContinuous();
        yawPID.setInputRange(-1f, 1f);
        yawPID.setOutputRange(-0.5f, 0.5f);
        yawPID.setKp(4.5f);
        yawPID.setKi(0.0f);
        yawPID.setKd(0.65f);

        // set up the P-controller for the z axis
        zPID = new PIDController(0, 0, 0, TIME_SHIFT);
        zPID.setContinuous(false);
        zPID.setOutputRange(-0.5f, 0.5f);
        zPID.setKp(0.65f);
        zPID.setKi(0.0f);
        zPID.setKd(0.0f);
        // set up the PD-controller for the pitch axis
        pitchPID = new PIDController(0, 0, 0, TIME_SHIFT);
        pitchPID.setContinuous(false);
        pitchPID.setOutputRange(-0.2f, 0.2f);
        pitchPID.setKp(0.2f);
        pitchPID.setKi(0.0f);
        pitchPID.setKd(0.15f);
        autoMode = false;
//        autoMode = true; // for testing purposes, remove before flight!
    }

    public synchronized boolean isAutoMode() {
        return autoMode;
    }

    public synchronized void setAutoMode(boolean autoMode) {
        this.autoMode = autoMode;
    }

    /* Drone dynamics are unknown, these methods are for tuning the gain
     parameters in order to achieve acceptable performance
     @param: Kp = proportional gain, Ki = integral gain, Kd = derivative gain
     */
    public synchronized void setKpYaw(float kpYaw) {
        yawPID.setKp(kpYaw);
    }

    public synchronized void setKiYaw(float kiYaw) {
        yawPID.setKi(kiYaw);
    }

    public synchronized void setKdYaw(float kdYaw) {
        yawPID.setKd(kdYaw);
    }

    public synchronized void setKpZ(float kpZ) {
        zPID.setKp(kpZ);
    }

    public synchronized void setKiZ(float kiZ) {
        zPID.setKi(kiZ);
    }

    public synchronized void setKdZ(float kdZ) {
        zPID.setKd(kdZ);
    }

    public synchronized void setKpPitch(float kpPitch) {
        pitchPID.setKp(kpPitch);
    }

    public synchronized void setKiPitch(float kiPitch) {
        pitchPID.setKi(kiPitch);
    }

    public synchronized void setKdPitch(float kdPitch) {
        pitchPID.setKd(kdPitch);
    }

    public synchronized float getKpYaw() {
        return yawPID.getKp();
    }

    public synchronized float getKiYaw() {
        return yawPID.getKi();
    }

    public synchronized float getKdYaw() {
        return yawPID.getKd();
    }

    public synchronized float getKpZ() {
        return zPID.getKp();
    }

    public synchronized float getKiZ() {
        return zPID.getKi();
    }

    public synchronized float getKdZ() {
        return zPID.getKd();
    }

    public synchronized float getKpPitch() {
        return pitchPID.getKp();
    }

    public synchronized float getKiPitch() {
        return pitchPID.getKi();
    }

    public synchronized float getKdPitch() {
        return pitchPID.getKd();
    }

    @Override
    public void run() {
        while (true) {
            // Only run while drone is in autonomous mode, and the drone has found a hulahoop
            while (autoMode && dh.HasCircle()) {
                yawAct = navData.getYaw() / 1000f; // angles from the drone is in 1/1000 degrees
                pitchAct = navData.getPitch() / 1000f;
                rollAct = navData.getRoll() / 1000f;
                zAct = navData.getExtAltitude().getRaw() / 1000f; // altitude from the drone is in mm

                float yaw = dh.GetDiff()[0];
                float yawDes = yawAct + yaw; // convert desired angular movement to global yaw coordinates
                if (yawDes >= 180f) {
                    yawDes = (yawAct - 360f) + yaw; // Compensate for illegal desired angles (-180<yaw<180)
                } else if (yawDes <= -180f) {
                    yawDes = (yawAct + 360f) + yaw;
                }

                yawPID.setSetpoint(mapAngles(yawDes)); // map the desired yaw angle to values in [-1,1]
                yawPID.setInput(mapAngles(yawAct)); // map the actual yaw angle to values in [-1,1]
                droneInputs[3] = yawPID.runPID();
                // DEBUG
                System.out.println("Desired yaw angle: " + yawDes + " - actual yaw angle: " + yawAct);
                System.out.println("-----------------------------------------------------------");

                float z = dh.GetDiff()[1] / 100f;
                float zDes = zAct + z; // Convert desired upward movement to altitude referenced from ground
                zPID.setSetpoint(zDes);
                zPID.setInput(zAct);
                droneInputs[2] = zPID.runPID();
                // DEBUG
                System.out.println("Desired altitude: " + zDes + " - actual altitude: " + zAct);
                System.out.println("-----------------------------------------------------------");

                // TODO: control algorithms for roll and pitch
                droneInputs[1] = droneInputs[0] = 0f;
                dc.move(droneInputs);
            }

            while (autoMode) {
                // if the drone is not in autoMode, we reset the controllers
                droneInputs[0] = droneInputs[1] = droneInputs[2] = droneInputs[3] = 0f;
                dc.move(droneInputs);
                pitchPID.reset();
                zPID.reset();
                yawPID.reset();
            }
        }
    }
    /*
     convert angles between -180 to 180 into values between -1 and 1 (float)
     */

    private float mapAngles(float angle) {
        return angle / 180f;
    }
}
