package penoplatinum.fullTests;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import junit.framework.TestCase;
import org.junit.Test;

import penoplatinum.Config;

import penoplatinum.driver.Driver;
import penoplatinum.driver.ManhattanDriver;
import penoplatinum.driver.behaviour.*;

import penoplatinum.gateway.GatewayClient;

import penoplatinum.grid.Grid;
import penoplatinum.grid.TransformedGrid;
import penoplatinum.map.Map;
import penoplatinum.map.MapHashed;

import penoplatinum.model.part.GridModelPart;

import penoplatinum.navigator.Navigator;
import penoplatinum.navigator.GhostNavigator;

import penoplatinum.robot.GhostRobot;

import penoplatinum.simulator.SimulatedGatewayClient;
import penoplatinum.simulator.Simulator;

import penoplatinum.simulator.entities.SimulatedEntity;
import penoplatinum.simulator.entities.SimulatedEntityFactory;

import penoplatinum.simulator.tiles.Sector;

import penoplatinum.simulator.view.SimulationView;
import penoplatinum.simulator.view.SwingSimulationView;

import penoplatinum.grid.view.SwingGridView;
import penoplatinum.map.MapFactory;
import penoplatinum.map.mazeprotocol.ProtocolMapFactory;
import penoplatinum.util.Bearing;
import penoplatinum.util.Rotation;
import penoplatinum.util.TransformationTRT;
import penoplatinum.util.Utils;

public class FullRobotTest extends TestCase {

  public static void main(String[] args) throws Exception {
    FullRobotTest florian = new FullRobotTest();
    florian.testSimulator();

  }

  public void testSimulator() throws FileNotFoundException {
    Config.load("../../test/fullTest.properties");

    // setup simulator
    Simulator simulator = this.createSimulator(makeMap());

    // add four ghosts ...
    GhostRobot robot1 = this.createGhostRobot("robot1", simulator, 220, 220, -180);
    //GridModelPart.from(robot1.getModel()).getMyAgent().activate();
//
    GhostRobot robot2 = this.createGhostRobot("robot2", simulator, 220, 20, -90);
//    //GridModelPart.from(robot2.getModel()).getMyAgent().activate();

    GhostRobot robot3 = this.createGhostRobot("robot3", simulator, 20, 220, 0);
    //GridModelPart.from(robot3.getModel()).getMyAgent().activate();

    GhostRobot robot4 = this.createGhostRobot("robot4", simulator, 60, 20, 90);
//    //GridModelPart.from(robot4.getModel()).getMyAgent().activate();

    // run the simulator for 30000 steps
    simulator.run(3000000);
  }

  private Simulator createSimulator(Map map) {
    Simulator simulator = new Simulator();
    SimulationView simView = new SwingSimulationView();
    simulator.useMap(map);
    simulator.displayOn(simView);
    return simulator;
  }

  private GhostRobot createGhostRobot(String name, Simulator simulator, int x, int y, int b) {
    // create the robot with all of its parts...
    GhostRobot robot = this.createRobot(name);
    // setup simulator and simulated entity for the robot
    SimulatedEntity entity = this.createSimulatedEntityFor(robot);
    entity.putRobotAt(x, y, b);
    simulator.addSimulatedEntity(entity);
    ;

    SwingGridView gridView = new SwingGridView();
//    Rotation rotation = Bearing.NESW.get((int) (Utils.ClampLooped(b, 0, 360) / 90)).to(Bearing.E);
//    rotation = Rotation.NONE;
//    Grid fullGrid = new TransformedGrid(GridModelPart.from(robot.getModel()).getFullGrid())
//            .setTransformation(new TransformationTRT()
//            .setTransformation(0, 0, rotation, 0, 0));
    
    Grid fullGrid = GridModelPart.from(robot.getModel()).getFullGrid();
    gridView.displayWithoutWindow(fullGrid);
    ((SwingSimulationView) simulator.getView()).addGrid(gridView);

    return robot;
  }

  private GhostRobot createRobot(String name) {
    GhostRobot robot = new GhostRobot(name);
    Driver driver = this.createDriver();
    Navigator navigator = this.createNavigator();
    GatewayClient client = this.createGatewayClient();
    robot.useDriver(driver).useNavigator(navigator).useGatewayClient(client);
    return robot;
  }

  private Driver createDriver() {
    return new ManhattanDriver(0.4).addBehaviour(new FrontProximityDriverBehaviour()).addBehaviour(new SideProximityDriverBehaviour()).addBehaviour(new BarcodeDriverBehaviour()).addBehaviour(new LineDriverBehaviour());
  }

  private Navigator createNavigator() {
    return new GhostNavigator();
  }

  private GatewayClient createGatewayClient() {
    return new SimulatedGatewayClient();
  }

  private SimulatedEntity createSimulatedEntityFor(GhostRobot robot) {
    return SimulatedEntityFactory.make(robot);
  }

  private Map makeMap() throws FileNotFoundException {
    MapFactory mapFac = new ProtocolMapFactory();
    Scanner sc = new Scanner(new File("../../maps/MediumBoard.txt"));
    return mapFac.getMap(sc);
  }

  private Map makeLongMap() {
    MapHashed out = new MapHashed();
    Sector s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    s1.addWall(3);
    out.put(s1, 1, 1);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    out.put(s1, 2, 1);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    out.put(s1, 3, 1);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    s1.addBarcode(15, 3);
    out.put(s1, 4, 1);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    out.put(s1, 5, 1);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    out.put(s1, 6, 1);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    s1.addWall(1);
    out.put(s1, 7, 1);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    s1.addWall(3);
    out.put(s1, 1, 2);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    out.put(s1, 2, 2);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    out.put(s1, 3, 2);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    s1.addBarcode(14, 3);
    out.put(s1, 4, 2);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    out.put(s1, 5, 2);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    out.put(s1, 6, 2);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(2);
    s1.addWall(1);
    out.put(s1, 7, 2);
    return out;
  }

  private Map makeSquareMap() {
    MapHashed out = new MapHashed();
    Sector s1 = new Sector();
    s1.addWall(0);
    s1.addWall(3);
    out.put(s1, 1, 1);

    s1 = new Sector();
    s1.addWall(0);
    out.put(s1, 2, 1);

    s1 = new Sector();
    s1.addWall(1);
    s1.addWall(3);
    out.put(s1, 1, 2);

    s1 = new Sector();
    s1.addWall(1);
    s1.addWall(3);
    s1.addBarcode(19, 2);
    out.put(s1, 2, 2);

    s1 = new Sector();
    s1.addWall(1);
    s1.addWall(2);
    s1.addWall(3);
    out.put(s1, 1, 3);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(1);
    out.put(s1, 3, 1);

    s1 = new Sector();
    s1.addWall(1);
    s1.addWall(2);
    s1.addWall(3);
    out.put(s1, 3, 2);

    s1 = new Sector();
    s1.addWall(2);
    s1.addWall(3);
    out.put(s1, 2, 3);

    s1 = new Sector();
    s1.addWall(0);
    s1.addWall(1);
    s1.addWall(2);
    out.put(s1, 3, 3);

    return out;
  }
}
