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
 * @author vegard Class for testing the dualshock 3 -> java inteface Prints out
 * the states of the ds3
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

            while (c != null) {

                state = c.read();
                ControllerStateChange cont_change = new ControllerStateChange(oldState, state);
                if (cont_change.isButtonStateChanged() || cont_change.isJoysticksChanged())
                setState(state);
                oldState = state;
            }
        } catch (IOException ex) {
            Logger.getLogger(PS3ControllerReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setState(GameControllerState s) {
        try {
            sem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(PS3ControllerReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!storage.getAvailable()) {
            storage.setState(s);
            
        }
        sem.release();
    }

}
