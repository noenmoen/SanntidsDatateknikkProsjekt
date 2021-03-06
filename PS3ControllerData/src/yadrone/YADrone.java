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

/**
 *
 * @author vegard/martin/morten
 *
 */
public class YADrone
{

    static final int PERIOD = 10;
    static final boolean IS_RESOLUTION_HIGH = false;
    static IARDrone drone;
    static PS3ControllerReader reader;
    static Timer timer = new Timer();
    static Semaphore mySem = new Semaphore(1, true);
    static ControllerStateStorage store = new ControllerStateStorage();
    static int[] resolution = new int[2];

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        setResolution();
        declarePS3Controller();
        declareDrone();

        DataHandler dh = new DataHandler(resolution);
        ProcessedImagePanel pip = new ProcessedImagePanel(resolution);
        DroneControl cont = new DroneControl(drone, mySem, store);
        TimerTask reg = new Regulator(cont, dh, PERIOD);
        cont.setRegulator((Regulator) reg);
        CircleDetection cd = new CircleDetection(125, drone, pip, dh);
        DroneGUI gui = new DroneGUI(drone, cont, pip, reg, cd, resolution);
        Thread guiThread = new Thread(gui);

//==============================================================================
// Start threads
//============================================================================== 
        reader.start();
        timer.scheduleAtFixedRate(reg, 0, PERIOD);
        cont.start();
        cd.setPriority(10);
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
        drone.getCommandManager().setVideoCodecFps(30);
        drone.getCommandManager().setVideoBitrateControl(
                VideoBitRateMode.DISABLED);   
        if (IS_RESOLUTION_HIGH) {
            drone.getCommandManager().setVideoCodec(VideoCodec.H264_720P);
        }
        else {
            drone.getCommandManager().setVideoCodec(VideoCodec.H264_360P);
        }

    }

    private static void setResolution()
    {
        if (IS_RESOLUTION_HIGH) {
            resolution[0] = 1280;
            resolution[1] = 720;
        }
        else {
            resolution[0] = 640;
            resolution[1] = 360;
        }
    }
}
