/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ps3controllerdata;

import com.codeminders.ardrone.ARDrone;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vegard
 * Class for testing flight of the drone using PS3 dual shock controller
 */
public class DroneControl extends Thread {

    private final ARDrone drone;
    private final PS3ControllerRead reader;
    private boolean freeroam;

    public DroneControl(ARDrone drone, PS3ControllerRead reader) {
        this.reader = reader;
        this.drone = drone;
        freeroam = false;
    }

    @Override
    public void run() {
        while (true) {
            // Pressing triangle on the DS3 will make the drone hover
            if (reader.getTriangle()) {
                try {
                    drone.hover();
                } catch (IOException ex) {
                    Logger.getLogger(DroneControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            // Pressing square will enable/disable free roaming
            // While free roaming, the drone is controlled by the analog sticks of the DS3
            if (reader.getSquare()) {
                freeroam = !freeroam;
                if(!freeroam) try {
                    drone.move(0,0,0,0); // Stop all movement of drone if free roam is disabled
                } catch (IOException ex) {
                    Logger.getLogger(DroneControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            // If free roam is enabled and the drone is hovering, it can be controlled by the DS3
            if (freeroam) {
                try {
                    drone.move(reader.getLeftStickX(), reader.getLeftStickY(), reader.getRightStickY(), reader.getRightStickX());
                } catch (IOException ex) {
                    Logger.getLogger(DroneControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            // Pressing cross on the DS3 will make the drone land
            if (reader.getCross()) {
                freeroam = false;
                try {
                    drone.land();
                } catch (IOException ex) {
                    Logger.getLogger(DroneControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
