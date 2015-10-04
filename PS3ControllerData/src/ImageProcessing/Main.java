/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

import java.awt.image.BufferedImage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author Morten
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ImageViewer iv = new ImageViewer();
        long a = System.currentTimeMillis();
        String fileName = "" + System.getProperty("user.dir") + "/dronetest00.PNG";   //Bilde av typen BGR
        Mat inImage = Imgcodecs.imread(fileName);
        TestCircleDetection cd = new TestCircleDetection(
                inImage, 2000, 50, 4, 25,204,200,1);
        
        long b = System.currentTimeMillis();
        long c = b - a;
        System.out.println("runtime: " + c);

    }

}
