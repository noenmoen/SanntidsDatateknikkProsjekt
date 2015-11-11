/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import com.codeminders.ardrone.controllers.GameControllerState;
import de.yadrone.base.IARDrone;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vegard Class for testing flight of the drone using PS3 dual shock
 * controller
 */
public class DroneControl extends Thread {

    public enum DroneMode {

        MAN_MODE, AUTO_MODE, LANDING;
    };

    private final IARDrone drone;
    private boolean freeroam;
    private Semaphore sem;
    private ControllerStateStorage storage;
    private GameControllerState state;
    private Regulator reg;
    private DroneMode mode;
    private boolean flying;

    public DroneControl(IARDrone drone, Semaphore s, ControllerStateStorage storage) {
        sem = s;
        this.storage = storage;
        this.drone = drone;
        freeroam = false;
        mode = DroneMode.MAN_MODE;

    }

    @Override
    public void run() {
        while (true) {
            DroneMode m = getDroneMode();
            switch (m) {
                case MAN_MODE:
                    if (reg.isAutoMode()) {
                        reg.setAutoMode(false);
                    }
                    while (storage.getAvailable()) { // If the controller has produced new data

                        try {
                            sem.acquire();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(DroneControl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        state = storage.getState();

                        moveMan(state);
                        // If a landing flag is still set while in man. mode, reset the flag.
                        if (storage.isNewFlag()) {
                            storage.getLandingFlag();
                        }
                        sem.release();

                    }
                    break;
                case AUTO_MODE:
                    // If the drone is not flying, take off.
                    if (!flying) {
                        drone.getCommandManager().flatTrim();
                        drone.getCommandManager().takeOff();
                        flying = true;
                    }
                    // Check for manual landing input from the DS3
                    try {
                        sem.acquire();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DroneControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // If the controller has set the landing flag, always land.
                    if (storage.isNewFlag()) {
                        storage.getLandingFlag();
                        setMode(DroneMode.LANDING);
                        sem.release();
                        break;
                    }
                    sem.release();
                    // If regulator is not in automode, set automode.
                    if (!reg.isAutoMode()) {
                        reg.setAutoMode(true);
                    }
                    break;
                // Land command from the GUI or DS3 (while automode)
                case LANDING:
                    drone.getCommandManager().landing().flatTrim();
                    flying = false;
            }
        }
    }

    // Converting joystick coordinates (int from -128 to 127) to float values between -1 and 1
    private float getLeftJoystickX() {
        return state.getLeftJoystickX() / 128f;
    }

    private float getLeftJoystickY() {
        return state.getLeftJoystickY() / 128f;
    }

    private float getRightJoystickX() {
        return state.getRightJoystickX() / 128f;
    }

    private float getRightJoystickY() {
        return state.getRightJoystickY() / 128f;
    }

    private void moveMan(GameControllerState st) {
        if (st.isTriangle()) {
            drone.getCommandManager().flatTrim();
            System.out.println("Drone take off");
            drone.getCommandManager().takeOff();
            flying = true;

        }
        // Pressing square will enable/disable free roaming
        // While free roaming, the drone is controlled by the analog sticks of the DS3
        if (st.isSquare()) {
            System.out.println("Freeroaming");
            freeroam = true;
        }
        if (st.isCircle()) {
            System.out.println("Disabled freeroam");
            freeroam = false;
            drone.freeze(); // Stop all movement of drone if free roam is disabled
        }
        // If free roam is enabled and the drone is hovering, it can be controlled by the DS3
        if (freeroam) {
            float[] inputs = new float[4];
            inputs[0] = getLeftJoystickX();
            inputs[1] = -getLeftJoystickY();
            inputs[2] = -getRightJoystickY();
            inputs[3] = getRightJoystickX();
            System.out.println("Coord: left x = " + getLeftJoystickX() + " left y = " + -getLeftJoystickY() + " right y = " + -getRightJoystickY() + " right x = " + getRightJoystickX());
            System.out.println("----------------------------------------------------------------------");
            move(inputs);

        }
        // Pressing cross on the DS3 will make the drone land
        if (st.isCross()) {
            System.out.println("Drone landing");
            freeroam = false;
            drone.getCommandManager().landing().
                    flatTrim();
            flying = false;

        }
    }

    public synchronized void setMode(DroneMode mode) {
        this.mode = mode;
    }

    private synchronized DroneMode getDroneMode() {
        return mode;
    }

    public void setRegulator(Regulator reg) {
        this.reg = reg;
    }

    public void move(float inputs[]) {
            drone.getCommandManager().move(inputs[0], inputs[1], inputs[2], inputs[3]);
    }

    public IARDrone getDrone() {
        return drone;
    }
}
