/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DroneProject;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.controllers.GameControllerState;
import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vegard Class for testing flight of the drone using PS3 dual shock
 * controller
 */
public class DroneControl extends TimerTask {

    private final ARDrone drone;
    private boolean freeroam;
    private Semaphore sem;
    private ControllerStateStorage storage;
    private GameControllerState state;

    public DroneControl(ARDrone drone, Semaphore s, ControllerStateStorage storage) {
        sem = s;
        this.storage = storage;
        this.drone = drone;
        freeroam = false;
    }

    @Override
    public void run() {
        while (true) {
            while (storage.getAvailable()) { // If the controller has produced new data

                try {
                    sem.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(DroneControl.class.getName()).log(Level.SEVERE, null, ex);
                }
                state = storage.getState();
                System.out.println("Dronecontrol got a state ");

                move(state);

                sem.release();
            }
        }
    }

    // Converting joystick coordinates (int from -128 to 127) to float values between 0 and +-100
    private float getLeftJoystickX() {
        return (float) (state.getLeftJoystickX() / 1.28);
    }

    private float getLeftJoystickY() {
        return (float) (state.getLeftJoystickY() / 1.28);
    }

    private float getRightJoystickX() {
        return (float) (state.getRightJoystickX() / 1.28);
    }

    private float getRightJoystickY() {
        return (float) (state.getRightJoystickY() / 1.28);
    }

    private void move(GameControllerState st) {
        if (st.isTriangle()) {
            System.out.println("Drone take off");
            System.out.println(drone.getState());
            try {
                drone.takeOff();
                System.out.println(drone.getState());
            } catch (IOException ex) {
                Logger.getLogger(DroneControl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // Pressing square will enable/disable free roaming
        // While free roaming, the drone is controlled by the analog sticks of the DS3
        if (st.isSquare()) {
            System.out.println("Freeroaming");
            freeroam = !freeroam;
            if (!freeroam) {
                try {
                    drone.move(0, 0, 0, 0); // Stop all movement of drone if free roam is disabled
                } catch (IOException ex) {
                    Logger.getLogger(DroneControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        // If free roam is enabled and the drone is hovering, it can be controlled by the DS3
        if (freeroam) {
            try {
                drone.move(getLeftJoystickX(), getLeftJoystickY(), getRightJoystickY(), getRightJoystickX());
            } catch (IOException ex) {
                Logger.getLogger(DroneControl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // Pressing cross on the DS3 will make the drone land
        if (st.isCross()) {
            freeroam = false;
            try {
                drone.land();
            } catch (IOException ex) {
                Logger.getLogger(DroneControl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
