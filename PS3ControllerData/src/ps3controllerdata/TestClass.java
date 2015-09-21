/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ps3controllerdata;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.controllers.*;
import com.codeminders.ardrone.controllers.hid.manager.HIDControllerFinder;
import com.codeminders.hidapi.ClassPathLibraryLoader;

import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 *
 * @author vegard
 */
public class TestClass {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    private static final long CONNECT_TIMEOUT = 3000;

    public static void main(String[] args) throws IOException, InterruptedException {
        Semaphore sem = new Semaphore(1, true);
        ClassPathLibraryLoader.loadNativeHIDLibrary();
        ControllerStateStorage storage = new ControllerStateStorage();

        Controller c = HIDControllerFinder.findController();
        PS3ControllerRead reader = new PS3ControllerRead(c, sem, storage);

        ARDrone drone = null;

        try {

            drone = new ARDrone();

            drone.connect();

            drone.clearEmergencySignal();

            drone.waitForReady(CONNECT_TIMEOUT);
            
            drone.trim();
            // Wait until drone is ready

        } catch (Throwable e) {
            System.out.println("Initializing drone failed." + e);
        }
        if (drone != null) {
            System.out.println("Drone version: " + drone.getDroneVersion());
            //System.out.println("Drone config: " + drone.readDroneConfiguration());
        }

        
        
        DroneControl dc = new DroneControl(drone, sem, storage);
        reader.start();
        dc.start();

    }

}
