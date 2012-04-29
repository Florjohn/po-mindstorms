/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package penoplatinum.grid;

import penoplatinum.util.Bearing;
import penoplatinum.util.Point;
import penoplatinum.util.SimpleHashMap;
import penoplatinum.util.TransformationTRT;

/**
 * This grid only supports read operations!! The grid is the merged result
 * of all the ghosts information
 * 
 * The name of the Main ghost is provided at construction. Information provided
 * by this ghost is given priority
 * 
 * @author MHGameWork
 */
public class MultiGhostGrid implements Grid, GridObserver {

  private SimpleHashMap<String, Ghost> ghosts = new SimpleHashMap<String, Ghost>();
  private final String mainGhostName;
  private AggregatedGrid grid;

  public MultiGhostGrid(String mainGhostName) {
    this.mainGhostName = mainGhostName;

    grid = new AggregatedGrid(getGhostGrid(mainGhostName));

  }

  public Grid getGhostGrid(String name) {
    Ghost g = ghosts.get(name);
    if (g == null)
      g = createNewGhost(name);

    return g.getGrid();

  }

  private Ghost createNewGhost(String name) {
    ObservableGrid grid = new ObservableGrid(new LinkedGrid());
    grid.useObserver(this);
    Ghost g = new Ghost(grid);
    ghosts.put(name, g);

    return g;
  }

  @Override
  public Grid add(Sector s, Point position) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Sector getSectorAt(Point position) {
    return grid.getSectorAt(position);
  }

  @Override
  public Point getPositionOf(Sector sector) {
    return grid.getPositionOf(sector);
  }

  @Override
  public Iterable<Sector> getSectors() {
    return grid.getSectors();
  }

  @Override
  public Grid add(Agent agent, Point position, Bearing bearing) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Grid moveTo(Agent agent, Point position, Bearing bearing) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Sector getSectorOf(Agent agent) {
    return grid.getSectorOf(agent);
  }

  @Override
  public Bearing getBearingOf(Agent agent) {
    return grid.getBearingOf(agent);
  }

  @Override
  public Agent getAgent(String name) {
    return grid.getAgent(name);
  }

  @Override
  public Agent getAgentAt(Point position) {
    return grid.getAgentAt(position);
  }

  @Override
  public Iterable<Agent> getAgents() {
    return grid.getAgents();
  }

  @Override
  public int getMinLeft() {
    return grid.getMinLeft();
  }

  @Override
  public int getMaxLeft() {
    return grid.getMaxLeft();
  }

  @Override
  public int getMinTop() {
    return grid.getMinTop();
  }

  @Override
  public int getMaxTop() {
    return grid.getMaxTop();
  }

  @Override
  public int getWidth() {
    return grid.getWidth();
  }

  @Override
  public int getHeight() {
    return grid.getHeight();
  }

  @Override
  public int getSize() {
    return grid.getSize();
  }

  @Override
  public void agentChanged(Grid g, Agent a) {
    if (!(a instanceof BarcodeAgent))
      return;

    BarcodeAgent barcode = (BarcodeAgent) a;

    for (Ghost ghost : ghosts.values()) {
      Grid iGrid = ghost.getGrid();
      if (iGrid == g)
        continue;

      if (iGrid.getSectorOf(a) == null)
        continue; // Barcode not found on grid

      TransformationTRT transform = mapBarcode(g, iGrid, barcode);

      //TODO: test

      if (g == getGhostGrid(mainGhostName))
        // g is the local ghost, so transform maps from remote to local
        mapGhost(ghosts.get(mainGhostName), transform.invert());
      else
        // g is a remote ghost, so transform maps from local to remote
        mapGhost(findGhostWithGrid(g), transform);

    }


  }

  private Ghost findGhostWithGrid(Grid g) {
    for (Ghost ghost : ghosts.values())
      if (ghost.getGrid() == g)
        return ghost;
    return null;
  }

  /**
   * This function handles what should happen when a mapping is found from the
   * local ghost to a remote ghost
   */
  private void mapGhost(Ghost g, TransformationTRT transform) {
    if (g == getGhostGrid(mainGhostName))
      throw new IllegalArgumentException();

    grid.activateSubGrid(g.getGrid(), transform);

  }

  /**
   * Creates a transformation to transform coordinates from grid 2 to grid 1
   */
  private TransformationTRT mapBarcode(Grid g1, Grid g2, BarcodeAgent barcode) {
    Bearing b1 = g1.getBearingOf(barcode);
    Bearing b2 = g2.getBearingOf(barcode);
    Point pos1 = g1.getPositionOf(g1.getSectorOf(barcode));
    Point pos2 = g1.getPositionOf(g1.getSectorOf(barcode));

    TransformationTRT transform = new TransformationTRT().setTransformation(-pos2.getX(), -pos2.getY(), b2.to(b1), pos1.getX(), pos1.getY());
    return transform;
  }

  class Ghost {

    private Grid grid;
    private TransformationTRT transform;

    public Ghost(Grid grid) {
      this.grid = grid;
    }

    public Grid getGrid() {
      return grid;
    }
  }
}