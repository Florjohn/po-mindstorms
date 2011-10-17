/*
 *
 */
package PenoPlatinum.movement;

import java.io.*;
import lejos.nxt.*;
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
            lastLightValue = light.readNormalizedValue();
               if (checkNotOnLine(black)) {
                   int rotate = 5; 
                   int whileCounter=0;
                   while ( checkNotOnLine(black)&&whileCounter!=9 ) {
                        agent.TurnOnSpotCCW(rotate);
                        lastLightValue = light.readNormalizedValue();
                        rotate *=-2;
                        whileCounter++;
                        if(whileCounter==4){
                            agent.TurnOnSpotCCW(-60);
                            rotate=-5;
                        }
                        
                    }
                } else {
                    agent.MoveStraight(100);
                }


        }
    }

    private boolean checkNotOnLine(boolean black) {
        LCD.drawInt(lastLightValue, 0, 0);
        if(black){
            return lastLightValue > 40;
        }
        else{
            return lastLightValue< 60;
                  
        }
    }

    public boolean LineDeterminer() {
        // determines if the Line is white or black
        if (LineThresHold < platformThresHold) {
            light.setLow(LineThresHold);
            light.setHigh(platformThresHold);
            return false;
        }
        light.setLow(platformThresHold);
        light.setHigh(LineThresHold);
        return true;
    }
    
}
