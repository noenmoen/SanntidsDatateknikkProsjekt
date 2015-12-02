/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

/**
 * A class for running the PID-algorithm By setting the integral gain and/or
 * derivative gain = 0, the controller can be either PD or PI (or P)
 *
 * @author vegard
 *
 */
public class PIDController {

    private float kp;
    private float ki;
    private float kd;
    private float reference;
    private float maxOutp;
    private float minOutp;
    private float maxInp;
    private float minInp;
    private boolean continuous; // do the endpoints wrap around? (abs encoder, yaw angle...)
    private float prevError;
    private float totError;
    private float tolerance = 0.2f;
    private float setPoint;
    private float error;
    private float output;
    private float cycleTime;

    public PIDController(float kp, float ki, float kd, float cycleTime) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.cycleTime = cycleTime;
    }
    /*
     The PID-algorithm that calculates the control input
     based on the error, accumulated errors, and upcoming error (derivative)
     */

    private void calculate() {
        error = setPoint - reference;

        // If continuous is set to true allow wrap around
        if (continuous) {
            if (Math.abs(error)
                    > (maxInp - minInp) / 2) {
                if (error > 0) {
                    error = error - maxInp + minInp;
                } else {
                    error = error + maxInp - minInp;
                }
            }
        }
        /* Integrate the errors as long as the upcoming integrator does
         not exceed the minimum and maximum output thresholds */
        if ((totError + error * cycleTime) * ki < maxOutp
                && (totError + error * cycleTime) * ki > minOutp) {
            totError += error * cycleTime;
        }
        // Perform the PID calculations
        output = kp * error + ki * totError + kd * ((error - prevError) / cycleTime);
        if (output > maxOutp) {
            output = maxOutp;
        } else if (output < minOutp) {
            output = minOutp;
        }
        prevError = error;
    }
    /*
     Setters and getters for the gains
     */

    public float getKp() {
        return kp;
    }

    public void setKp(float kp) {
        this.kp = kp;
    }

    public float getKi() {
        return ki;
    }

    public void setKi(float ki) {
        this.ki = ki;
    }

    public float getKd() {
        return kd;
    }

    public void setKd(float kd) {
        this.kd = kd;
    }

    /*
     Method that is called by the regulator class.
     Performs the PID algorithm, and returns the control input to the drone.
     */
    public float runPID() {
        calculate();
        if (onTarget()) {
            return 0.0f;
        }
        return output;
    }

    /**
     * Set the PID controller to consider the input to be continuous, Rather
     * then using the max and min in as constraints, it considers them to be the
     * same point and automatically calculates the shortest route to the
     * setpoint.
     *
     * @param continuous Set to true turns on continuous, false turns off
     * continuous
     */
    public void setContinuous(boolean cont) {
        continuous = cont;
    }

    /**
     * Set the PID controller to consider the input to be continuous, Rather
     * then using the max and min in as constraints, it considers them to be the
     * same point and automatically calculates the shortest route to the
     * setpoint.
     */
    public void setContinuous() {
        this.setContinuous(true);
    }
    /*
     Sets the maximum allowed input range, used for inputs that wrap around
     */

    public void setInputRange(float minInput, float maxInput) {
        minInp = minInput;
        maxInp = maxInput;
    }
    /*
     Set the max allowed output range
     */

    public void setOutputRange(float minOutput, float maxOutput) {
        maxOutp = maxOutput;
        minOutp = minOutput;
    }

    /**
     * Set the setpoint for the PIDController
     *
     * @param setpoint the desired setpoint
     */
    public void setSetpoint(float setpoint) {
        if (maxInp > minInp) {
            if (setpoint > maxInp) {
                setpoint = maxInp;
            } else if (setpoint < minInp) {
                setpoint = minInp;
            } else {
                setPoint = setpoint;
            }
        } else {
            setPoint = setpoint;
        }
    }

    /**
     * Return true if the error is within a preset tolerance of the max and min
     * input range. Only applicable for the yaw axis, since this has a well
     * defined input range.
     *
     * @return true if the error is less than the tolerance
     */
    public boolean onTarget() {
        if (continuous) {
            return (Math.abs(error) < tolerance / 100
                    * (maxInp - minInp));
        } else {
            return false;
        }
    }

    // Set the reference value for the PID algorithm
    public void setReference(float input) {
        this.reference = input;
    }

    /**
     * Reset the previous error and the integral term.
     */
    public void reset() {
        prevError = 0;
        totError = 0;
        output = 0;
    }

}
