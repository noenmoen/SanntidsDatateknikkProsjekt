/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DroneProject;

import com.codeminders.ardrone.controllers.*;
import java.io.IOException;
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
    private ControllerStateStorage storage;

    public PS3ControllerReader(Controller c, Semaphore s, ControllerStateStorage storage) {
        sem = s;
        this.c = c;
        this.storage = storage;
    }

    @Override
    public void run() {
        try {

            while (true) {

                state = c.read();
                setState(state);
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
            storage.setState(state);
            System.out.println("Controller set the state.");
        }
        sem.release();
    }

    public boolean hasData() {
        return state != null;
    }
}

    
