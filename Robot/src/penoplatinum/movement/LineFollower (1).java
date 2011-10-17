package PenoPlatinum.movement;

import lejos.nxt.*;
import lejos.subsumption.*;
import lejos.navigation.*;

/**
 * LineFollower.java for NXT version that follows a black line on a white board.
 *  
 * 
 * Previous teams' code were examined and adapted for the new NXT platform, which
 * uses a subsumption architecture. The subsumption architecture uses an array
 * of Behaviors that activate under certain conditions. The Behavior will run
 * certain actions, determined by the program, while the activation condition is true,
 * and run more actions specific to after the activation condition becomes false.
 *
 * <B>Our Attempts</b>
 * We had issues with grasping the subsumption architecture. At first we started with
 * two behaviors, one for when the center sensor was on the line and one for when it 
 * was not. When the sensor was on the line the robot moved forward. The other behavior 
 * performed different actions depending on which light sensors read black.
 *
 * We were unable to successfully implement this system so we tried creating a behavior 
 * for each of the separate light combinations. This came to a total of eight behaviors
 * which was probably the wrong direction to take since we could not successfully make
 * it work. 
 *
 * To improve this implementation we moved each case into a single behavior called the 
 * StupidRobot behavior. It ran continuously checking each case in order. This worked 
 * relatively well and with more time we could probably separate this into multiple
 * behaviors. 
 *
 *<b>Random Issues</b>
 * The only issues we encountered once we completed the LineFollwer was that the light
 * sensors were inconsitent with their reading and certain cases would trigger when
 * they shouldn't. This generally occurred with the right light sensor. This would read
 * black when the background was right, causing the robot to turn around completely.
 * Ways that we could handle this would be change the positions of the sensor. We could 
 * also try to catch these errors in the code and have the robot fix itself.
 *
 * @author CS371 2008
 * @author <a href="mailto:dickal02@gettysburg.edu">Alex Dick</a>
 * @author <a href="mailto:johnad01@gettysburg.edu">Adrian Johnson</a>
 * @author <a href="mailto:hollmi01@gettysburg.edu">Mike Hollander</a>
 **/


public class LineFollower
{
    //Variable RTurn used to tell robot which direction to turn first when off the Line
    private static boolean RTurn = false;

    public static void main (String[] aArg) throws Exception
    {
	final LightSensor leftLight = new LightSensor(SensorPort.S4,true);
	final LightSensor midLight = new LightSensor(SensorPort.S3,true);
	final LightSensor rightLight = new LightSensor(SensorPort.S2,true);
	//Motor.B is the motor for the right wheel
	Motor.B.setSpeed(50);
	//Motor.C is the motor for the right wheel
	Motor.C.setSpeed(50);
	//Motor.A is for... added visual benefits, aka badass points
	//We added, for fun, a third motor that always ran.
	//Claws were attached to the motor.
	//This served no benefit to the completion of the assignment.
	Motor.A.setSpeed(200); 


	Behavior StupidRobot = new Behavior()
	    {
		public boolean takeControl()
		{
		    return true;
		}
		public void action()
		{
		    int L;
		    int C;
		    int R;
		    while (true){
			L = leftLight.readValue();
			C = midLight.readValue();
			R = rightLight.readValue();
			
			Motor.A.forward();
			LCD.clear();

			// only Center Light reading black: goes forward until that changes
			if (L > 30 && C <= 30 && R > 30){
			    //gets new reading
			    C = midLight.readValue();
			    L = leftLight.readValue();
			    R = rightLight.readValue();

			    //if the Left Light reads black
			    //set Rturn to false, mean the robot will turn left first
			    if (L <= 30)
				RTurn = false;
			    //if the Right Light reads black
			    //set Rturn to false, mean the robot will turn right first
			    if (R <=30)
				RTurn = true;
			    LCD.drawString("CENTER", 0,1);
			    Motor.B.forward();
			    Motor.C.forward();
			}
		    
			// All lights reading white: finds the line
                    else if (L > 30 && C > 30 && R > 30){
			    LCD.drawString("WHITE", 0,1);

			    // iterating loops that cause the robot to change the direction.
			    // continues to turn until the Center Light reads black
			    // first loop should cover over 90 degrees and find up to a sharp turn
			    int i = 0;
			    while(C > 30 && L > 30 && R > 30 && i < 2500){
				//gets new light readings every iteration
				C = midLight.readValue();
				L = leftLight.readValue();
				R = rightLight.readValue();
				
				//checks for new RTurn values
				if (L <= 30)
				    RTurn = false;
				if (R <=30)
				    RTurn = true;

				//if RTurn is true, start turning right
				//otherwise, start turning left
				if (RTurn){
				    Motor.B.backward();
				    Motor.C.forward();
				}
				else{
				    Motor.B.forward();
				    Motor.C.backward();
				}

				i++;
			    }
			    //second iterative loop for opposite direction
			    // should cover 180+ degrees
			    i = 0;
			    while(C > 30 && L > 30 && R > 30 && i < 4000){
				C = midLight.readValue();
				L = leftLight.readValue();
				R = rightLight.readValue();

				// if RTurn was true before, now turn left
				// otherwise, now turn right
				if (RTurn){
				    Motor.B.forward();
				    Motor.C.backward();
				}
				else{
				    Motor.B.backward();
				    Motor.C.forward();
				}

				i++;
			    }
			}
			// Only the Right Light reads black: turn right
			else if(L > 30 && C > 30 && R <= 30){
			    LCD.drawString("RIGHT: " + R, 0,1);
			    //set RTurn to true, so when all white, the robot will turn right first
			    RTurn = true;
			    // continue turning until Center Light reads black
			    while (C > 30){
				C = midLight.readValue();
				Motor.B.backward();
				Motor.C.forward();
			    }
			}
			// Only the Left Light reads black: turn left
			else if(L <= 30 && C > 30 && R > 30){
			    LCD.drawString("LEFT", 0,1);
			    // set RTurn to false, so when all white, the robot will turn left first
			    RTurn = false;
			    // continue turning until Center Light reads black
			    while (C > 30){
				C = midLight.readValue();
				Motor.B.forward();
				Motor.C.backward();
			    }
			    
			}
			// Both Left and Center Lights read black: funtions the same as Left only.
			// Usually will occur at a corner
			else if(L > 30 && C <= 30 && R <= 30){
			    LCD.drawString("LEFT-CENTER", 0,1);
			    RTurn = false;
			    while (C > 30){
				C = midLight.readValue();
				Motor.B.forward();
				Motor.C.backward();
			    }
			}
			// Both Right and Center Lights read black: functions the same as right only
			// Usually will occur at a corner
			else if (L > 30 && C <= 30 && R <= 30){
			    LCD.drawString("RIGHT-CENTER", 0,1);
			    RTurn = true;
			    while (C > 30){
				C = midLight.readValue();
				L = leftLight.readValue();
				R = rightLight.readValue();
				Motor.B.backward();
				Motor.C.forward();
			    }
			}
			// If Left, Right and Center all read black: go straight
			// Likely means the robot is at an intersection.
			else if(L <= 30 && C <= 30 && R <= 30) {
			    LCD.drawString("BLACK", 0,1);
			    Motor.B.forward();
			    Motor.C.forward();
			}
			// Last Case:
			// Left and Right Lights read black
			// The robot is either at fork or approaching a corner at a bad angle
			// The case should not occur while only following a line
			else if (L <= 30 && C > 30 && R <= 30){
			    LCD.drawString("LEFT_RIGHT", 0,1);
			    // continue turning until Center Light reads black
			    // turns based on RTurn: the last side light that read black
			    while (C > 30){
				C = midLight.readValue();
				if (RTurn){    
				    Motor.B.backward();
				    Motor.C.forward();
				}
				else{
				    Motor.B.forward();
				    Motor.C.backward();   
				}
			    }
			}
		    }
		}
		
		public void suppress()
		{
		    LCD.clear();
		}
		
	    };

	// Makes the robot follow certain behaviors
	Button.ENTER.waitForPressAndRelease();
	Behavior[] bArray = {StupidRobot};
	(new Arbitrator(bArray)).start();
    }
}
    // LineFollower