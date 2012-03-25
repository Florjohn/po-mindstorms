package penoplatinum.simulator.sensors;

import penoplatinum.simulator.RobotEntity;
import penoplatinum.simulator.Sensor;
import penoplatinum.simulator.SimulatedEntity;
import penoplatinum.simulator.Simulator;

/**
 *
 * IRSensor
 *
 * implementatie van de IRSensor voor de simulator, om pacman te herkennen.
 *
 * @author: Team Platinum
 */
public class IRSensor implements Sensor {

  Simulator sim;
  SimulatedEntity simEntity;

  @Override
  public int getValue() {

    // calculates the angles and distances needed
    RobotEntity pacman = sim.getPacMan();
    if (pacman == null) {
      return 0;
    }
    double dx = sim.getXDistanceToPacman(simEntity);
    double dy = sim.getYDistanceToPacman(simEntity);
    double actualAngle = sim.getRelativeAnglePacman(simEntity);
    final int changedOriginVector = (int) (sim.getAngleToNorthPacman(simEntity) + 90) % 360;
    int distanceToWall = sim.getFreeDistance(simEntity.getCurrentTileCoordinates(), simEntity.getCurrentOnTileCoordinates(), changedOriginVector);
    int distanceToPacman = sim.getDistanceToPacman(simEntity);

    // checks if the distance to the pacman is higher than 5 meter
    if (distanceToPacman > 500) {
      return 0;
    }

    // Checks if there is a wall in the way that prevents vision of the pacman
    if (distanceToWall <= distanceToPacman) {
      return 0;
    }
    int angle = (int) ((-actualAngle + 165 + 360) % 360 / 30);
    if (angle > 9) {
      return 0;
    }

    return angle;
  }

  @Override
  public void useSimulator(Simulator sim) {
    this.sim = sim;
  }

  @Override
  public void useSimulatedEntity(SimulatedEntity simEntity) {
    this.simEntity = simEntity;
  }
}