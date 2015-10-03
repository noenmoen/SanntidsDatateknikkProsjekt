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
        ImageViewer v = new ImageViewer();
        long a = System.currentTimeMillis();
        String fileName = "" + System.getProperty("user.dir") + "/dronetest00.PNG";   //Bilde av typen BGR
        Mat inImage = Imgcodecs.imread(fileName);
        
//        System.out.println(inImage.channels());
//        ImageConverter ic = new ImageConverter();
//        v.show(inImage);
//        BufferedImage bi = ic.MatToBufferedImage(inImage, ".JPG");
//        v.show(bi, "bufferImage");
//        Mat mi = ic.BufferedImageToMat(bi);
//        System.out.println(mi.depth() + " " + mi.channels() + " " + mi.width() + " " + mi.height());
//        v.show(mi);

            CircleDetection C = new CircleDetection(inImage,1000,70,2);
        long b = System.currentTimeMillis();
        long c = b - a;
        System.out.println("runtime: " + c);

    }

}
