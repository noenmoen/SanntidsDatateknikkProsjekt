/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import de.yadrone.base.IARDrone;
import de.yadrone.base.command.VideoChannel;
import de.yadrone.base.video.ImageListener;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author vegard
 */
public class VideoListener extends JFrame implements ImageListener {

    private BufferedImage image = null;

    public VideoListener(final IARDrone drone) {
        super("YADrone Video");
        setSize(1280, 720);
        setVisible(true);

        drone.getVideoManager().addImageListener(this);
        System.out.println("In VideoListener: Added imagelistener");
       
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                drone.getCommandManager().setVideoChannel(VideoChannel.NEXT);
            }
        });

        // close the 
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                drone.stop();
                System.exit(0);
            }
        });

    }

    @Override
    public void paint(Graphics g) {
        if (image != null) {
            g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        }
    }

    @Override
    public void imageUpdated(BufferedImage newImage) {
        
        image = newImage;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                repaint();
            }
        });

    }
    public BufferedImage getDroneImage() {
        return image;
    }
    public boolean hasDroneImage() {
        return image != null;
    }

}
