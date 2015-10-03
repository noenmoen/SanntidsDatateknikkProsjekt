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

    private ArrayList<BufferedImage> buffer = null;
    private final int bufferSize;
    int newest = -1;
    int oldest = 0;

    public ImageBuffer(final IARDrone drone, int bufferSize)
    {
        this.bufferSize = bufferSize;
        buffer = new ArrayList<>(bufferSize);
        drone.getVideoManager().addImageListener(this);
    }

    @Override
    public void imageUpdated(BufferedImage bi)
    {
        incrementNewest();
        if (newest == oldest && !buffer.isEmpty()) {
            incrementOldest();
        }
        buffer.add(newest, bi);
    }

    public BufferedImage getBufferedImage()
    {
        BufferedImage image = buffer.get(oldest);
        if (oldest != newest) {
            incrementOldest();
        }
        return image;
    }

    private void incrementNewest()
    {
        newest++;
        if (newest > bufferSize) {
            newest = 0;
        }
    }

    private void incrementOldest()
    {
        oldest++;
        if (oldest > bufferSize) {
            oldest = 0;
        }
    }
}
