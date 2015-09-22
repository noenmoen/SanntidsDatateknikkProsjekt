/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DroneProject;

import com.codeminders.ardrone.controllers.GameControllerState;

/**
 *
 * @author vegard
 * Class for storing the GameControllerState object used to control the drone.
 * Access is controlled by a shared Semaphore
 */
public class ControllerStateStorage {

    private boolean available;
    private GameControllerState state;
    public ControllerStateStorage() {
        available = false;
    }

    public boolean getAvailable() {
        return available;
    }

    public GameControllerState getState() {
        available = false;
        return state;

    }

    public void setState(GameControllerState state) {
        
        this.state = state;
        available = true;
    }

}
