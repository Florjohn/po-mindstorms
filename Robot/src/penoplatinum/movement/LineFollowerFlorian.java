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
        if(LineDeterminer()){
           light.setLow(LineThresHold);
        }
        else{
            light.setHigh(LineThresHold);
        }

    }

    public void ActionLineFollower() {
        CalibrateLightSensor();
        while (true) {
            lastLightValue = light.readNormalizedValue();
               if (checkNotOnLine(LineDeterminer())) {
                   LineFinder();
                } else {
                    agent.MoveStraight(100);
                }


        }
    }

    private boolean checkNotOnLine(boolean black) {
        LCD.drawInt(lastLightValue, 0, 0);
        if(black){
            return lastLightValue > 30 && checkOnPlatform();
        }
        else {
            return lastLightValue< 70 && checkOnPlatform();
                  
        }
    }
    
    private boolean checkOnPlatform(){  
        return (lastLightValue<platformThresHold*1.3 || lastLightValue>platformThresHold*0.7);      
    }

    public boolean LineDeterminer() {
        // determines if the Line is white or black
      return (LineThresHold < platformThresHold);
    }
    
    public void LineFinder(){
        boolean black = LineDeterminer();
        int rotate = 5;
        while (checkNotOnLine(black)) {
            agent.TurnOnSpotCCW(rotate);
            lastLightValue = light.readNormalizedValue();
            rotate *= -1.1;
        }
    }
    
}
