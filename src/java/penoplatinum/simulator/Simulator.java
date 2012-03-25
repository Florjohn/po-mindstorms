package penoplatinum.simulator;

/**
 * Simulator
 * 
 * Accepts a robot, map and view and simulates the how the robot would run
 * this map (in a perfect world). Its main use is to test the theory behind
 * Navigator implementations, without the extra step onto the robot.
 * 
 * Future Improvements: Add support for multiple robots
 * 
 * @author: Team Platinum
 */
import penoplatinum.map.Map;
import penoplatinum.simulator.tiles.TileGeometry;
import penoplatinum.simulator.tiles.Tile;
import penoplatinum.simulator.view.SilentSimulationView;
import penoplatinum.simulator.view.SimulationView;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import penoplatinum.util.Utils;

public class Simulator {
  // the Simulator can run until different goals are reached

  public static final double TIME_SLICE = 0.008;
  // a view to display the simulation, by default it does nothing
  SimulationView view = new SilentSimulationView();
  private Map map;                // the map that the robot will run on
  private List<RobotEntity> robotEntities = new ArrayList<RobotEntity>();
  private HashMap<String, RemoteEntity> remoteEntities = new HashMap<String, RemoteEntity>();
  private RobotEntity pacmanEntity;
  private ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<String>();
  private Runnable stepRunnable;
  private double circleRadius = 5;

  // main constructor, no arguments, Simulator is selfcontained
  public Simulator() {
  }
  
  public void addSimulatedEntity(SimulatedEntity r) {
    robotEntities.add(r);
    view.addRobot(r.getViewRobot());
    r.useSimulator(this);
  }
  
  public RemoteEntity addRemoteEntity(String entityName, int originX, int originY, int originBearing) {
    
    RemoteEntity ent = new RemoteEntity(entityName);
    view.addRobot(ent.getViewRobot());
    
    ent.useSimulator(this);
    
    robotEntities.add(ent);
    remoteEntities.put(entityName, ent);
    ent.setOrigin(originX, originY, originBearing);
    
    return ent;
  }

  /**
   * On a SimulationView, the Simulator can visually show what happens during
   * the simulation.
   */
  public Simulator displayOn(SimulationView view) {
    this.view = view;
    for (RobotEntity s : robotEntities) {
      view.addRobot(s.getViewRobot());
    }
    if (pacmanEntity != null) {
      view.addRobot(pacmanEntity.getViewRobot());
    }
    return this;
  }
  
  public double getAngleToNorthPacman(SimulatedEntity simEntity) {
    double angleToNorth = 0;
    double dx = getXDistanceToPacman(simEntity);
    double dy = getYDistanceToPacman(simEntity);
    if (dy != 0) {
      angleToNorth = Math.atan(dx / dy) / Math.PI * 180;
      if (dy > 0) {
        angleToNorth += 180;
      }
    } else if (dx > 0) {
      angleToNorth = 270;
    } else {
      angleToNorth = 90;
    }
    angleToNorth += 360;
    angleToNorth %= 360;
    return angleToNorth;
  }
  
  public double getYDistanceToPacman(SimulatedEntity simEntity) {
    double dy = getPacMan().getPosY() - simEntity.getPosY();
    return dy;
  }

  /**
   * A map can be provided. This will determine what information the 
   * Simulator will send to the robot's sensors, using the SimulationRobotAPI.
   */
  public Simulator useMap(Map map) {
    this.map = map;
    return this;
  }

  // determine the distance to the first obstacle in direct line of sight 
  // under a given angle
  public int getDistanceToWall(Point tile, Point pos, int angle) {
    
    return this.findHitDistance(angle,
            (int) tile.getX(), (int) tile.getY(),
            (int) pos.getX(), (int) pos.getY());
  }
  
  public int getFreeDistance(Point tile, Point pos, int angle, SimulatedEntity simEntity) {
    int minimum = Math.min(getDistanceToWall(tile, pos, angle), getDistanceToPacman(simEntity));
    return Math.min(minimum, findDistanceToRobot(simEntity,angle));
    
    
  }

  private int findDistanceToRobot(SimulatedEntity simEntity,int angle) {
    
    double minDistanceToEntity = 10000;    
    double x1 = simEntity.getPosX();
    double y1 = simEntity.getPosY();
    double m = Math.tan(Math.toRadians(angle));
    double m2 = Math.pow(m, 2);
    double a = 1 + Math.pow(m, 2);
    for (RobotEntity entity : robotEntities) {
      if(!simEntity.equals(entity)){
      double x0 = entity.getPosX();
      double y0 = entity.getPosY();
      double b = -2 * m2 * x1 + 2 * m * y1 - 2 * x0 - 2 * m * y0;
      double c = Math.pow(y0, 2) + Math.pow(x0, 2) + Math.pow(circleRadius, 2) + 2 * y1 * y0 + 2 * m * x1 * y0 + Math.pow(y1, 2) + 2 * m * y1 + m2 * Math.pow(x1, 2);
      if((Math.pow(b, 2)-4*a*c)>=0){
              double x2 = (Math.pow(b, 2) + Math.sqrt(4 * a * c)) / (2 * a);
      double x3 = (Math.pow(b, 2) - Math.sqrt(4 * a * c)) / (2 * a);
      double y2 = m * (x2 - x1) + y1;
      double y3 = m * (x3 - x2) + y1;
      double distanceToEntity = TileGeometry.getDistance(x2, y2, new Point((int) x1, (int) y1));
      double distanceToEntity2 = TileGeometry.getDistance(x3, y3, new Point((int) x1, (int) y1));
      distanceToEntity = Math.min(distanceToEntity,distanceToEntity2);
      if (minDistanceToEntity > distanceToEntity) {
        minDistanceToEntity = distanceToEntity;
      }
      }
      }
    
    }
//    System.out.println(simEntity.getRobot().getName());
    return (int) minDistanceToEntity;
    
  }
  
  public Tile getCurrentTile(Point tile) {
    return this.map.get((int) tile.getX(), (int) tile.getY());
  }

  /**
   * determines the distance to the first hit wall at the current bearing.
   * if the hit is not on a wall on the current tile, we follow the bearing
   * to the next tile and recursively try to find the hit-distance
   */
  int findHitDistance(int angle, int left, int top, double x, double y) {
    // Force angles between 0 and 360 !!!
    angle = penoplatinum.util.Utils.ClampLooped(angle, 0, 360);
    //if (angle < 0 || angle > 360) throw new IllegalArgumentException();

    // determine the point on the (virtual) wall on the current tile, where
    // the robot would hit at this bearing
    double dist = 0;
    int bearing = 0;
    Tile tile = null;
    Point hit = null;
    do {
      tile = this.map.get(left, top);
      
      if (tile == null) {
        System.out.println(left + " " + top + " " + hit.x + " " + hit.y + " " + angle);
        left++;
      }
      hit = TileGeometry.findHitPoint(x, y, angle, tile.getSize());

      // distance from the starting point to the hit-point on this tile
      dist += TileGeometry.getDistance(x, y, hit);

      // if we don't have a wall on this tile at this bearing, move to the next
      // at the same bearing, starting at the hit point on the tile
      // FIXME: throws OutOfBoundException, because we appear to be moving
      //        through walls.
      bearing = TileGeometry.getHitWall(hit, tile.getSize(), angle);
      /*if(x == hit.x && y == hit.y){
      System.out.println(left+" "+top+" "+angle+" "+angle/45+" "+hit.x+" "+hit.y+" "+x+" "+y);
      int pos = angle/45;
      int[] dLeft = new int[]{0, -1, 1, 0, 0, 1, -1, 0};
      int[] dTop = new int[]{-1, 0, 0, 1, 1, 0, 0, -1};
      left += dLeft[pos];
      top += dTop[pos];
      } else {/**/
      left = left + Bearing.moveLeft(bearing);
      top = top + Bearing.moveTop(bearing);
      //}
      //System.out.println(left + " " + top);
      x = hit.x == 0 ? tile.getSize() : (hit.x == tile.getSize() ? 0 : hit.x);
      y = hit.y == 0 ? tile.getSize() : (hit.y == tile.getSize() ? 0 : hit.y);
      
    } while (!tile.hasWall(bearing));
    return (int) Math.round(dist);
  }

  /**
   * Allows the end-user to send commands through the communication layer
   * to the Robot. In the real world this is done through the RobotAgent,
   * which here is being provided and controlled by the Simulator.
   */
  public Simulator send(String cmd) {
    //robotEntities.robotAgent.receive(cmd);
    return this;
  }

  /**
   * This processes status-feedback from the RobotAgent, extracted from the
   * Model and Navigator.
   */
  public Simulator receive(String status) {
    // this is normally used by the PC client to implement a View of the 
    // Robot's mind.
    return this;
  }
  
  public double getXDistanceToPacman(SimulatedEntity simEntity) {
    double dx = getPacMan().getPosX() - simEntity.getPosX();
    return dx;
  }

  // at the end of a step, refresh the visual representation of our world
  private void refreshView() {
    this.view.updateRobots();
  }

  /**
   * This starts the actual simulation, which will start the robot and the 
   * agent.
   */
  public Simulator run() {
    this.view.showMap(this.map);
    
    
    while (true) {
      this.step();
      if (false) {
        break;
      }
//      Utils.Sleep(3);
    }
    this.view.log("");
    return this;
  }
  
  private void step() {
    for (RobotEntity robotEntity : robotEntities) {
      robotEntity.step();
    }
    if (stepRunnable != null) {
      stepRunnable.run();
    }
    refreshView();

//    Utils.Sleep(20);

  }
  
  boolean hasTile(double positionX, double positionY) {
    int x = (int) positionX / this.getTileSize() + 1;
    int y = (int) positionY / this.getTileSize() + 1;
    return map.exists(x, y);
  }
  
  boolean goesThroughWallX(SimulatedEntity entity, double dx) {
    double positionX = entity.getPosX();
    double positionY = entity.getPosY();
    double LENGTH_ROBOT = entity.LENGTH_ROBOT;
    
    double posXOnTile = positionX % this.getTileSize();
    int tileX = (int) positionX / this.getTileSize() + 1;
    int tileY = (int) positionY / this.getTileSize() + 1;
    return (this.map.get(tileX, tileY).hasWall(Bearing.W)
            && dx < 0 && (posXOnTile + dx < LENGTH_ROBOT))
            || (this.map.get(tileX, tileY).hasWall(Bearing.E)
            && dx > 0 && (posXOnTile + dx > this.getTileSize() - LENGTH_ROBOT));
  }
  
  boolean goesThroughWallY(SimulatedEntity entity, double dy) {
    double positionX = entity.getPosX();
    double positionY = entity.getPosY();
    double LENGTH_ROBOT = entity.LENGTH_ROBOT;
    
    double posYOnTile = positionY % this.getTileSize();
    int tileX = (int) positionX / this.getTileSize() + 1;
    int tileY = (int) positionY / this.getTileSize() + 1;
    
    return (this.map.get(tileX, tileY).hasWall(Bearing.N)
            && dy > 0 && (posYOnTile - dy < LENGTH_ROBOT))
            || (this.map.get(tileX, tileY).hasWall(Bearing.S)
            && dy < 0 && (posYOnTile - dy > this.getTileSize() - LENGTH_ROBOT));
  }
  
  public RobotEntity getPacMan() {
    return this.pacmanEntity;
  }
  
  public void setPacmanEntity(RobotEntity pacmanEntity) {
    this.pacmanEntity = pacmanEntity;
    view.addRobot(pacmanEntity.getViewRobot());
  }
  
  public int getTileSize() {
    return map.getFirst().getSize();
  }
  
  public void useStepRunnable(Runnable runnable) {
    stepRunnable = runnable;
  }
  
  public int getDistanceToPacman(SimulatedEntity simEntity) {
    // calculates the angles and distances needed
    RobotEntity pacman = getPacMan();
    if (pacman == null) {
      return 0;
    }
    double dx = getXDistanceToPacman(simEntity);
    double dy = getYDistanceToPacman(simEntity);
    int distanceToPacman = (int) Math.sqrt(dx * dx + dy * dy);
    return distanceToPacman;
  }

  public double getRelativeAnglePacman(SimulatedEntity simEntity) {
    double angleToNorth = getAngleToNorthPacman(simEntity);
    return (angleToNorth - simEntity.getDir() + 360) % 360;
  }
}
