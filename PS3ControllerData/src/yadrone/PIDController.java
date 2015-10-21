/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yadrone;

/**
 * A class for running the PID-algorithm
 * By setting the integral gain and/or derivative gain = 0,
 * the controller can be either PD or PI
 * @author vegard
 * 
 */
public class PIDController {

    private float kp;
    private float ki;
    private float kd;
    private float input;
    private float maxOutp;
    private float minOutp;
    private float maxInp;
    private float minInp;
    private boolean continuous; // do the endpoints wrap around? (abs encoder, yaw angle...)
    private float prevError;
    private float totError;
    private float tolerance;
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

    private void calculate() {
        error = setPoint - input;

        // If continuous is set to true allow wrap around
        if (continuous) {
            if (Math.abs(error)
                    > (maxInp - minInp) / 2) {
                if (error > 0) {
                    error = error - maxInp + minInp;
                } else {
                    error = error
                            + maxInp - minInp;
                }
            }
        }
        /* Integrate the errors as long as the upcoming integrator does
         not exceed the minimum and maximum output thresholds */
        if ((totError + error*cycleTime) * ki < maxOutp
                && (totError + error*cycleTime) * ki > minOutp) {
            totError += error*cycleTime;
        }
        // Perform the PID calculations
        output = kp * error + ki * totError + kd * ((error - prevError) / cycleTime);
        if (output > maxOutp) {
            output = maxOutp;
        } else if (output < minOutp) {
            output = minOutp;
        }

    }

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

    public float runPID() {
        calculate();
        return output;
    }

    public void setContinuous(boolean cont) {
        continuous = cont;
    }

    public void setContinuous() {
        this.setContinuous(true);
    }

    public void setInputRange(float minInput, float maxInput) {
        minInp = minInput;
        maxInp = maxInput;
    }
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
     * Returns the current difference of the input from the setpoint
     *
     * @return the current error
     */
    public synchronized float getError() {
        return error;
    }
    
    public void setInput(float input) {
        this.input = input; 
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
