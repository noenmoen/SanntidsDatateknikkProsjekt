/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ps3controllerdata;

import com.codeminders.ardrone.controllers.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vegard Class for testing the dualshock 3 -> java inteface Prints out
 * the states of the ds3
 */
public class PS3ControllerRead extends Thread {

    private final Controller c;
    private GameControllerState state;

    public PS3ControllerRead(Controller c) {
        this.c = c;
    }

    @Override
    public void run() {
        try {

            while (true) {

                state = c.read();

            }
        } catch (IOException ex) {
            Logger.getLogger(PS3ControllerRead.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public float getLeftStickX() {
        return (float) (state.getLeftJoystickX() / 1.28);
    }

    public float getLeftStickY() {
        return (float) (state.getLeftJoystickY() / 1.28);
    }

    public float getRightStickX() {
        return (float) (state.getRightJoystickX() / 1.28);
    }

    public float getRightStickY() {
        return (float) (state.getRightJoystickY() / 1.28);
    }

    public boolean getSquare() {
        return state.isSquare();
    }

    public boolean getTriangle() {
        return state.isTriangle();
    }

    public boolean getCircle() {
        return state.isCircle();
    }
    
    public boolean getCross() {
        return state.isCross();
    }
}
