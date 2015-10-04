/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import java.awt.image.BufferedImage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author Morten
 */
public class ImageProcessing
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
       
        IARDrone drone = null;
        try {
            drone = new ARDrone();
            drone.start();
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
        CircleDetection cd = new CircleDetection(1000, 30, 3, 13, 204, 200, 3, drone, 10);
        cd.start();
    }

}
