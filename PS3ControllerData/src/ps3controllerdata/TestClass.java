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
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;
import java.io.IOException;

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
    public static void main(String[] args) throws IOException {

        try {
            ClassPathLibraryLoader.loadNativeHIDLibrary();
            HIDManager dManager = HIDManager.getInstance();
            HIDDeviceInfo[] info = dManager.listDevices();
            for (HIDDeviceInfo hIDDeviceInfo : info) {
                //System.out.println(hIDDeviceInfo.getProduct_string() + " Vendor id: " + hIDDeviceInfo.getVendor_id() + " product id: " + hIDDeviceInfo.getProduct_id() + " ");
            }
        } catch (IOException ex) {
            //Logger.getLogger(formLiveReload.class.getName()).log(Level.SEVERE, null, ex);
        }
        Controller c = HIDControllerFinder.findController();
        //System.out.println(c.getProductString());
        PS3ControllerRead reader = new PS3ControllerRead(c);
        reader.start();
        ARDrone drone = null;
        try {
            drone = new ARDrone();
            drone.connect();
            drone.clearEmergencySignal();

            // Wait until drone is ready
            drone.waitForReady(CONNECT_TIMEOUT);
        }
        catch(Throwable e) {
            System.out.println("Initializing drone failed.");
        }
        DroneControl dc = new DroneControl(drone, reader);
        dc.start();
        

    }

}
