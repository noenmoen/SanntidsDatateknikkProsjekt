/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import de.yadrone.base.IARDrone;
import de.yadrone.base.navdata.Altitude;
import de.yadrone.base.navdata.AltitudeListener;
import de.yadrone.base.navdata.AttitudeListener;
import de.yadrone.base.navdata.BatteryListener;

/**
 *
 * @author vegard
 */
public class NavDataListener {
    public NavDataListener(IARDrone drone) {
        drone.getNavDataManager().addAttitudeListener(new AttitudeListener() {
			
			public void attitudeUpdated(float pitch, float roll, float yaw)
			{
		    	System.out.println("Pitch: " + pitch + " Roll: " + roll + " Yaw: " + yaw);
			}

			public void attitudeUpdated(float pitch, float roll) { }
			public void windCompensation(float pitch, float roll) { }
		});
		
		drone.getNavDataManager().addBatteryListener(new BatteryListener() {
			
			public void batteryLevelChanged(int percentage)
			{
				System.out.println("Battery: " + percentage + " %");
			}
			
			public void voltageChanged(int vbat_raw) { }
		});
                drone.getNavDataManager().addAltitudeListener(new AltitudeListener() {
                    public void receivedAltitude(int altitude)
                    {
                        System.out.println("Altitude: " + altitude);
                    }
                    public void receivedExtendedAltitude(Altitude a) {}
                });
	}
}
