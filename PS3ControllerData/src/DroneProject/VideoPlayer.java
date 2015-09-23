/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DroneProject;

//import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.DroneStatusChangeListener;
import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;
//import com.codeminders.ardrone.VideoDataDecoder;
import com.twilight.h264.player.PlayerFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author vegard
 */
public class VideoPlayer implements  DroneVideoListener, NavDataListener, DroneStatusChangeListener {
    private PlayerFrame displayPanel;
    private final ARDrone drone;
    private JLabel batteryStatus;
    
    public VideoPlayer(ARDrone dr) {
        drone = dr;
        drone.addImageListener(this);
        JFrame frame = new JFrame("videoplayer");
        batteryStatus = new javax.swing.JLabel();
        displayPanel = new PlayerFrame();
        
        frame.getContentPane().add(batteryStatus, BorderLayout.SOUTH);
        frame.getContentPane().add(displayPanel, BorderLayout.CENTER);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            displayPanel.setVisible(true);
            frame.pack();
            frame.setVisible(true);
            frame.setSize(645, 380);
            
    }


    @Override
    public void frameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        image.setRGB(startX, startY, w, h, rgbArray, offset, scansize);
        displayPanel.lastFrame = image;
        displayPanel.invalidate();
        displayPanel.updateUI(); 
    }

    @Override
    public void frameReceived(BufferedImage bi) {
        displayPanel.lastFrame = bi;
        displayPanel.invalidate();
        displayPanel.updateUI();
    }

    @Override
    public void navDataReceived(NavData nd) {
        updateBatteryStatus(nd.getBattery());
    }
    private void updateBatteryStatus(int value)     {
        java.awt.EventQueue.invokeLater(() -> {
            batteryStatus.setText(value + "%");
            if(value < 15)
            {
                batteryStatus.setForeground(Color.RED);
            }
            else if(value < 50)
            {
                batteryStatus.setForeground(Color.ORANGE);
            }
            else
            {
                batteryStatus.setForeground(Color.GREEN);
            }
        });
    }

    @Override
    public void ready() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
