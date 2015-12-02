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
 * @author vegard Class for receiving navigational data from the drone
 */
public class NavDataListener {

    private float pitch;
    private float roll;

    private float yaw;
    private int percentage;
    private int altitude;
    private Altitude extAltitude;
    
    // Setters and getters for the variables
    public synchronized Altitude getExtAltitude() {
        return extAltitude;
    }

    public synchronized void setExtAltitude(Altitude extAltitude) {
        this.extAltitude = extAltitude;
    }

    public synchronized void setRollPitchYaw(float roll, float pitch, float yaw) {
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public synchronized float getPitch() {
        return pitch/1000f;
    }

    public synchronized float getRoll() {
        return roll/1000f;
    }

    public synchronized float getYaw() {
        return yaw/1000f;
    }

    public synchronized int getPercentage() {
        return percentage;
    }

    public synchronized void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public synchronized int getAltitude() {
        return altitude/100;
    }

    public synchronized void setAltitude(int altitude) {
        this.altitude = altitude;
    }
    
    
    public NavDataListener(IARDrone drone) {
        // Add attitudelistener in the navdatamanager
        drone.getNavDataManager().addAttitudeListener(new AttitudeListener() {
            // Receive the attitude of the drone
            public void attitudeUpdated(float pitch, float roll, float yaw) {
                setRollPitchYaw(roll, pitch, yaw);
            }

            public void attitudeUpdated(float pitch, float roll) {
            }

            public void windCompensation(float pitch, float roll) {
            }
        });
        // Add batterylistener in the navdatamanager
        drone.getNavDataManager().addBatteryListener(new BatteryListener() {
            // Receive the battery level of the drone
            public void batteryLevelChanged(int percentage) {

                setPercentage(percentage);
            }

            public void voltageChanged(int vbat_raw) {
            }
        });
        // Add altitudelistener in the navdatamanager
        drone.getNavDataManager().addAltitudeListener(new AltitudeListener() {
            // receive the altitude from the drone
            public void receivedAltitude(int altitude) {
                setAltitude(altitude);
            }

            public void receivedExtendedAltitude(Altitude a) {
                setExtAltitude(a);
            }
        });
    }
}
