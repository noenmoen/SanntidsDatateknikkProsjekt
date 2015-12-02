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
import com.codeminders.ardrone.controllers.GameControllerState;

/**
 *
 * @author vegard
 * Class for storing the GameControllerState object used to control the drone.
 * Access is controlled by a shared Semaphore
 */
public class ControllerStateStorage {

    private boolean available;
    private boolean newFlag;
    private GameControllerState state;
    public ControllerStateStorage() {
        available = false;
    }
    // Check if the storage has a new GameControllerState
    public boolean getAvailable() {
        return available;
    }
    // Get the GameControllerState, set available=false
    public GameControllerState getState() {
        available = false;
        return state;

    }
    // Set the GameControllerState
    public void setState(GameControllerState state) {        
        this.state = state;
        available = true;
    }
    // Set a flag telling the drone to land.
    public void setLandingFlag() {
        newFlag = true;        
    }
    // Reset the landing flag
    public void getLandingFlag() {
        newFlag = false;
    }
    // Check if the landing flag is set
    public boolean isNewFlag() {
        return newFlag;
    }

}

