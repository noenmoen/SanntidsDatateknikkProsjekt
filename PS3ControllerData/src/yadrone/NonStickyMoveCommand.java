/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

import de.yadrone.base.command.PCMDCommand;

/**
 *
 * @author vegard
 * Copy of class MoveCommand in Yadrone library,
 * but non-sticky type, to avoid unnecessary command sending.
 */
public class NonStickyMoveCommand extends PCMDCommand {
	public NonStickyMoveCommand(boolean combined_yaw_enabled, float left_right_tilt, float front_back_tilt,
			float vertical_speed, float angular_speed) {
		super(false, combined_yaw_enabled, left_right_tilt, front_back_tilt, vertical_speed, angular_speed);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yadrone.base.command.DroneCommand#isSticky()
	 */
	@Override
	public boolean isSticky() {
		return false;
	}

}