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
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author vegard Class for receiving the BufferedImage from the videostream
 * Will update the image in the GUI
 */
public class VideoListener extends JPanel implements ImageListener {

    private BufferedImage image = null;

    public VideoListener(final IARDrone drone,int[] resolution) {
        
        setSize(resolution[0], resolution[1]);
        setVisible(true);

        drone.getVideoManager().addImageListener(this);
        
       // Switch video input to other camera with mouseclick on the image in the GUI
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                drone.getCommandManager().setVideoChannel(VideoChannel.NEXT);
            }
        });

    }

    // Repaint the panel
    @Override
    public void paint(Graphics g) {
        if (image != null) {
            g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        }
    }
    // Gets called when a new image is received
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

    public BufferedImage getImage()
    {
        return image;
    }

}
