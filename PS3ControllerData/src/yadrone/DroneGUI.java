/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;


import de.yadrone.base.IARDrone;
import javax.swing.JFrame;

/**
 *
 * @author vegard
 */
public class DroneGUI extends Thread{
    
    private JFrame videoPanel;
    
    public DroneGUI(final IARDrone drone) {
        
        videoPanel = new VideoListener(drone);
        System.out.println("In dronegui constructor");
    }
    @Override
    public void run (){
 
    }
}
