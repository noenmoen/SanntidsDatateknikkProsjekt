/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import de.yadrone.base.IARDrone;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 *
 * @author vegard
 */
public class DroneGUI extends Thread {

    private VideoListener videoPanel;
    private BufferedImage image;
    private int i;

    public DroneGUI(final IARDrone drone) {
        i = 0;
        videoPanel = new VideoListener(drone);
    }

    @Override
    public void run() {
        // Saving 3 images to disk for testing
        /*
        while (true) {
            while ((i < 3) && videoPanel.hasDroneImage()) {
                try {
                    image = videoPanel.getDroneImage();
                    File outputfile = new File("C://Users//vegard//Pictures/drone/dronetestangle" + i + ".png");
                    ImageIO.write(image, "png", outputfile);
                } catch (IOException e) {
                    System.out.println("Image not saved :(");
                }
                i++;
            }
        }*/
    }
}
