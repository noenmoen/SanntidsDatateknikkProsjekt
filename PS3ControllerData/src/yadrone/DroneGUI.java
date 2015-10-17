/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import ImageProcessing.ProcessedImagePanel;
import de.yadrone.base.IARDrone;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Morten
 */
public class DroneGUI extends javax.swing.JFrame implements Runnable {

    /**
     * Creates new form
     */
    private VideoListener v1;
    private NavDataListener navData;

    private DroneControl cont;
    private IARDrone drone;
    private final ProcessedImagePanel pil;

    public DroneGUI(IARDrone drone, DroneControl cont, ProcessedImagePanel pil) {
        this.drone = drone;
        v1 = new VideoListener(drone);
        navData = new NavDataListener(drone);
        this.cont = cont;
        this.pil = pil;
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
            }

        }
        initComponents();
        this.setVisible(true);
    }

    @Override
    public void run() {
        while (true) {
            rollTextField.setText("Roll: " + navData.getRoll()/1000f);
            pitchTextField.setText("Pitch: " + navData.getPitch()/1000f);
            yawTextField.setText("Yaw: " + navData.getYaw()/1000f);
            altitudeTextField.setText("Altitude: " + navData.getAltitude()/100f);
            batTextField.setText("Battery status : " + navData.getPercentage() + "%");
            repaintTextFields();
        }
    }

    @SuppressWarnings("unchecked")

    private void initComponents() {

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                drone.stop();
                System.exit(0);
            }
        });
        jPanel1 = new javax.swing.JPanel();
        VideoStreamViewer = v1;
        ImageProcessViewer = pil;
        ButtonPanel1 = new javax.swing.JPanel();
        manButton = new javax.swing.JButton();
        autoButton = new javax.swing.JButton();
        modeTextField = new javax.swing.JTextField();
        rollTextField = new javax.swing.JTextField();
        pitchTextField = new javax.swing.JTextField();
        yawTextField = new javax.swing.JTextField();
        altitudeTextField = new javax.swing.JTextField();
        batTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        VideoStreamViewer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout VideoStreamViewerLayout = new javax.swing.GroupLayout(VideoStreamViewer);
        VideoStreamViewer.setLayout(VideoStreamViewerLayout);
        VideoStreamViewerLayout.setHorizontalGroup(
                VideoStreamViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 640, Short.MAX_VALUE)
        );
        VideoStreamViewerLayout.setVerticalGroup(
                VideoStreamViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 360, Short.MAX_VALUE)
        );

        ImageProcessViewer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout ImageProcessViewerLayout = new javax.swing.GroupLayout(ImageProcessViewer);
        ImageProcessViewer.setLayout(ImageProcessViewerLayout);
        ImageProcessViewerLayout.setHorizontalGroup(
                ImageProcessViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 640, Short.MAX_VALUE)
        );
        ImageProcessViewerLayout.setVerticalGroup(
                ImageProcessViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 360, Short.MAX_VALUE)
        );

        ButtonPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        manButton.setText("Manual mode");
        manButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manButtonActionPerformed(evt);
            }
        });

        autoButton.setText("Autonomous mode");
        autoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoButtonActionPerformed(evt);
            }
        });

        modeTextField.setText("Mode: Manual mode");

        rollTextField.setText("Roll:");

        pitchTextField.setText("Pitch:");

        yawTextField.setText("Yaw:");

        altitudeTextField.setText("Altitude:");

        batTextField.setText("Battery Status:");

        javax.swing.GroupLayout ButtonPanel1Layout = new javax.swing.GroupLayout(ButtonPanel1);
        ButtonPanel1.setLayout(ButtonPanel1Layout);
        ButtonPanel1Layout.setHorizontalGroup(
                ButtonPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ButtonPanel1Layout.createSequentialGroup()
                        .addGap(103, 103, 103)
                        .addComponent(modeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(ButtonPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(ButtonPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(yawTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                                .addComponent(pitchTextField)
                                .addComponent(rollTextField))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(ButtonPanel1Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addGroup(ButtonPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(ButtonPanel1Layout.createSequentialGroup()
                                        .addComponent(altitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(batTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(ButtonPanel1Layout.createSequentialGroup()
                                        .addComponent(manButton, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                                        .addComponent(autoButton)))
                        .addGap(33, 33, 33))
        );
        ButtonPanel1Layout.setVerticalGroup(
                ButtonPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ButtonPanel1Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(rollTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(pitchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(yawTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addGroup(ButtonPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(altitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(batTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(modeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addGroup(ButtonPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(manButton)
                                .addComponent(autoButton))
                        .addGap(27, 27, 27))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(ButtonPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(VideoStreamViewer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(ImageProcessViewer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(VideoStreamViewer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ImageProcessViewer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(ButtonPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        VideoStreamViewer.getAccessibleContext().setAccessibleName("");
        ImageProcessViewer.getAccessibleContext().setAccessibleName("ImageProcessViewer");
        ImageProcessViewer.getAccessibleContext().setAccessibleDescription("The Processed Images");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        

    private void manButtonActionPerformed(java.awt.event.ActionEvent evt) {
        modeTextField.setText("Mode: Manual mode");
        modeTextField.repaint();
        cont.setMode(DroneControl.DroneMode.MAN_MODE);
    }

    private void autoButtonActionPerformed(java.awt.event.ActionEvent evt) {
        modeTextField.setText("Mode: Autonomous mode");
        modeTextField.repaint();
        cont.setMode(DroneControl.DroneMode.AUTO_MODE);
    }

    // Variables declaration - do not modify                     
    private javax.swing.JPanel ButtonPanel1;
    private javax.swing.JPanel ImageProcessViewer;
    private javax.swing.JPanel VideoStreamViewer;
    private javax.swing.JButton autoButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField rollTextField;
    private javax.swing.JTextField pitchTextField;
    private javax.swing.JTextField yawTextField;
    private javax.swing.JTextField altitudeTextField;
    private javax.swing.JTextField batTextField;
    private javax.swing.JButton manButton;
    private javax.swing.JTextField modeTextField;
    // End of variables declaration                   

    private void repaintTextFields() {
        rollTextField.repaint();
        pitchTextField.repaint();
        yawTextField.repaint();
        altitudeTextField.repaint();
        batTextField.repaint();

    }

}
