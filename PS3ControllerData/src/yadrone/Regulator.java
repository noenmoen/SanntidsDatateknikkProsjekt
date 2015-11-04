/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.TimerTask;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Vegard
 */
public class Regulator extends TimerTask
{

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
    private boolean isReset;
    private boolean scanning;

    public Regulator(DroneControl dc, DataHandler dh, int CYCLE_TIME)
    {
        TIME_SHIFT = (float) CYCLE_TIME / 1000f;
        navData = new NavDataListener(dc.getDrone());
        this.dh = dh;
        setupControllers();
        autoMode = false;
        this.dc = dc;
//        autoMode = true; // for testing purposes, remove before flight!
    }

    public synchronized boolean isAutoMode()
    {
        return autoMode;
    }

    public synchronized void setAutoMode(boolean autoMode)
    {
        this.autoMode = autoMode;
    }

    /* Drone dynamics are unknown, these methods are for tuning the gain
     parameters in order to achieve acceptable performance
     @param: Kp = proportional gain, Ki = integral gain, Kd = derivative gain
     */
    public synchronized void setKpYaw(float kpYaw)
    {
        yawPID.setKp(kpYaw);
    }

    public synchronized void setKiYaw(float kiYaw)
    {
        yawPID.setKi(kiYaw);
    }

    public synchronized void setKdYaw(float kdYaw)
    {
        yawPID.setKd(kdYaw);
    }

    public synchronized void setKpZ(float kpZ)
    {
        zPID.setKp(kpZ);
    }

    public synchronized void setKiZ(float kiZ)
    {
        zPID.setKi(kiZ);
    }

    public synchronized void setKdZ(float kdZ)
    {
        zPID.setKd(kdZ);
    }

    public synchronized void setKpPitch(float kpPitch)
    {
        pitchPID.setKp(kpPitch);
    }

    public synchronized void setKiPitch(float kiPitch)
    {
        pitchPID.setKi(kiPitch);
    }

    public synchronized void setKdPitch(float kdPitch)
    {
        pitchPID.setKd(kdPitch);
    }

    public synchronized float getKpYaw()
    {
        return yawPID.getKp();
    }

    public synchronized float getKiYaw()
    {
        return yawPID.getKi();
    }

    public synchronized float getKdYaw()
    {
        return yawPID.getKd();
    }

    public synchronized float getKpZ()
    {
        return zPID.getKp();
    }

    public synchronized float getKiZ()
    {
        return zPID.getKi();
    }

    public synchronized float getKdZ()
    {
        return zPID.getKd();
    }

    public synchronized float getKpPitch()
    {
        return pitchPID.getKp();
    }

    public synchronized float getKiPitch()
    {
        return pitchPID.getKi();
    }

    public synchronized float getKdPitch()
    {
        return pitchPID.getKd();
    }

    @Override
    public void run()
    {

        long start = System.currentTimeMillis();
        // Only run while drone is in autonomous mode, and the drone has found a hulahoop
        if (autoMode && dh.HasCircle()) {
            float[] diff = new float[4];
            try {
                diff = dh.GetDiff();
            }
            catch (Exception e) {
                System.out.println("Automode: " + e);
            }
            isReset = false;
            yawAct = navData.getYaw() / 1000f; // angles from the drone is in 1/1000 degrees
            pitchAct = navData.getPitch() / 1000f;
            rollAct = navData.getRoll() / 1000f;
            zAct = navData.getExtAltitude().getRaw() / 1000f; // altitude from the drone is in mm

            float yaw = diff[0];
            float yawDes = yawAct + yaw; // convert desired angular movement to global yaw coordinates
            if (yawDes >= 180f) {
                yawDes = (yawAct - 360f) + yaw; // Compensate for illegal desired angles (-180<yaw<180)
            }
            else if (yawDes <= -180f) {
                yawDes = (yawAct + 360f) + yaw;
            }

            yawPID.setSetpoint(mapAngles(yawDes)); // map the desired yaw angle to values in [-1,1]
            yawPID.setInput(mapAngles(yawAct)); // map the actual yaw angle to values in [-1,1]
            droneInputs[3] = yawPID.runPID();
            // DEBUG

            float z = diff[1] / 100f;
            float zDes = zAct + z; // Convert desired upward movement to altitude referenced from ground
            zPID.setSetpoint(zDes);
            zPID.setInput(zAct);
            droneInputs[2] = zPID.runPID();
            // DEBUG
            System.out.println("Desired yaw angle: " + yawDes
                    + "  |  actual yaw angle: " + yawAct
                    + "  |  Yaw control input: " + droneInputs[3]
                    + "  |  Desired altitude: " + zDes
                    + "  |  actual altitude: " + zAct
                    + "  |  Z control input: " + droneInputs[2]);

            // TODO: control algorithms for pitch
            droneInputs[1] = droneInputs[0] = 0f;
            dc.move(droneInputs);
            scanning = false;
        }
        if (autoMode && !dh.HasCircle() && !scanning) {
            isReset = false;
            // Reset yaw and pitch PIDs
            pitchPID.reset();
            yawPID.reset();
            // Scan the surroundings for rings
//                zAct = navData.getExtAltitude().getRaw() / 1000f; // altitude from the drone is in mm
//                zPID.setSetpoint(1.0f); // Fly to 1,0 m height and scan
//                zPID.setInput(zAct);
            droneInputs[3] = 0.0f;
            droneInputs[2] = 0.0f;
            droneInputs[0] = droneInputs[1] = 0f;
            dc.move(droneInputs);
            scanning = true;
        }

        if (!autoMode && !isReset) {
            // if the drone is not in autoMode, we reset the controllers
            droneInputs[0] = droneInputs[1] = droneInputs[2] = droneInputs[3] = 0f;
            pitchPID.reset();
            zPID.reset();
            yawPID.reset();
            isReset = true;
            scanning = false;
        }
        System.out.println("Cycletime regulator: " + (System.currentTimeMillis() - start));
    }

    /*
     convert angles between -180 to 180 into values between -1 and 1 (float)
     */
    private float mapAngles(float angle)
    {
        return angle / 180f;
    }

    private void setupControllers()
    {
        // set up the PD-controller for the yaw axis
        yawPID = new PIDController(0, 0, 0, TIME_SHIFT);
        yawPID.setContinuous();
        yawPID.setInputRange(-1f, 1f);
        yawPID.setOutputRange(-0.5f, 0.5f);

        // set up the P-controller for the z axis
        zPID = new PIDController(0, 0, 0, TIME_SHIFT);
        zPID.setContinuous(false);
        zPID.setOutputRange(-0.5f, 0.5f);
        // set up the PD-controller for the pitch axis
        pitchPID = new PIDController(0, 0, 0, TIME_SHIFT);
        pitchPID.setContinuous(false);
        // Limit the pitch angle, aggressive manouvers are not desirable
        pitchPID.setOutputRange(-0.2f, 0.2f);
        // Load and set PID gains
        loadAndSetGainParameters();
    }

    private void loadAndSetGainParameters()
    {
        String s;
        try {
            s = FileUtils.readFileToString(
                    new File(System.getProperty("user.dir")
                            + "\\PIDparameters.txt"));
            String[] paramStrs = s.split(" ");

            float[] params = new float[Array.getLength(paramStrs)];
            for (int i = 0; i < Array.getLength(paramStrs); i++) {
                params[i] = Float.valueOf(paramStrs[i]);
            }
            yawPID.setKp(params[0]);
            yawPID.setKi(params[1]);
            yawPID.setKd(params[2]);
            zPID.setKp(params[3]);
            zPID.setKi(params[4]);
            zPID.setKd(params[5]);
            pitchPID.setKp(params[6]);
            pitchPID.setKi(params[7]);
            pitchPID.setKd(params[8]);
        }
        catch (IOException ex) {
            System.out.println("Parameter Loading Failed: " + ex);
        }

    }
}
