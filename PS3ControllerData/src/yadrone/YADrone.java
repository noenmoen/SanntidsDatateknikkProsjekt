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
 *
 */
public class YADrone
{

    static final int PERIOD = 50;
    static IARDrone drone;
    static PS3ControllerReader reader;
    static Timer timer = new Timer();
    static Semaphore mySem = new Semaphore(1, true);
    static ControllerStateStorage store = new ControllerStateStorage();
    static ProcessedImagePanel pip = new ProcessedImagePanel();
    static DataHandler dh = new DataHandler();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        declarePS3Controller();
        declareDrone();
        DroneControl cont = new DroneControl(drone, mySem, store);
        TimerTask reg = new Regulator(cont, dh, PERIOD);
        cont.setRegulator((Regulator) reg);
        CircleDetection cd = new CircleDetection(
                1000, 30, 4, 3, drone, 3, pip, dh);
        DroneGUI gui = new DroneGUI(drone, cont, pip, reg, cd);
        Thread guiThread = new Thread(gui);

//==============================================================================
// Start threads
//============================================================================== 
        reader.start();
        timer.scheduleAtFixedRate(reg, 0, PERIOD);
        cont.start();
        cd.start();
        guiThread.start();
    }

    private static void declarePS3Controller()
    {
        try {
            reader = new PS3ControllerReader(mySem, store);
        }
        catch (IOException ex) {

        }
    }

    private static void declareDrone()
    {
        try {
            drone = new ARDrone();
            drone.start();
        }
        catch (Exception exc) {
            declareDrone();
            System.out.println("Failed. Trying to reconnect to drone.");
        }
        drone.getCommandManager().setVideoBitrateControl(
                VideoBitRateMode.DISABLED); // Test this        
        drone.getCommandManager().setVideoCodec(
                VideoCodec.H264_360P); // Test this
    }
}
