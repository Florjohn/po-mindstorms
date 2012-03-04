public class MiniSimulation {
  // TEMPORARY CHEATING TRICK !!!
  public static Grid goalGrid;
  
  public static void main(String[] args) {

    // we use a Grid+View+ProxyAgents to create a mini-simualtor
    GridView goalView    = new SwingGridView()
                            .changeTitle("Goal")
                            .changeLocation(100,100);

    // TEMPORARY CHEATING TRICK !!!                      
    // we're using a globally static variable to make it accessible to
    // Navigators to check for agents on adjacent sectors
    goalGrid = new SimpleGrid().load(args[0]).clearAgents();
    ProxyAgent[] proxies   = { new ProxyAgent("0"), new ProxyAgent("1"),
                               new ProxyAgent("2"), new ProxyAgent("3") };
    goalGrid.getSector(2,2).put(new StaticTargetAgent(), Bearing.E);
    goalGrid.getSector(0,0).put(proxies[0], Bearing.N);
    goalGrid.getSector(9,0).put(proxies[1], Bearing.N);
    goalGrid.getSector(0,9).put(proxies[2], Bearing.N);
    goalGrid.getSector(9,9).put(proxies[3], Bearing.N);

    goalGrid.setProcessor(new DiffusionGridProcessor())
            .displayOn(goalView);

    // we instantiate 4 GhostRobots
    Robot[] robots = new GhostRobot[4];
    // each with its own view
    GridView[] views  = new GridView[4];
    // a RobotAPI that uses a proxy to provid valid info about the world
    RobotAPI[] apis   = new MiniSimulationRobotAPI[4];
    // and a RobotAgent
    SimulationRobotAgent[] robotAgents = new SimulationRobotAgent[4];
    // and one common Queue (for the Agents)
    Queue queue = new Queue();
    // and one Spy for logging the conversation
    queue.subscribe(new MessageSpy());

    // instantiate 4 ghosts
    // TODO: create GhostFactory
    for( int r=0; r<4; r++ ) {
      apis[r] = new MiniSimulationRobotAPI(proxies[r]);
      views[r] = new SwingGridView().changeTitle("Discoverer " + r)
                                    .changeLocation(300+(r*200),100);
      robotAgents[r] = new SimulationRobotAgent();
      queue.subscribe(robotAgents[r]);
      robots[r] = new GhostRobot(""+r, views[r])
                    .useRobotAPI(apis[r])
                    .useCommunicationAgent(robotAgents[r]);
    }

    System.out.println("*** ready, press return to start...");
    try { System.in.read(); } catch(Exception e) {}

    while( ! ( robots[0].reachedGoal() && robots[1].reachedGoal() && 
               robots[2].reachedGoal() && robots[3].reachedGoal() ) )
    {
      for(int d=0;d<4;d++) {
        robots[d].step();
        views[d].refresh();
        // System.out.println( robots[d] );
        // try { System.in.read(); } catch(Exception e) {}
      }
      goalGrid.refresh();

      try { Thread.sleep(Integer.parseInt(args[1])); } catch(Exception e) {}
      // System.out.print(".");
    }
    
    // System.out.println("*** done, press return to exit...");
    // try { System.in.read(); } catch(Exception e) {}
    // System.exit(0);
  }
}
