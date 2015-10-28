/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import ImageProcessing.CircleDetection;
import ImageProcessing.ProcessedImagePanel;
import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.VideoBitRateMode;
import de.yadrone.base.command.VideoCodec;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

/**
 *
 * @author vegard
 */
public class YADrone
{
    static final float PERIOD = 0.1f;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Timer timer = new Timer();
        Semaphore mySem = new Semaphore(1, true);
        ControllerStateStorage store = new ControllerStateStorage();
        PS3ControllerReader reader = null;
        ProcessedImagePanel pip = new ProcessedImagePanel();
        DataHandler dh = new DataHandler();

        IARDrone drone = null;
        try {
            drone = new ARDrone();
            drone.start();
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
        drone.getCommandManager().setVideoBitrateControl(VideoBitRateMode.DISABLED); // Test this        
        drone.getCommandManager().setVideoCodec(VideoCodec.H264_360P); // Test this
        try {
            reader = new PS3ControllerReader(mySem, store);
        } catch (IOException ex) {
            
        }
        reader.start();
        DroneControl cont = new DroneControl(drone, mySem, store);
        TimerTask reg = new Regulator(cont, dh, PERIOD);
        cont.start();
        timer.scheduleAtFixedRate(reg, 0, (int)(1000*PERIOD));
        CircleDetection cd = new CircleDetection(
                1000, 30, 4, 3, 204, 200, 2, drone, 3, pip, dh);
        DroneGUI gui = new DroneGUI(drone, cont, pip, reg, cd);
        Thread guiThread = new Thread(gui);
        cd.start();
        guiThread.start();
    }
}
