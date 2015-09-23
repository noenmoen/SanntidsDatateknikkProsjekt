/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DroneProject;

import com.codeminders.ardrone.ARDrone;

import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 *
 * @author vegard
 */
public class ControlTower {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    private static final long CONNECT_TIMEOUT = 3000;

    public static void main(String[] args) throws IOException, InterruptedException {
        Semaphore sem = new Semaphore(1, true);

        ControllerStateStorage storage = new ControllerStateStorage();


        PS3ControllerReader reader = new PS3ControllerReader(sem, storage);

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

        
        VideoPlayer player = new VideoPlayer(drone);
        DroneControl dc = new DroneControl(drone, sem, storage);
        reader.start();
        dc.start();

    }

}
