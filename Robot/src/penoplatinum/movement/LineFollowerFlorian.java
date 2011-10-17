/*
 *
 */
package PenoPlatinum.movement;

import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import penoplatinum.movement.RotationMovement;

/**
 * This class is meant to be used as a controller for line following. It is based upon the fact we can use a light intensity sensor
 * 
 * @author Florian
 */
public class LineFollowerFlorian {

    final LightSensor light = new LightSensor(SensorPort.S1, true);
    final RotationMovement agent = new RotationMovement();
    int LineThresHold;
    int platformThresHold;
    private int lastLightValue;

    public LineFollowerFlorian() {
    }

    public void CalibrateLightSensor() {
        //calibrating Line vs Platform  

        LineThresHold = light.readValue();
        agent.TurnOnSpotCCW(20);
        platformThresHold = light.readValue();
        agent.TurnOnSpotCCW(-40);
        platformThresHold = (platformThresHold + light.readValue()) / 2;
        agent.TurnOnSpotCCW(20);

    }

    public void ActionLineFollower() {
        CalibrateLightSensor();
        while (true) {
            boolean black = LineDeterminer();
            lastLightValue = light.readValue();
                if (checkOnLine(black,0.2f)) {
                    while ( checkOnLine(black,0.10f)) {
                        agent.TurnOnSpotCCW(10);
                        lastLightValue = light.readValue();
                        if (checkOnLine(black, 0.15f)) {
                            agent.TurnOnSpotCCW(-15);
                        }
                        lastLightValue= light.readValue();
                    }
                } else {
                    agent.MoveStraight(100);
                }


        }
    }

    private boolean checkOnLine(boolean black, float threshold) {
        
        if(black){
            return lastLightValue > LineThresHold*threshold;
        }
        else{
            return lastLightValue< LineThresHold*(1-threshold);
                  
        }
    }

    public boolean LineDeterminer() {
        // determines if the Line is white or black
        if (LineThresHold < platformThresHold) {
            return false;
        }
        return true;
    }
    
}
