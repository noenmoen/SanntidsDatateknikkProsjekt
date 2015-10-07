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

        MAN_MODE, AUTO_MODE
    };

    private final IARDrone drone;
    private boolean freeroam;
    private Semaphore sem;
    private ControllerStateStorage storage;
    private GameControllerState state;
    private NavDataListener navData;
    private DroneMode mode;

    public DroneControl(IARDrone drone, Semaphore s, ControllerStateStorage storage) {
        sem = s;
        navData = new NavDataListener(drone);
        this.storage = storage;
        this.drone = drone;
        freeroam = false;
    }

    public DroneControl(Semaphore s, ControllerStateStorage storage) {
        sem = s;
        this.storage = storage;
        freeroam = false;
        drone = null;
    }

    @Override
    public void run() {
        while (true) {
            switch (mode) {
                case MAN_MODE:
                    while (storage.getAvailable()) { // If the controller has produced new data

                        try {
                            sem.acquire();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(DroneControl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        state = storage.getState();

                        move(state);

                        sem.release();
                        break;
                    }
                case AUTO_MODE:
                    drone.getCommandManager().landing().doFor(2000); // Automatic mode not implemented yet
                    
            }
        }
    }

    // Converting joystick coordinates (int from -128 to 127) to int values between 0 and +-100
    private int getLeftJoystickX() {
        return (int) (state.getLeftJoystickX() / 1.28);
    }

    private int getLeftJoystickY() {
        return (int) (state.getLeftJoystickY() / 1.28);
    }

    private int getRightJoystickX() {
        return (int) (state.getRightJoystickX() / 1.28);
    }

    private int getRightJoystickY() {
        return (int) (state.getRightJoystickY() / 1.28);
    }

    private void move(GameControllerState st) {
        if (st.isTriangle()) {
            System.out.println("Drone take off");
            drone.getCommandManager().takeOff().doFor(2000);
            //drone.takeOff();

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
            //System.out.println("Coord: left x = " + getLeftJoystickX()+"left y = " + -getLeftJoystickY()+"right y = " + -getRightJoystickY()+"right x = " + getRightJoystickX());
            drone.move3D(getLeftJoystickX(), -getLeftJoystickY(), -getRightJoystickY(), getRightJoystickX());

        }
        // Pressing cross on the DS3 will make the drone land
        if (st.isCross()) {
            System.out.println("Drone landing");
            freeroam = false;
            drone.getCommandManager().landing().doFor(3000);
            //drone.landing();

        }
    }

    public void setMode(DroneMode mode) {
        this.mode = mode;
    }
}
