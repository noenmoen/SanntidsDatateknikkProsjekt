/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

/**
 *
 * @author vegard
 */
import com.codeminders.ardrone.controllers.*;

import com.codeminders.ardrone.controllers.hid.manager.HIDControllerFinder;
import com.codeminders.hidapi.ClassPathLibraryLoader;
import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vegard Class for reading the state of the DualShock 3 controller
 */
public class PS3ControllerReader extends Thread {

    private Semaphore sem;
    private final Controller c;
    private GameControllerState state;
    private GameControllerState oldState;
    private ControllerStateStorage storage;

    public PS3ControllerReader(Semaphore s, ControllerStateStorage storage) throws IOException {
        ClassPathLibraryLoader.loadNativeHIDLibrary();
        c = HIDControllerFinder.findController();
        System.out.println("SET UP DS3 " + c.getName());
        sem = s;
        this.storage = storage;
    }

    @Override
    public void run() {
        oldState = null;
        try {

            while (c != null) { // if controller is connected

                state = c.read(); // read the current state
                ControllerStateChange cont_change = new ControllerStateChange(oldState, state);
                // Check if the state is different from the previous state
                if (cont_change.isChanged()) {
                    // Set the state
                    setState(state);
                }
                // Save the state for comparison next time
                oldState = state;

            }
        } catch (IOException ex) {
            Logger.getLogger(PS3ControllerReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    Method for storing the GameControllerState in the ControllerStateStorage
    */
    private void setState(GameControllerState s) {
        // Aquire the semaphore, granting access to the ControllerStateStorage
        try {
            sem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(PS3ControllerReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Check if DroneControl has read the previous state. If so, store the new one
        if (!storage.getAvailable()) {
            storage.setState(s);
        }
        // If cross is pressed, set a flag that lands the drone even if in auto mode.
        if (state.isCross()) {
            // If the previous flag is read
            if (!storage.isNewFlag()) {
                storage.setLandingFlag();
            }
        }
        sem.release();
    }

}
