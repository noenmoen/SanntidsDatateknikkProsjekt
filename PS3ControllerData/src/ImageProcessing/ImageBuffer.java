/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

import de.yadrone.base.IARDrone;
import de.yadrone.base.video.ImageListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author Martin Strøm Pedersen
 */
public class ImageBuffer implements ImageListener
{

//    private ArrayList<BufferedImage> buffer = null;
    private final int bufferSize;
    private int newest = -1;
    private int oldest = 0;
    private BufferedImage[] buffer;

    public ImageBuffer(final IARDrone drone, int bufferSize)
    {
        this.bufferSize = bufferSize;
//        buffer = new ArrayList<>();
        drone.getVideoManager().addImageListener(this);
        buffer = new BufferedImage[bufferSize];
    }

    @Override
    public synchronized void imageUpdated(BufferedImage bi)
    {
        incrementNewest();
        if (newest == oldest && !(buffer[0]==null)) {
            incrementOldest();
        }
//        System.out.println("newest: " + newest);
        buffer[newest]=bi;
        notify();
    }

    public synchronized BufferedImage getBufferedImage()
    {
//        System.out.println("oldest: " + oldest);
        BufferedImage image = buffer[oldest];
        if (oldest != newest) {
            incrementOldest();
        }
        notify();
        return image;
    }

    private void incrementNewest()
    {
        newest++;
        if (newest >= bufferSize) {
            newest = 0;
        }
    }

    private void incrementOldest()
    {
        oldest++;
        if (oldest >= bufferSize) {
            oldest = 0;
        }
    }
}
