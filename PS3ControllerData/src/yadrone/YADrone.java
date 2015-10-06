/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import ImageProcessing.CircleDetection;
import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.VideoBitRateMode;
import de.yadrone.base.command.VideoCodec;
import java.io.IOException;
import java.util.Timer;
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
        Timer timer = new Timer();
        Semaphore mySem = new Semaphore(1, true);
        ControllerStateStorage store = new ControllerStateStorage();
        PS3ControllerReader reader = null;
        IARDrone drone = null;
        try {
            drone = new ARDrone();
            drone.start();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        
        drone.getCommandManager().setVideoBitrateControl(VideoBitRateMode.DISABLED); // Test this        
        drone.getCommandManager().setVideoCodec(VideoCodec.H264_720P); // Test this
        drone.getCommandManager().setVideoBitrate(4000); // Test (max bitrate)
        
        CircleDetection cd = new CircleDetection(1000, 30, 3, 13, 204, 200, 3, drone, 3);
        cd.start();
        DroneGUI gui = new DroneGUI(drone);
        gui.start();
        
        try {
            reader = new PS3ControllerReader(mySem, store);
        } catch (IOException ex) {
            Logger.getLogger(YADrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        timer.scheduleAtFixedRate(reader, 0, 5);
        DroneControl cont = new DroneControl(drone, mySem, store);
        timer.scheduleAtFixedRate(cont, 0, 5);
        
    }
}
