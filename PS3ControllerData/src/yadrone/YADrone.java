/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vegard
 */
public class YADrone {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       Semaphore mySem = new Semaphore(1,true);
        ControllerStateStorage store = new ControllerStateStorage();
        IARDrone drone = null;
        try {
            drone = new ARDrone();
            drone.start();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        DroneGUI ui = new DroneGUI(drone);
        ui.start();
        try {
            PS3ControllerReader reader = new PS3ControllerReader(mySem, store);
        } catch (IOException ex) {
            Logger.getLogger(YADrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        DroneControl cont = new DroneControl(drone, mySem, store);
    }
}

