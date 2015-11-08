/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Martin Strøm Pedersen
 */
public class ProcessedImagePanel extends JPanel
{
    private BufferedImage bufferedImage = null;

    /**
     * Constructor. Sets panel size and visibility
     */
    public ProcessedImagePanel()
    {
        setSize(640, 360);
        setVisible(true);
    }

    @Override
    public void paint(Graphics g)
    {
        if (bufferedImage != null) {
            g.drawImage(bufferedImage, 0, 0, bufferedImage.getWidth(),
                    bufferedImage.getHeight(), null);
        }
    }

    /**
     * Updates and repaints image panel
     * @param bufferedImage input image
     */
    public synchronized void setBufferedImage(BufferedImage bufferedImage)
    {
        this.bufferedImage = bufferedImage;
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                repaint();
            }
        });
    }

}
